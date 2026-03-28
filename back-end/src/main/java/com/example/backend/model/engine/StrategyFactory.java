package com.example.backend.model.engine;

import com.example.backend.model.ast.Statement;
import com.example.backend.model.ast.DoneStatement;
import com.example.backend.model.parser.ExprParser;
import com.example.backend.model.parser.ExprTokenizer;
import com.example.backend.model.parser.Tokenizer;

public class StrategyFactory {

    public static Statement createStrategy(int type, int player) {

        String script = "";

        // 🌟 1 อาชีพ = 1 สคริปต์ตายตัว ไม่แยกฝั่ง ไม่ใช้การตัดต่อคำใดๆ
        switch (type) {
            case 1: // Saber
                script = """
                      step = 3
                      while(step) {
                          move downright
                          step = step - 1
                      }
                      shoot downright 20
                        """;
                break;

            case 2: // Archer
                script = """
                        if (nearby up) then shoot up 100
                        else {
                            done
                        }
                        """;
                break;

            case 3: // Lancer
                script = """
                        if(row - 4) then {
                            if(col - 4) then {
                                move upleft
                            }
                            else move upright
                        }
                        else {
                            if(col - 4) then {
                                move downleft
                            }
                            else move downright
                        }
                        """;
                break;

            case 4: // Caster
                script = """
                        move downright
                        shoot downright 15
                        move downright
                        """;
                break;

            case 5: // Berserker
                script = """
                        while (1) {
                            if (nearby downright) then
                                shoot downright 50
                            else
                                move downright
                        }
                        """;
                break;

            default:
                script = "done";
                break;
        }

        // =========================================================
        // จุดเชื่อมต่อ Parser เข้ากับ Game Engine (เปิดแจ้งเตือนแล้ว!)
        // =========================================================
        try {
            Tokenizer tokenizer = new ExprTokenizer(script);
            ExprParser parser = new ExprParser(tokenizer);
            return parser.parse();

        } catch (Exception e) {
            // 🚨 เปิดไฟฉายส่องบั๊ก: ถ้า Syntax ผิด จะโวยวายออก Terminal ตรงนี้เลย!
            System.err.println("🚨 Parse Error (P" + player + " / คลาส " + type + "): " + e.getMessage());
            e.printStackTrace();
            return new DoneStatement(); // และให้หมากยืนนิ่งๆ เป็นกระสอบทรายไปก่อน
        }
    }
}