import java.util.Map;

interface Expr {
    // เมธอดแบบเก่าสำหรับรองรับ bindings ทั่วไป
    default int eval(Map<String, Integer> bindings) throws EvalError {
        // ให้เรียกใช้เมธอด 2 พารามิเตอร์โดยส่ง Map เดียวกันเข้าไปทั้งคู่
        return eval(bindings, bindings);
    }

    // เมธอดหลักที่ต้องใช้ในการประมวลผลกลยุทธ์ Minion
    int eval(Map<String, Integer> localVars, Map<String, Integer> globalVars) throws EvalError;
}