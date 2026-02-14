package ast;

import exception.EvalError;
import engine.GameState;

import java.util.Map;

/**
 * ast.NearbyExpr -> nearby Direction
 * ทำหน้าที่เป็น ast.Node ใน Expression Tree ที่คืนค่าเป็นตัวเลขสถานะของช่องข้างเคียง
 */
public record NearbyExpr(String direction) implements Expr {

    @Override
    public long eval(Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {
        // 1. นำทิศทางที่เก็บไว้ (เช่น "up", "downleft") ไปถาม engine.GameState
        // 2. engine.GameState จะตรวจสอบตำแหน่งปัจจุบันของผู้เล่น และดูช่องที่ระบุ
        Long info = GameState.query("nearby", direction);

        /* ตัวอย่างการส่งคืนค่าของ engine.GameState (สมมติ):
           0   -> ว่างเปล่า
           1   -> เจอเพื่อน (Ally)
           2   -> เจอศัตรู (Opponent)
           -1  -> ขอบแผนที่ / สิ่งกีดขวาง
        */

        return info;
    }

    @Override
    public String toString() {
        return "nearby " + direction;
    }
}