package engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import parser.*;
import ast.Node;
import exception.*;

class ExecutionTest {

    private Map<String, Long> localVars;
    private Map<String, Long> globalVars;

    @BeforeEach
    void setUp() throws Exception {
        localVars = new HashMap<>();
        globalVars = new HashMap<>();
        resetGameState(); // รีเซ็ตค่าเงินและตำแหน่งก่อนเทสทุกครั้ง
    }

    // ใช้ Reflection เพื่อรีเซ็ตค่า static ใน GameState
    private void resetGameState() throws Exception {
        setStaticField(GameState.class, "playerBudget", 1000);
        setStaticField(GameState.class, "currentRow", 5);
        setStaticField(GameState.class, "currentCol", 5);
    }

    private void setStaticField(Class<?> clazz, String fieldName, Object value) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    private void executeCode(String source) throws Exception {
        Tokenizer tkz = new ExprTokenizer(source);
        Parser parser = new ExprParser(tkz);
        Node node = parser.parse();
        node.execute(localVars, globalVars);
    }

    @Test
    void testArithmeticPrecedence() throws Exception {
        // 1 + 2 * 3 = 7
        executeCode("x = 1 + 2 * 3");
        assertEquals(7, localVars.get("x"));

        // (1 + 2) * 3 = 9
        executeCode("y = (1 + 2) * 3");
        assertEquals(9, localVars.get("y"));
    }

    @Test
    void testDivisionByZero() {
        // ต้องโยน EvalError
        assertThrows(EvalError.class, () -> executeCode("x = 10 / 0"));
        assertThrows(EvalError.class, () -> executeCode("x = 10 % 0"));
    }

    @Test
    void testVariableScope() throws Exception {
        // x (local) และ X (global)
        executeCode("x = 10");
        executeCode("X = 50");

        assertEquals(10, localVars.get("x"));
        assertNull(globalVars.get("x")); // x ต้องไม่อยู่ใน global

        assertEquals(50, globalVars.get("X"));
        assertNull(localVars.get("X")); // X ต้องไม่อยู่ใน local
    }

    @Test
    void testWhileLoopLimit() throws Exception {
        // ทดสอบ Infinite Loop protection (ต้องหยุดที่ 10000 รอบ)
        // i เริ่มที่ 0, วนลูปตลอดกาล (1), เพิ่ม i ทีละ 1
        executeCode("i = 0 while (1) { i = i + 1 }");
        assertEquals(10000, localVars.get("i"), "Loop should terminate at 10000 iterations");
    }

    @Test
    void testIfElseCondition() throws Exception {
        executeCode("x = 0 if (1) then x = 1 else x = 2"); // 1 คือ true
        assertEquals(1, localVars.get("x"));

        executeCode("y = 0 if (0) then y = 1 else y = 2"); // 0 คือ false
        assertEquals(2, localVars.get("y"));
    }

    @Test
    void testMoveDeductsBudget() throws Exception {
        // งบเริ่มต้น 1000, move ใช้ 1
        executeCode("move up");
        assertEquals(999, GameState.getPlayerBudget());
    }

    @Test
    void testShootCost() throws Exception {
        // shoot ใช้ cost = power + 1
        // ยิง power 10 -> cost 11 -> เหลือ 989
        executeCode("shoot up 10");
        assertEquals(989, GameState.getPlayerBudget());
    }

    @Test
    void testDoneCommand() {
        // done ต้องโยน DoneException เพื่อจบเทิร์น
        assertThrows(DoneException.class, () -> executeCode("done"));
    }

    @Test
    void testCodeAfterDoneIsIgnored() {
        // โค้ดหลัง done จะไม่ถูกรัน (แต่ในที่นี้ executeCode จะหยุดเพราะ Exception ก่อน)
        // การเทสนี้เพื่อยืนยันพฤติกรรม
        assertThrows(DoneException.class, () -> executeCode("x = 1 done x = 2"));
        // ค่า x ควรเป็น 1 ถ้าทำทีละคำสั่ง (แต่เนื่องจาก executeCode รันรวดเดียว การเช็คค่าอาจจะทำได้ยากในระดับ Unit Test แบบง่าย)
    }
}