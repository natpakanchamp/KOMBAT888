package com.example.backend.model.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GameConfig {
    // ค่าเริ่มต้น (Default Values)
    public static long spawn_cost = 100;
    public static long hex_purchase_cost = 1000;
    public static long init_budget = 10000;
    public static long init_hp = 100;
    public static long turn_budget = 90;
    public static long max_budget = 23456;
    public static long interest_pct = 5;
    public static long max_turns = 69;
    public static long max_spawns = 47;

    public static void loadConfig(String filePath) {
        Path path = Path.of(filePath);

        // 1. ตรวจสอบว่ามีไฟล์อยู่จริงหรือไม่ก่อนอ่าน
        if (!Files.exists(path)) {
            System.out.println("Config file not found at '" + filePath + "'. Using default values.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(path);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();

                // ข้ามบรรทัดว่าง และบรรทัดที่เป็น Comment
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue;

                // 2. จำกัดการ split แค่ 2 ส่วน ป้องกันกรณีมีเครื่องหมาย = โผล่ไปใน value
                String[] parts = line.split("=", 2);

                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String valueStr = parts[1].trim();

                    try {
                        // 3. ดักจับ Error กรณีที่ค่าใน config ไม่ใช่ตัวเลข
                        long value = Long.parseLong(valueStr);
                        setConfigValue(key, value);
                    } catch (NumberFormatException e) {
                        System.err.println("Config Warning [Line " + (i + 1) + "]: Invalid number format for key '" + key + "'. Value: '" + valueStr + "'");
                    }
                } else {
                    System.err.println("Config Warning [Line " + (i + 1) + "]: Invalid syntax. Expected 'key = value'.");
                }
            }
            System.out.println("Configuration loaded successfully from: " + filePath);
        } catch (IOException e) {
            System.err.println("Load config failed: " + e.getMessage());
        }
    }

    private static void setConfigValue(String key, long value) {
        // 4. (Optional) ป้องกันไม่ให้ค่าติดลบ หากกติกาไม่อนุญาต
        if (value < 0) {
            System.err.println("Config Warning: Negative value not allowed for '" + key + "'. Skipping.");
            return;
        }

        switch (key) {
            case "spawn_cost" -> spawn_cost = value;
            case "hex_purchase_cost" -> hex_purchase_cost = value;
            case "init_budget" -> init_budget = value;
            case "init_hp" -> init_hp = value;
            case "turn_budget" -> turn_budget = value;
            case "max_budget" -> max_budget = value;
            case "interest_pct" -> interest_pct = value;
            case "max_turns" -> max_turns = value;
            case "max_spawns" -> max_spawns = value;

            // 5. แจ้งเตือนหากมี Key แปลกปลอมที่ไม่รู้จักในระบบ
            default -> System.err.println("Config Warning: Unknown configuration key '" + key + "'.");
        }
    }
}