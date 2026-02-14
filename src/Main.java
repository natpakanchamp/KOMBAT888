import ast.*;
import exception.*;
import paser.*;
import engine.*; // import engine เพื่อเรียกใช้ GameConfig และ GameState

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

void main() {
    String configPath = "config.txt"; // ชื่อไฟล์ Config ของคุณ
    String strategyPath = "strategy.txt";

    try {
        // 1. โหลดค่า Config
        System.out.println("Loading configuration...");
        GameConfig.loadConfig(configPath);

        // 2. ตั้งค่าเริ่มต้นให้ GameState (เช่น เงินเริ่มต้น)
        GameState.initialize();
        System.out.println("Initial Budget: " + GameState.getPlayerBudget());

        // 3. อ่านและรันไฟล์ Strategy ตามปกติ
        String content = Files.readString(Path.of(strategyPath));
        Tokenizer tokenizer = new ExprTokenizer(content);
        Parser parser = new ExprParser(tokenizer);
        Node strategy = parser.parse();

        Map<String, Long> localVars = new HashMap<>();
        Map<String, Long> globalVars = new HashMap<>();

        System.out.println("Starting Minion Strategy...");
        strategy.execute(localVars, globalVars);

    } catch (DoneException e) {
        System.out.println("Minion finished its turn.");
    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();
    }
}