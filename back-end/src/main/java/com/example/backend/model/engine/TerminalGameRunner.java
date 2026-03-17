package com.example.backend.model.engine;

import com.example.backend.model.ast.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class TerminalGameRunner {

    // โซนตั้งค่า Minion ที่อนุญาตให้เล่น
    private static final List<Integer> ALLOWED_MINIONS = List.of(1, 2, 3, 4, 5);
    private static final String MINION_MENU_TEXT = "[ 1=Saber, 2=Archer, 3=Lancer, 4=Caster, 5=Berserker ]";

    public static void main(String[] args) {
        System.out.println("=== 🚀 KOMBAT AUTO-BATTLER STARTED ===");

        // โหลด Config จากไฟล์
        GameConfig config = GameConfig.loadFromFile("config.txt");

        // โยน Config ให้ GameState
        GameState state = new GameState(8, 8, config);
        Unit.resetId();
        Scanner scanner = new Scanner(System.in);

        // สร้างตัวเริ่มต้น โดยดึงเลือดเริ่มต้นจาก Config
        Unit p1Starter = new Unit(config.getInitHp(), 1, Unit.TYPE_SABER, 0, 0);
        p1Starter.setStrategy(StrategyFactory.createStrategy(Unit.TYPE_SABER, 1));
        state.addUnit(p1Starter);

        Unit p2Starter = new Unit(config.getInitHp(), 2, Unit.TYPE_ARCHER, 7, 7);
        p2Starter.setStrategy(StrategyFactory.createStrategy(Unit.TYPE_ARCHER, 2));
        state.addUnit(p2Starter);

        while (state.checkNormalWin() == MatchResult.ONGOING && state.getCurrentTurn() <= config.getMaxTurns()) {

            // 🌟 แจกเงินรายเทิร์น + ดอกเบี้ย (เริ่มแจกตั้งแต่เทิร์นที่ 2 เป็นต้นไป)
            if (state.getCurrentTurn() > 1) {
                state.applyTurnIncome(config);
            }

            state.cleanUpDeadUnits();
            printBoard(state);

            System.out.println("\n=================================");
            System.out.println("           Turn: " + state.getCurrentTurn()      );
            System.out.println("=================================");
            System.out.println("\n[ Phase 1: ผู้เล่นจัดการทรัพยากร ]");

            for (int player = 1; player <= 2; player++) {
                System.out.println("\n--- 🎮 สิทธิ์ของ Player " + player + " ---");
                long currentBudget = (player == 1) ? state.getP1Budget() : state.getP2Budget();
                System.out.println("💰 งบประมาณที่มี: $" + currentBudget);

                // ดึงราคา Hex จาก Config
                long hexCost = config.getHexPurchaseCost();
                System.out.print("ต้องการซื้อพื้นที่ 1 ช่อง (Hex ราคา $" + hexCost + ") หรือไม่? (y/n): ");
                String wantToBuyHex = scanner.nextLine().trim().toLowerCase();

                if (wantToBuyHex.equals("y")) {
                    while (true) {
                        List<int[]> availableHexes = state.getPurchasableHexes(player);
                        if (availableHexes.isEmpty()) {
                            System.out.println("⚠️ ไม่มีพื้นที่ที่ติดกับอาณาเขตคุณให้ซื้อแล้ว!");
                            break;
                        }

                        currentBudget = (player == 1) ? state.getP1Budget() : state.getP2Budget();
                        System.out.print("📍 พื้นที่ที่ซื้อได้: ");
                        for (int[] hex : availableHexes) {
                            System.out.print("[" + hex[0] + "," + hex[1] + "] ");
                        }
                        System.out.println("\n(งบเหลือ: $" + currentBudget + ") >> ป้อนพิกัด (row col) หรือพิมพ์ 'done': ");

                        String hexInput = scanner.nextLine().trim().toLowerCase();
                        if (hexInput.equals("done")) break;

                        try {
                            String[] tokens = hexInput.split(" ");
                            int row = Integer.parseInt(tokens[0]);
                            int col = Integer.parseInt(tokens[1]);

                            boolean isValid = false;
                            for (int[] hex : availableHexes) {
                                if (hex[0] == row && hex[1] == col) { isValid = true; break; }
                            }

                            if (isValid) {
                                if (state.buyHex(row, col, player, hexCost)) {
                                    System.out.println("✅ P" + player + " ซื้อพื้นที่ [" + row + "," + col + "] สำเร็จ!");
                                    break;
                                } else {
                                    System.out.println("❌ เงินไม่พอ!");
                                }
                            } else {
                                System.out.println("❌ ซื้อไม่ได้! ต้องเป็นช่องที่ติดกับอาณาเขตของคุณ");
                            }
                        } catch (Exception e) {
                            System.out.println("❌ รูปแบบพิกัดไม่ถูกต้อง!");
                        }
                    }
                }

                // --- ขั้นตอนที่ 2: ซื้อ Minion ---
                System.out.print("ต้องการสร้าง Minion 1 ตัว ลงกระดานหรือไม่? (y/n): ");
                String wantToBuyMinion = scanner.nextLine().trim().toLowerCase();

                if (wantToBuyMinion.equals("y")) {
                    while (true) {
                        List<int[]> validSpawnHexes = new ArrayList<>();
                        for (int r = 0; r < state.getBoardRows(); r++) {
                            for (int c = 0; c < state.getBoardCols(); c++) {
                                if (state.getHexOwnership()[r][c] == player && state.getUnitAt(r, c) == null) {
                                    validSpawnHexes.add(new int[]{r, c});
                                }
                            }
                        }

                        if (validSpawnHexes.isEmpty()) {
                            System.out.println("⚠️ ไม่มีพื้นที่ว่างให้วาง Minion แล้ว!");
                            break;
                        }

                        currentBudget = (player == 1) ? state.getP1Budget() : state.getP2Budget();
                        System.out.print("📍 พื้นที่ที่วางได้: ");
                        for (int[] hex : validSpawnHexes) {
                            System.out.print("[" + hex[0] + "," + hex[1] + "] ");
                        }

                        // ดึงราคา Spawn จาก Config
                        long spawnCost = config.getSpawnCost();
                        System.out.println("\n(งบเหลือ: $" + currentBudget + ", ราคาเกิด: $" + spawnCost + ")");
                        System.out.println("ตัวเลือกอาชีพ: " + MINION_MENU_TEXT);
                        System.out.print(">> ป้อนข้อมูล (หมายเลขอาชีพ row col) เช่น '1 1 0' หรือพิมพ์ 'done': ");

                        String minionInput = scanner.nextLine().trim().toLowerCase();
                        if (minionInput.equals("done")) break;

                        String[] tokens = minionInput.split(" ");
                        if (tokens.length == 3) {
                            try {
                                int type = Integer.parseInt(tokens[0]);
                                int row = Integer.parseInt(tokens[1]);
                                int col = Integer.parseInt(tokens[2]);

                                if (!ALLOWED_MINIONS.contains(type)) {
                                    System.out.println("❌ อาชีพเบอร์ " + type + " ไม่มีให้เลือกในแมตช์นี้ครับ!");
                                    continue;
                                }

                                boolean isValidSpawn = false;
                                for (int[] hex : validSpawnHexes) {
                                    if (hex[0] == row && hex[1] == col) { isValidSpawn = true; break; }
                                }

                                if (isValidSpawn) {
                                    if (currentBudget >= spawnCost) {
                                        // ใช้ config.getInitHp() ในการสร้างตัวละคร
                                        Unit newUnit = new Unit(config.getInitHp(), player, type, row, col);
                                        newUnit.setStrategy(StrategyFactory.createStrategy(type, player));

                                        state.addUnit(newUnit);
                                        if (player == 1) state.setP1Budget(currentBudget - spawnCost);
                                        else state.setP2Budget(currentBudget - spawnCost);

                                        System.out.println("P" + player + " สร้าง Minion (คลาส " + type + ") พร้อมติดตั้งสมองกลสำเร็จ!");
                                        break;
                                    } else {
                                        System.out.println("❌ เงินไม่พอ!");
                                    }
                                } else {
                                    System.out.println("❌ สร้างไม่ได้! พื้นที่ไม่ถูกต้อง");
                                }
                            } catch (Exception e) {
                                System.out.println("❌ รูปแบบไม่ถูกต้อง!");
                            }
                        } else {
                            System.out.println("❌ กรุณาป้อนให้ครบ 3 ค่า");
                        }
                    }
                }
            }

            // ==========================================
            // Phase 2: Execution Phase (Minion รัน AST อัตโนมัติ)
            // ==========================================
            System.out.println("\n🤖 [ Phase 2: Minion กำลังประมวลผลกลยุทธ์ (AST) ] 🤖");

            for (Unit currentUnit : state.getUnits()) {
                if (currentUnit.isAlive()) {
                    Statement strategy = currentUnit.getStrategy();
                    if (strategy != null) {
                        try {
                            strategy.execute(state, currentUnit, new HashMap<>(), state.getGlobalVars());
                        } catch (Exception e) {
                            System.out.println("AST Error: " + e.getMessage());
                        }
                    }
                }
            }

            state.setCurrentTurn(state.getCurrentTurn() + 1);
        }

        state.cleanUpDeadUnits();
        printBoard(state);
        System.out.println("\n=== GAME OVER ===");
        System.out.println("ผลการแข่งขัน: " + (state.checkNormalWin() == MatchResult.ONGOING ? state.evaluateTimeOutWinner() : state.checkNormalWin()));
    }

    private static void printBoard(GameState state) {
        System.out.println("\n  (สัญลักษณ์พื้นที่: x = P1, o = P2, . = ว่าง)");
        System.out.print("  ");
        for (int i = 0; i < state.getBoardCols(); i++) System.out.print(i + " ");
        System.out.println();

        for (int row = 0; row < state.getBoardRows(); row++) {
            System.out.print(row + " ");
            for (int col = 0; col < state.getBoardCols(); col++) {
                Unit u = state.getUnitAt(row, col);
                int owner = state.getHexOwnership()[row][col];

                if (u != null) {
                    System.out.print(u.getId() + " ");
                } else {
                    if (owner == 1) System.out.print("x ");
                    else if (owner == 2) System.out.print("o ");
                    else System.out.print(". ");
                }
            }
            System.out.print("  | ");
            for (Unit u : state.getUnits()) {
                if (u.getRow() == row && u.isAlive()) {
                    System.out.print("[ID:" + u.getId() + " P" + u.getOwner() + " อาชีพ:" + u.getType() + " HP:" + u.getHP() + "] ");
                }
            }
            System.out.println();
        }
    }
}