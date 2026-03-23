package com.example.backend.model.engine;

import lombok.Getter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Getter
public class GameConfig {
    private long spawnCost;
    private long hexPurchaseCost;
    private long initBudget;
    private long initHp;
    private long turnBudget;
    private long maxBudget;
    private long interestPct;
    private long maxTurns;
    private long maxSpawns;

    // ฟังก์ชันสำหรับอ่านไฟล์และคืนค่าเป็น Object Config
    public static GameConfig loadFromFile(String filePath) {
        GameConfig config = new GameConfig();

        // 1. ตั้งค่า Default เผื่อกรณีหาไฟล์ไม่เจอ (อิงจาก Sample ในรูป)
        config.spawnCost = 100;
        config.hexPurchaseCost = 1000;
        config.initBudget = 10000;
        config.initHp = 100;
        config.turnBudget = 90;
        config.maxBudget = 23456;
        config.interestPct = 5;
        config.maxTurns = 69;
        config.maxSpawns = 47;

        // 2. พยายามอ่านไฟล์ Config
        try {
            List<String> lines = Files.readAllLines(Path.of(filePath));
            for (String line : lines) {
                line = line.trim();
                // ข้ามบรรทัดว่าง
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String key = parts[0].trim().toLowerCase();
                    long value = Long.parseLong(parts[1].trim());

                    // แมพค่าลงตัวแปร
                    switch (key) {
                        case "spawn_cost" -> config.spawnCost = value;
                        case "hex_purchase_cost" -> config.hexPurchaseCost = value;
                        case "init_budget" -> config.initBudget = value;
                        case "init_hp" -> config.initHp = value;
                        case "turn_budget" -> config.turnBudget = value;
                        case "max_budget" -> config.maxBudget = value;
                        case "interest_pct" -> config.interestPct = value;
                        case "max_turns" -> config.maxTurns = value;
                        case "max_spawns" -> config.maxSpawns = value;
                    }
                }
            }
            System.out.println(" โหลด Configuration จากไฟล์สำเร็จ!");
        } catch (IOException e) {
            System.out.println("️ ไม่พบไฟล์ '" + filePath + "' จะใช้ค่าเริ่มต้น (Default) แทน");
        } catch (NumberFormatException e) {
            System.out.println(" รูปแบบตัวเลขใน Config ผิดพลาด จะใช้ค่าเริ่มต้นแทนบางส่วน");
        }

        return config;
    }
}