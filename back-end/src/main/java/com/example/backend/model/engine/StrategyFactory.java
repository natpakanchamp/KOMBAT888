package com.example.backend.model.engine;

import com.example.backend.model.ast.Statement;
import com.example.backend.model.ast.DoneStatement;
import com.example.backend.model.parser.ExprParser;
import com.example.backend.model.parser.ExprTokenizer;
import com.example.backend.model.parser.Tokenizer;

public class StrategyFactory {

    public static Statement createStrategy(int type, int player) {

        String script = "";

        // กำหนดทิศทางเดินหน้าตามฝั่งของผู้เล่น
        // (P1 เดินลงขวา, P2 เดินขึ้นซ้าย)
        String forward = (player == 1) ? "downright" : "upleft";

        // กำหนดสคริปต์ให้แต่ละคลาส
        switch (type) {
            case Unit.TYPE_SABER:
                // ใช้ Budget (พิมพ์ใหญ่) แทน budget >= 1
                // เพราะ Parser ตอนนี้รองรับเฉพาะคณิตศาสตร์ (ค่า > 0 ถือว่าเป็น True)
                script = "if (Budget) then\n" +
                        "    move " + forward + "\n" +
                        "else\n" +
                        "    done\n";
                break;

            case Unit.TYPE_ARCHER:
                // เพิ่ม { } ครอบบล็อก while เพื่อให้ตัวแปร m ถูกบวกค่าป้องกัน Infinite Loop
                script = "t = t + 1\n" +
                        "m = 0\n" +
                        "while (3 - m) {\n" +
                        "    move " + forward + "\n" +
                        "    m = m + 1\n" +
                        "}\n" +
                        "shoot " + forward + " 3\n";
                break;

            case Unit.TYPE_LANCER:
                script = "if (nearby " + forward + ") then\n" +
                        "    shoot " + forward + " 2\n" +
                        "else\n" +
                        "    move " + forward + "\n";
                break;

            default:
                script = "done\n";
                break;
        }

        System.out.println("📜 โหลดสคริปต์ให้ P" + player + " (คลาส " + type + "):\n" + script);

        // =========================================================
        // 🌟 3. จุดเชื่อมต่อ Parser ของจริงเข้ากับ Game Engine 🌟
        // =========================================================
        try {
            // โยนข้อความสคริปต์เข้า Tokenizer
            Tokenizer tokenizer = new ExprTokenizer(script);
            // ให้ Parser แปลง Token เป็นโครงสร้าง AST (Statement)
            ExprParser parser = new ExprParser(tokenizer);

            return parser.parse();

        } catch (Exception e) {
            System.err.println("🚨 Parse Error (P" + player + " / คลาส " + type + "): " + e.getMessage());
            // ถ้าสคริปต์มีปัญหา ให้มินเนียนตัวนั้นยืนนิ่งๆ (done) แทนที่จะปล่อยให้เกมพัง
            return new DoneStatement();
        }
    }
}