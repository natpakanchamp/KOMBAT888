package com.example.backend.model.engine;

import com.example.backend.model.ast.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class TerminalGameRunner {

    public static void main(String[] args) {
        GameState state = new GameState(8, 8, 50, 100);
        Unit.resetId();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== 🚀 KOMBAT AUTO-BATTLER STARTED ===");

        // 🌟 1. สร้างตัวเริ่มต้น และ "ยัดสมอง (Strategy)" ให้มันทันที
        Unit p1Starter = new Unit(10, 1, Unit.TYPE_SABER, 0, 0);
        p1Starter.setStrategy(getMockStrategyForType(Unit.TYPE_SABER, 1));
        state.addUnit(p1Starter);

        Unit p2Starter = new Unit(10, 2, Unit.TYPE_ARCHER, 7, 7); // สมมติ Archer เลือด 10 เท่ากันก่อน
        p2Starter.setStrategy(getMockStrategyForType(Unit.TYPE_ARCHER, 2));
        state.addUnit(p2Starter);

        while (state.checkNormalWin() == MatchResult.ONGOING && state.getCurrentTurn() <= state.getMaxTurns()) {
            state.cleanUpDeadUnits();
            printBoard(state);

            System.out.println("\n=================================");
            System.out.println("           Turn: " + state.getCurrentTurn()      );
            System.out.println("=================================");

            System.out.println("\n[ Phase 1: ผู้เล่นจัดการทรัพยากร ]");

            for (int player = 1; player <= 2; player++) {
                System.out.println("\n--- 🎮 สิทธิ์ของ Player " + player + " ---");
                int currentBudget = (player == 1) ? state.getP1Budget() : state.getP2Budget();
                System.out.println("💰 งบประมาณที่มี: $" + currentBudget);

                // --- ขั้นตอนที่ 1: ซื้อ Hex ---
                int hexCost = 15;
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
                                if (hex[0] == row && hex[1] == col) {
                                    isValid = true; break;
                                }
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

                        int budget = (player == 1) ? state.getP1Budget() : state.getP2Budget();
                        System.out.print("📍 พื้นที่ที่วางได้: ");
                        for (int[] hex : validSpawnHexes) {
                            System.out.print("[" + hex[0] + "," + hex[1] + "] ");
                        }

                        System.out.println("\n(งบเหลือ: $" + budget + ", ราคาเกิด: $20)");
                        System.out.println("ตัวเลือกอาชีพ: [ 1 = Saber, 2 = Archer ]");
                        System.out.print(">> ป้อนข้อมูล (หมายเลขอาชีพ row col) เช่น '1 1 0' หรือพิมพ์ 'done': ");

                        String minionInput = scanner.nextLine().trim().toLowerCase();
                        if (minionInput.equals("done")) break;

                        String[] tokens = minionInput.split(" ");
                        if (tokens.length == 3) {
                            try {
                                int type = Integer.parseInt(tokens[0]);
                                int row = Integer.parseInt(tokens[1]);
                                int col = Integer.parseInt(tokens[2]);
                                int spawnCost = 20;

                                boolean isValidSpawn = false;
                                for (int[] hex : validSpawnHexes) {
                                    if (hex[0] == row && hex[1] == col) {
                                        isValidSpawn = true; break;
                                    }
                                }

                                if (isValidSpawn) {
                                    if (budget >= spawnCost) {
                                        Unit newUnit = new Unit(10, player, type, row, col);

                                        // 🌟 2. ยัดสมองให้ Minion ตัวใหม่ตามอาชีพที่เลือก
                                        newUnit.setStrategy(getMockStrategyForType(type, player));

                                        state.addUnit(newUnit);
                                        if (player == 1) state.setP1Budget(budget - spawnCost);
                                        else state.setP2Budget(budget - spawnCost);

                                        System.out.println("✅ P" + player + " สร้าง Minion พร้อมติดตั้งสมองกลสำเร็จ!");
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
                            // ให้ Minion รันสมองของตัวเอง
                            strategy.execute(state, currentUnit, new HashMap<>(), state.getGlobalVars());
                        } catch (Exception e) {
                            System.out.println("⚠️ AST Error: " + e.getMessage());
                        }
                    }
                }
            }

            state.setCurrentTurn(state.getCurrentTurn() + 1);
        }

        state.cleanUpDeadUnits();
        printBoard(state);
        System.out.println("\n=== 🏁 GAME OVER ===");
        System.out.println("ผลการแข่งขัน: " + (state.checkNormalWin() == MatchResult.ONGOING ? state.evaluateTimeOutWinner() : state.checkNormalWin()));
    }

    // ==========================================
    // 🌟 ฟังก์ชันสร้าง Mock AST ฝังในโค้ด (Lambda Expression)
    // ==========================================
    private static Statement getMockStrategyForType(int type, int player) {
        return (state, currentUnit, localVars, globalVars) -> {

            // กำหนดทิศทางบุก: P1 บุกตะวันออกเฉียงใต้ (downright), P2 บุกตะวันตกเฉียงเหนือ (upleft)
            String forwardDir = (player == 1) ? "downright" : "upleft";

            if (type == Unit.TYPE_SABER) {
                // สมอง Saber: ถ้ามีเงิน 1 บาท ให้เดินหน้า 1 ช่อง
                if (state.pay(currentUnit, 1)) {
                    state.move(currentUnit, forwardDir);
                    System.out.println("⚔️ [AST] Saber (P" + player + ") เดินหน้าไปทาง " + forwardDir + " เสีย 1 บาท");
                } else {
                    System.out.println("💤 [AST] Saber (P" + player + ") เงินไม่พอเดิน ยืนนิ่ง");
                }
            }
            else if (type == Unit.TYPE_ARCHER) {
                // สมอง Archer: ยิงสาดไปข้างหน้า ระยะ 2 ช่อง (สมมติว่าใช้เงิน 3 บาท)
                if (state.pay(currentUnit, 3)) {
                    state.shoot(currentUnit, forwardDir, 2);
                    System.out.println("🏹 [AST] Archer (P" + player + ") ยิงธนูไปทาง " + forwardDir + " ระยะ 2 ช่อง เสีย 3 บาท!");
                } else {
                    System.out.println("💤 [AST] Archer (P" + player + ") เงินไม่พอยิง ยืนนิ่ง");
                }
            }
        };
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