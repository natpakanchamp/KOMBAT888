package parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import exception.SyntaxError;
import exception.CheckException;
import ast.Node;

class ExprParserTest {

    // Helper method เพื่อลดโค้ดซ้ำ
    private Node parse(String source) throws SyntaxError, CheckException {
        Tokenizer tkz = new ExprTokenizer(source);
        Parser parser = new ExprParser(tkz);
        return parser.parse();
    }

    @Test
    void testValidAssignment() {
        assertDoesNotThrow(() -> {
            parse("x = 100");
        });
    }

    @Test
    void testOperatorPrecedence() {
        // ทดสอบลำดับการคำนวณ: คูณหารต้องมาก่อนบวกลบ
        // 1 + 2 * 3 ต้องได้ 7 (ไม่ใช่ 9)
        assertDoesNotThrow(() -> {
            Node node = parse("x = 1 + 2 * 3");
            // ในระดับ Parser เราเช็คแค่ว่า Parse ผ่าน ส่วนค่าที่ถูกต้องจะไปเช็คใน ExecutionTest
        });
    }

    @Test
    void testParentheses() {
        // ทดสอบวงเล็บ (1+2)*3
        assertDoesNotThrow(() -> parse("x = (1 + 2) * 3"));
    }

    @Test
    void testValidCommands() {
        assertDoesNotThrow(() -> {
            parse("move up");
            parse("shoot downleft 5");
            parse("done");
            parse("if (x) then move up else move down");
            parse("while (i - 10) { i = i + 1 }");
        });
    }

    @Test
    void testInvalidSyntaxMissingStatement() {
        // while ที่ไม่มี body หรือ if ที่ไม่มี else
        assertThrows(CheckException.class, () -> parse("if (x) then move up"));
    }

    @Test
    void testInvalidSyntaxUnknownCommand() {
        assertThrows(CheckException.class, () -> parse("dance up")); // ไม่มีคำสั่ง dance
    }

    @Test
    void testMissingParentheses() {
        assertThrows(CheckException.class, () -> parse("if x > 0 then move up else move down")); // ขาด ( )
    }
}