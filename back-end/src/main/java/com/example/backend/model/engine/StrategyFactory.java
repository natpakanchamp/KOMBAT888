package com.example.backend.model.engine;

import com.example.backend.model.ast.Statement;

public class StrategyFactory {

    public static Statement createStrategy(int type, int player) {

        // 1. ตัวแปรเก็บข้อความสคริปต์หลายบรรทัด (คลีนๆ ไม่มี #)
        String script = "";

        // 2. กำหนดสคริปต์ให้แต่ละคลาสโดยใช้ Text Blocks (""")
        switch (type) {
            case Unit.TYPE_SABER:
                script = """
                         if (budget >= 1) then
                             move downright
                         else
                             done
                         """;
                break;

            case Unit.TYPE_ARCHER:
                script = """
                         t = t + 1
                         m = 0
                         while (3 - m)
                             move upleft
                             m = m + 1
                         shoot upleft 3
                         """;
                break;

            case Unit.TYPE_LANCER:
                script = """
                         if (nearby downright) then
                             shoot downright 2
                         else
                             move downright
                         """;
                break;

            default:
                script = "done\n";
                break;
        }

        System.out.println("📜 โหลดสคริปต์ให้ P" + player + " (คลาส " + type + "):\n" + script);

        // =========================================================
        // 🌟 3. จุดเชื่อมต่อ Parser ของจริง! 🌟
        // =========================================================
        // ถ้าเอาโค้ด Parser (จากโปรเจกต์ AST) มาใส่เตรียมไว้แล้ว
        // ให้เอา Comment 2 บรรทัดข้างล่างนี้ออก แล้วลบ Mock AST ด้านล่างทิ้งได้เลยครับ!

        // Statement realAST = Parser.parseString(script);
        // return realAST;


        // ⚠️ (ชั่วคราว) คืนค่าเป็น Mock AST ไปก่อน เพื่อไม่ให้เกมพังระหว่างรอต่อ Parser
        return (state, currentUnit, localVars, globalVars) -> {
            String forwardDir = (player == 1) ? "downright" : "upleft";
            if (type == Unit.TYPE_SABER) {
                if (state.pay(currentUnit, 1)) state.move(currentUnit, forwardDir);
            } else if (type == Unit.TYPE_ARCHER) {
                if (state.pay(currentUnit, 3)) state.shoot(currentUnit, forwardDir, 3);
            } else if (type == Unit.TYPE_LANCER) {
                if (state.pay(currentUnit, 2)) state.shoot(currentUnit, forwardDir, 2);
            }
        };
    }
}