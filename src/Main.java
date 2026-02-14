import ast.*;
import exception.*;
import paser.*;
import engine.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
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

            GameState.initialize();
            System.out.println("Initial Budget: " + GameState.getPlayerBudget());
            System.out.println("Max Turns: " + GameState.getMaxTurns());

            // --- Parsing Phase (ทำแค่ครั้งเดียว) ---
            System.out.println("Parsing strategy from " + strategyPath);
            String content = Files.readString(Path.of(strategyPath));
            Tokenizer tokenizer = new ExprTokenizer(content);
            Parser parser = new ExprParser(tokenizer);
            Node strategy = parser.parse();

            // เตรียมตัวแปร (Global เก็บค่าข้ามเทิร์นได้, Local รีเซ็ตทุกเทิร์นไหมขึ้นอยู่กับดีไซน์ แต่ปกติ Global คือ Persistent)
            Map<String, Long> globalVars = new HashMap<>();

            // --- Game Loop Phase ---
            int maxTurns = (int) GameConfig.max_turns;

            // วนลูปจนกว่าจะครบเทิร์น หรือ เกมจบ
            for (int turn = 1; turn <= maxTurns; turn++) {
                System.out.println("\n=== TURN " + turn + " ===");

                // 1. เริ่มเทิร์นใหม่ (คำนวณดอกเบี้ย + เพิ่มเงิน)
                GameState.startNewTurn();
                System.out.println("Current Budget: " + GameState.getPlayerBudget());

                // 2. เตรียมตัวแปร Local สำหรับเทิร์นนี้
                Map<String, Long> localVars = new HashMap<>();

                // 3. รัน Strategy (Minion Action)
                try {
                    strategy.execute(localVars, globalVars);
                } catch (DoneException e) {
                    System.out.println("Minion called 'done'. Ending turn.");
                } catch (EvalError e) {
                    System.err.println("Runtime Error: " + e.getMessage());
                    break; // ถ้า error อาจจะหยุดเกมเลย
                }

                // (Optional) เช็คเงื่อนไขจบเกมอื่นๆ เช่น Minion ตายหมด
                // if (GameState.isGameOver()) break;
            }

            System.out.println("\n=== GAME OVER ===");
            System.out.println("Final Budget: " + GameState.getPlayerBudget());

        } catch (Exception e) {
            System.err.println("Fatal Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}