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
                        if (nearby downright) then
                            shoot downright 25
                        else
                            move downright
                        """;
                break;

            case 2: // Archer
                script = """
                        t = t + 1
                        m = 0
                        while (3 - m) {
                            move downright
                            m = m + 1
                        }
                        shoot downright 40
                        """;
                break;

            case 3: // Lancer
                script = """
                          attack = 15 - random % 3
                          sprint = random % 10
                          if (sprint) then step = 2
                          else step = 3
                        
                          dir2 = (random % 6) + 1
                        
                          while (step) {
                              dir = opponent % 10
                        
                              if (nearby up) then
                                  shoot up attack
                              else if (nearby upright) then
                                  shoot upright attack
                              else if (nearby downright) then
                                  shoot downright attack
                              else if (nearby down) then
                                  shoot down attack
                              else if (nearby downleft) then
                                  shoot downleft attack
                              else if (nearby upleft) then
                                  shoot upleft attack
                        
                              else if (dir - 5) then
                                  move upleft
                              else if (dir - 4) then
                                  move downleft
                              else if (dir - 3) then
                                  move down
                              else if (dir - 2) then
                                  move downright
                              else if (dir - 1) then
                                  move upright
                              else if (dir) then
                                  move up
                        
                              else if (dir2 - 5) then
                                  move upleft
                              else if (dir2 - 4) then
                                  move downleft
                              else if (dir2 - 3) then
                                  move down
                              else if (dir2 - 2) then
                                  move downright
                              else if (dir2 - 1) then
                                  move upright
                              else move up
                        
                              step = step - 1
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
        // จุดเชื่อมต่อ Parser เข้ากับ Game Engine (รันแบบเงียบๆ)
        // =========================================================
        try {
            Tokenizer tokenizer = new ExprTokenizer(script);
            ExprParser parser = new ExprParser(tokenizer);
            return parser.parse();

        } catch (Exception e) {
            // ถ้า Syntax สคริปต์มีปัญหา ให้คืนค่า Done (ข้ามเทิร์น) แทน
            return new DoneStatement();
        }
    }
}