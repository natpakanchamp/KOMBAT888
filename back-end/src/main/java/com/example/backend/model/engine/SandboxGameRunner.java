package com.example.backend.model.engine;

import com.example.backend.model.ast.Statement;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map;

public class SandboxGameRunner {

    private static final Map<Integer, String> MINION_NAMES = Map.of(
            1, "Saber", 2, "Archer", 3, "Lancer", 4, "Caster", 5, "Berserker"
    );

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== 🧪 SANDBOX STRATEGY TESTER ===");

        GameConfig config = GameConfig.loadFromFile("D:\\proj200\\parserDemo\\back-end\\config.txt");
        GameState state = new GameState(8, 8, config);
        Unit.resetId();

        printBoard(state);

        while (true) {
            System.out.println("\n[ เมนูทดสอบ: 1=เสกมินเนียน | 2=รัน 1 เทิร์น | 3=รันรวดเดียว X เทิร์น | 0=ออก ]");
            System.out.print(">> เลือกคำสั่ง: ");
            String cmd = scanner.nextLine().trim();

            if (cmd.equals("0")) {
                System.out.println("👋 ออกจากโหมด Sandbox");
                break;
            }

            if (cmd.equals("1")) {
                System.out.println("คลาส: 1=Saber, 2=Archer, 3=Lancer, 4=Caster, 5=Berserker");
                System.out.print(">> ป้อนข้อมูล (ผู้เล่น คลาส Row Col) เช่น '1 2 4 4' (เสก P1 Archer ที่ 4,4): ");
                try {
                    String[] parts = scanner.nextLine().trim().split(" ");
                    int player = Integer.parseInt(parts[0]);
                    int type = Integer.parseInt(parts[1]);
                    int row = Integer.parseInt(parts[2]); // ใช้ 0-7 ตามกระดานจริง
                    int col = Integer.parseInt(parts[3]); // ใช้ 0-7 ตามกระดานจริง

                    // แอบเสกพื้นที่ให้เป็นของคนนั้นไปเลย มินเนียนจะได้รู้สึกเหมือนอยู่บ้าน
                    state.getHexOwnership()[row][col] = player;

                    // กำหนดค่า Def เริ่มต้นสำหรับการทดสอบเป็น 10
                    int testDef = 10;
                    Unit newUnit = new Unit(testDef, player, type, row, col);

                    // สวมสมอง (Strategy) ให้มินเนียน
                    newUnit.setStrategy(StrategyFactory.createStrategy(type, player));
                    state.addUnit(newUnit);

                    System.out.println("✅ เสก " + MINION_NAMES.get(type) + " ฝั่ง P" + player + " ลงที่ [" + row + "," + col + "] สำเร็จ!");
                    printBoard(state);
                } catch (Exception e) {
                    System.out.println("❌ ข้อมูลไม่ถูกต้อง กรุณาป้อนตัวเลข 4 ตัวเว้นวรรค (เช่น 1 3 2 2)");
                }
            }
            else if (cmd.equals("2")) {
                System.out.println("\n▶️ รันเทิร์นที่ " + state.getCurrentTurn());
                runTurn(state);
                printBoard(state);
            }
            else if (cmd.equals("3")) {
                System.out.print(">> ต้องการรันล่วงหน้ากี่เทิร์น?: ");
                try {
                    int turns = Integer.parseInt(scanner.nextLine().trim());
                    System.out.println("\n⏩ กำลังรัน " + turns + " เทิร์นรวด...");
                    for (int i = 0; i < turns; i++) {
                        runTurn(state);
                    }
                    printBoard(state);
                } catch (Exception e) {
                    System.out.println("❌ ตัวเลขไม่ถูกต้อง");
                }
            }
        }
    }

    // เมธอดสำหรับจำลอง 1 เทิร์น (มีแค่ Execution Phase ไม่มีการซื้อของ)
    private static void runTurn(GameState state) {
        state.cleanUpDeadUnits();
        for (Unit currentUnit : state.getUnits()) {
            if (currentUnit.isAlive()) {
                Statement strategy = currentUnit.getStrategy();
                if (strategy != null) {
                    try {
                        strategy.execute(state, currentUnit, new HashMap<>(), state.getGlobalVars());
                    } catch (Exception e) {
                        System.err.println("🚨 Execute Error [Unit ID: " + currentUnit.getId() + "]: " + e.getMessage());
                    }
                }
            }
        }
        state.setCurrentTurn(state.getCurrentTurn() + 1);
        state.cleanUpDeadUnits();
    }

    // ปริ้นต์กระดานแบบ 0-7 (ตามโค้ดล่าสุดของคุณ Phavit)
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
                    String typeName = MINION_NAMES.getOrDefault(u.getType(), String.valueOf(u.getType()));
                    System.out.print("[ID:" + u.getId() + " P" + u.getOwner() + " " + typeName + " HP:" + u.getHP() + "] ");
                }
            }
            System.out.println();
        }
    }
}