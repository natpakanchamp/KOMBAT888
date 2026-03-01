import ast.Node;
import parser.*; // ใช้ package 'parser' ตามที่คุณตั้งไว้ในโปรเจกต์
import utils.FileUtil;
import java.util.HashMap;
import java.util.Map;

public class RunStrategy {

    // เปลี่ยนชื่อเมธอดเป็น run (ตัวพิมพ์เล็ก) และใส่ static เพื่อให้เรียกใช้ง่าย
    public static void run(String filePath) {
        // 1. อ่านโค้ดจากไฟล์
        String content = FileUtil.readFile(filePath);

        if (content != null) {
            try {
                // 2. ส่งให้ Tokenizer
                Tokenizer tokenizer = new ExprTokenizer(content);

                // 3. ส่งให้ Parser เพื่อสร้าง AST Node
                Parser parser = new ExprParser(tokenizer);
                Node strategy = parser.parse();

                // 4. สั่งให้ Execute
                // ต้องเตรียม Map สำหรับเก็บตัวแปร local และ global
                Map<String, Long> localVars = new HashMap<>();
                Map<String, Long> globalVars = new HashMap<>();

                System.out.println("Executing strategy from: " + filePath);
                strategy.execute(localVars, globalVars);
                System.out.println("Execution finished.");

            } catch (Exception e) {
                System.err.println("Error running strategy:");
                e.printStackTrace();
            }
        } else {
            System.err.println("Could not read file: " + filePath);
        }
    }

    public static void main(String[] args) {
        // ตัวอย่างการเรียกใช้งาน
        // ถ้ามี argument ให้ใช้ไฟล์นั้น ถ้าไม่มีให้ใช้ strategy.txt
        String fileToRun = args.length > 0 ? args[0] : "strategy.txt";
        run(fileToRun);
    }
}