package ast;

import java.util.Map;

public record IntLit(int val) implements Expr {
    @Override
    public int eval(Map<String, Integer> localVars, Map<String, Integer> globalVars) {
        return val; // ส่งค่าตัวเลขกลับไปตรงๆ ไม่ต้องคำนวณหรือค้นหาใน Map
    }
}