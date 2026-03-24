package com.example.backend.model.engine;

import com.example.backend.model.ast.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

public class TerminalGameRunner {

    private static final List<Integer> ALLOWED_MINIONS = List.of(1, 2, 3, 4, 5);
    private static final String MINION_MENU_TEXT = "[ 1=Saber, 2=Archer, 3=Lancer, 4=Caster, 5=Berserker ]";
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== 🚀 KOMBAT AUTO-BATTLER STARTED ===");

        // --- เลือกระบบการเล่น ---
        System.out.println("\n[ เลือกโหมดการเล่น ]");
        System.out.println("1. Duel (คน VS คน)");
        System.out.println("2. Solitaire (คน VS บอท)");
        System.out.println("3. Auto (บอท VS บอท)");
        System.out.print(">> ป้อนหมายเลขโหมด (1-3): ");
        int mode = 1;
        try {
            mode = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("ป้อนข้อมูลไม่ถูกต้อง ค่าเริ่มต้นจะเป็น Duel");
        }

        GameConfig config = GameConfig.loadFromFile("C:\\Users\\User\\IdeaProjects\\KOMBAT888_VNNTTTT\\back-end\\config.txt");
        GameState state = new GameState(8, 8, config);
        Unit.resetId();

        Unit p1Starter = new Unit(config.getInitHp(), 1, Unit.TYPE_SABER, 0, 0);
        p1Starter.setStrategy(StrategyFactory.createStrategy(Unit.TYPE_SABER, 1));
        state.addUnit(p1Starter);

        Unit p2Starter = new Unit(config.getInitHp(), 2, Unit.TYPE_ARCHER, 7, 7);
        p2Starter.setStrategy(StrategyFactory.createStrategy(Unit.TYPE_ARCHER, 2));
        state.addUnit(p2Starter);

        while (state.checkNormalWin() == MatchResult.ONGOING && state.getCurrentTurn() <= config.getMaxTurns()) {

            if (state.getCurrentTurn() > 1) {
                state.applyTurnIncome(config);
            }

            state.cleanUpDeadUnits();
            printBoard(state);

            System.out.println("\n=================================");
            System.out.println("           Turn: " + state.getCurrentTurn()      );
            System.out.println("=================================");
            System.out.println("\n[ Phase 1: จัดการทรัพยากร (ซื้อที่ดิน/วางมินเนียน) ]");

            for (int player = 1; player <= 2; player++) {
                // เช็คว่าผู้เล่นปัจจุบันเป็นบอทหรือไม่
                boolean isBot = (mode == 2 && player == 2) || (mode == 3);

                long currentBudget = (player == 1) ? state.getP1Budget() : state.getP2Budget();
                long hexCost = config.getHexPurchaseCost();
                long spawnCost = config.getSpawnCost();

                if (isBot) {
                    System.out.println("\n--- 🤖 สิทธิ์ของ Player " + player + " (BOT) ---");
                    System.out.println("💰 งบประมาณ: $" + currentBudget);

                    // 1. บอทสุ่มซื้อที่ดิน (ถ้ามีเงินพอและมีที่ว่าง)
                    List<int[]> availableHexes = state.getPurchasableHexes(player);
                    if (!availableHexes.isEmpty() && currentBudget >= hexCost) {
                        // โอกาส 70% ที่จะซื้อที่ดิน เพื่อประหยัดเงินไว้ซื้อมินเนียนบ้าง
                        if (RANDOM.nextInt(100) < 70) {
                            int[] chosenHex = availableHexes.get(RANDOM.nextInt(availableHexes.size()));
                            state.buyHex(chosenHex[0], chosenHex[1], player, hexCost);
                            currentBudget -= hexCost;
                            System.out.println("✅ BOT P" + player + " ขยายอาณาเขตไปที่ [" + chosenHex[0] + "," + chosenHex[1] + "]");
                        } else {
                            System.out.println("⏩ BOT P" + player + " ตัดสินใจเก็บเงินไว้ไม่ซื้อที่ดิน");
                        }
                    }

                    // 2. บอทสุ่มวางมินเนียน (ถ้ามีเงินพอและมีที่ให้วาง)
                    if (currentBudget >= spawnCost) {
                        List<int[]> validSpawnHexes = new ArrayList<>();
                        for (int r = 0; r < state.getBoardRows(); r++) {
                            for (int c = 0; c < state.getBoardCols(); c++) {
                                if (state.getHexOwnership()[r][c] == player && state.getUnitAt(r, c) == null) {
                                    validSpawnHexes.add(new int[]{r, c});
                                }
                            }
                        }

                        if (!validSpawnHexes.isEmpty()) {
                            int[] chosenHex = validSpawnHexes.get(RANDOM.nextInt(validSpawnHexes.size()));
                            int chosenType = ALLOWED_MINIONS.get(RANDOM.nextInt(ALLOWED_MINIONS.size()));

                            Unit newUnit = new Unit(config.getInitHp(), player, chosenType, chosenHex[0], chosenHex[1]);
                            newUnit.setStrategy(StrategyFactory.createStrategy(chosenType, player));
                            state.addUnit(newUnit);

                            if (player == 1) state.setP1Budget(currentBudget - spawnCost);
                            else state.setP2Budget(currentBudget - spawnCost);

                            System.out.println("⚔️ BOT P" + player + " ส่งมินเนียน (คลาส " + chosenType + ") ลงที่ [" + chosenHex[0] + "," + chosenHex[1] + "]");
                        }
                    } else {
                        System.out.println("⏩ BOT P" + player + " เงินไม่พอซื้อมินเนียน ข้ามเทิร์น");
                    }

                    // หน่วงเวลาเล็กน้อยให้คนอ่านทัน
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

                } else {
                    // --- 👤 สิทธิ์ของ Player (คนเล่น) ---
                    System.out.println("\n--- 🎮 สิทธิ์ของ Player " + player + " ---");
                    System.out.println("💰 งบประมาณที่มี: $" + currentBudget);

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
                            // System.out.println("AST Error: " + e.getMessage());
                        }
                    }
                }
            }

            state.setCurrentTurn(state.getCurrentTurn() + 1);
            if(mode == 3) {
                // โหมด Auto หน่วงเวลา 2 วินาทีตอนจบเทิร์นให้ดูภาพรวมทัน
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
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