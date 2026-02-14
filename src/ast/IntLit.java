package ast;

import java.util.Map;

public record IntLit(long val) implements Expr {
    @Override
    public long eval(Map<String, Long> localVars, Map<String, Long> globalVars) {
        return val; // ส่งค่าตัวเลขกลับไปตรงๆ ไม่ต้องคำนวณหรือค้นหาใน Map
    }
}