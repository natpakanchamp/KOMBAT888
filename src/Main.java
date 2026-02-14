import ast.*;
import exception.*;
import paser.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

void main() {
    String filePath = "strategy.txt"; // ระบุชื่อไฟล์คำสั่งของคุณ

    try {
        // 1. อ่านไฟล์คำสั่ง
        String content = Files.readString(Path.of(filePath));

        // 2. สร้าง paser.Tokenizer และ paser.ExprParser.Parser จากโค้ดที่คุณเขียนไว้
        Tokenizer tokenizer = new ExprTokenizer(content);
        Parser parser = new ExprParser(tokenizer);

        // 3. แปลงคำสั่งเป็น ast.Node (AST)
        Node strategy = parser.parse();

        // 4. เตรียมตัวแปร (Local และ Global)
        Map<String, Integer> localVars = new HashMap<>();
        Map<String, Integer> globalVars = new HashMap<>();

        // 5. สั่งให้ Minion ทำงานตามกลยุทธ์
        System.out.println("Starting Minion Strategy...");
        strategy.execute(localVars, globalVars);

    } catch (DoneException e) {
        // เมื่อเจอคำสั่ง "done" จะหยุดการทำงานในเทิร์นนี้
        System.out.println("Minion finished its turn.");
    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();
    }
}
