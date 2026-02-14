import ast.Node;
import paser.*;
import utils.FileUtil;

public void RunStrategy(String filePath) {
    // 1. อ่านโค้ดจากไฟล์
    String content = FileUtil.readFile(filePath);

    if (content != null) {
        try {
            // 2. ส่งให้ paser.Tokenizer
            Tokenizer tokenizer = new ExprTokenizer(content);

            // 3. ส่งให้ paser.ExprParser.Parser เพื่อสร้าง ast.Node
            Parser parser = new ExprParser(tokenizer);
            Node strategy = parser.parse();

            // 4. สั่งให้ Execute (เตรียม Map สำหรับตัวแปร)
            // strategy.execute(localVars, globalVars);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

void main() {
}
