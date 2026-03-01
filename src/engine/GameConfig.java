package engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GameConfig {
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
        try {
            List<String> lines = Files.readAllLines(Path.of(filePath));
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    setConfigValue(parts[0].trim(), Long.parseLong(parts[1].trim()));
                }
            }
        } catch (IOException e) {
            System.err.println("Load config failed: " + e.getMessage());
        }
    }

    private static void setConfigValue(String key, long value) {
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
        }
    }
}