import ast.*;
import exception.*;
import parser.*;
import engine.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // 1. รับชื่อไฟล์จาก Arguments (ถ้าไม่มีให้ใช้ค่า Default)
        String configPath = args.length > 0 ? args[0] : "config.txt";
        String strategyPath = args.length > 1 ? args[1] : "strategy.txt";

        try {
            // --- Initialization Phase ---
            System.out.println("Loading configuration from " + configPath);
            GameConfig.loadConfig(configPath);

            System.out.println("Initializing Game State...");
            GameState.initialize(); // รีเซ็ตกระดานและเงิน

            // --- Spawn Initial Units (สร้างตัวแรกให้ทั้งสองฝ่าย) ---
            System.out.println("Spawning initial minions...");

            // Player 1: เกิดที่ (0, 0)
            GameState.setCurrentPlayer(1);
            GameState.spawnUnit(0, 0, GameConfig.init_hp, 0);
            System.out.println("Player 1 spawned at (0,0)");

            // Player 2: เกิดที่ (7, 7) หรือมุมล่างขวา
            GameState.setCurrentPlayer(2);
            GameState.spawnUnit(7, 7, GameConfig.init_hp, 0);
            System.out.println("Player 2 spawned at (7,7)");

            // --- Parsing Phase (โหลด Strategy ครั้งเดียวใช้ร่วมกัน) ---
            System.out.println("Parsing strategy from " + strategyPath);
            String content = Files.readString(Path.of(strategyPath));
            Tokenizer tokenizer = new ExprTokenizer(content);
            Parser parser = new ExprParser(tokenizer);
            Node strategy = parser.parse();

            // เตรียมตัวแปร Global แยกของใครของมัน (ความจำระยะยาว)
            Map<String, Long> globalVarsP1 = new HashMap<>();
            Map<String, Long> globalVarsP2 = new HashMap<>();

            long maxTurns = GameState.getMaxTurns();

            // --- Game Loop Phase ---
            for (int turn = 1; turn <= maxTurns; turn++) {
                System.out.println("\n========== TURN " + turn + " ==========");

                // >>> PLAYER 1 TURN <<<
                executePlayerTurn(1, strategy, globalVarsP1);

                // >>> PLAYER 2 TURN <<<
                executePlayerTurn(2, strategy, globalVarsP2);

                // จบเทิร์นใหญ่ (นับเวลาเพิ่ม)
                GameState.advanceGlobalTurn();
            }

            // --- Game Over Summary ---
            System.out.println("\n=== GAME OVER ===");

            GameState.setCurrentPlayer(1);
            System.out.println("Player 1 Final Budget: " + GameState.getPlayerBudget());

            GameState.setCurrentPlayer(2);
            System.out.println("Player 2 Final Budget: " + GameState.getPlayerBudget());

        } catch (Exception e) {
            System.err.println("Fatal Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // เมธอดสำหรับรันเทิร์นของผู้เล่น 1 คน (คุม Minion หลายตัว)
    private static void executePlayerTurn(int playerNum, Node strategy, Map<String, Long> globalVars) {
        System.out.println("--- Player " + playerNum + "'s Turn ---");

        // 1. ตั้งค่า Context ว่าเป็นตาของใคร
        GameState.setCurrentPlayer(playerNum);

        // 2. รับรายได้และดอกเบี้ยประจำเทิร์น
        GameState.processTurnIncome();
        System.out.println("Budget: " + GameState.getPlayerBudget());

        // 3. ดึงรายการ Minion ทั้งหมดของ Player นี้มา
        // (ใช้ List snapshot เพื่อป้องกันปัญหาเวลา Minion ขยับตำแหน่งใน Array)
        List<int[]> units = GameState.getPlayerUnitPositions(playerNum);
        System.out.println("Commanding " + units.size() + " minions.");

        // 4. สั่งงาน Minion ทีละตัว
        for (int[] pos : units) {
            int r = pos[0];
            int c = pos[1];

            // ตรวจสอบว่า Unit ยังอยู่ไหม (เผื่อมีกรณีตาย หรือ Error)
            Unit u = GameState.getUnitAt(r, c);
            if (u != null && u.getOwner() == playerNum) {

                // ระบุตำแหน่งตัวที่กำลังทำงาน (GPS)
                GameState.setMinionPos(r, c);

                // สร้าง Local Vars ใหม่เสมอสำหรับ Minion แต่ละตัวในแต่ละเทิร์น
                Map<String, Long> localVars = new HashMap<>();

                try {
                    // รันคำสั่งจากไฟล์ Strategy
                    strategy.execute(localVars, globalVars);
                } catch (DoneException e) {
                    // Minion สั่ง done ถือว่าจบงานตัวนี้
                } catch (EvalError e) {
                    System.err.println("Minion Error at (" + r + "," + c + "): " + e.getMessage());
                }
            }
        }
    }
}