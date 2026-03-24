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
                // ใช้ Text Block (พิมพ์ """ แล้วขึ้นบรรทัดใหม่)
                // จุดไหนที่ต้องการแทรกตัวแปร ให้ใส่ %s
                script = """
                        if (Budget) then
                            move %s
                        else
                            done
                        """.formatted(forward);
                break;

            case Unit.TYPE_ARCHER:
                script = """
                        t = t + 1
                        m = 0
                        while (3 - m) {
                            move %s
                            m = m + 1
                        }
                        shoot %s 3
                        """.formatted(forward, forward); // มี %s 2 ตัว ก็ใส่ forward 2 ครั้ง
                break;

            case Unit.TYPE_LANCER:
                script = """
                        if (nearby %s) then
                            shoot %s 2
                        else
                            move %s
                        """.formatted(forward, forward, forward); // มี %s 3 ตัว
                break;

            default:
                script = "done\n";
                break;
        }

        System.out.println("📜 โหลดสคริปต์ให้ P" + player + " (คลาส " + type + "):\n" + script);

        // =========================================================
        // จุดเชื่อมต่อ Parser ของจริงเข้ากับ Game Engine
        // =========================================================
        try {
            Tokenizer tokenizer = new ExprTokenizer(script);
            ExprParser parser = new ExprParser(tokenizer);
            return parser.parse();

        } catch (Exception e) {
            System.err.println("🚨 Parse Error (P" + player + " / คลาส " + type + "): " + e.getMessage());
            return new DoneStatement();
        }
    }
}