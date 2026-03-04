package com.example.backend.model.ast;

import com.example.backend.model.engine.GameState;
import com.example.backend.model.engine.Unit; // 🌟 อย่าลืม import Unit
import com.example.backend.model.exception.EvalError;

import java.util.Map;

/**
 * shoot direction expenditure
 */
public record ShootStatement(String direction, Expr expenditure) implements Statement {

    @Override
    // 🚨 1. เพิ่ม `Unit currentUnit` เข้ามาในพารามิเตอร์ เพื่อให้รู้ว่าตัวไหนกำลังรันคำสั่งนี้
    public void execute(GameState state, Unit currentUnit, Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {

        // (ข้อควรระวัง: เมธอด eval ของ Expr ก็ควรจะรับ currentUnit เข้าไปด้วย เผื่อในอนาคตมีตัวแปรอย่าง "hp ของฉัน")
        long x = expenditure.eval(state, currentUnit, localVars, globalVars);
        x = Math.max(0, x); // ป้องกันค่าติดลบ
        long cost = x + 1;

        // 🚨 2. ตรวจสอบกระเป๋าเงินให้ถูกคน (เช็คจาก owner ของยูนิตที่กำลังยิง)
        boolean isPaid = false;

        if (currentUnit.getOwner() == 1) {
            if (state.getP1Budget() >= cost) {
                state.setP1Budget((int) (state.getP1Budget() - cost)); // จ่ายเงิน
                isPaid = true;
            }
        } else if (currentUnit.getOwner() == 2) {
            if (state.getP2Budget() >= cost) {
                state.setP2Budget((int) (state.getP2Budget() - cost)); // จ่ายเงิน
                isPaid = true;
            }
        }

        // 🚨 3. ถ้าจ่ายเงินผ่าน ก็สั่งยิงได้เลย! (ส่งตัวคนยิงเข้าไปด้วย)
        if (isPaid) {
            state.shoot(currentUnit, direction, x);
        }
        // หากเงินไม่พอ isPaid จะเป็น false และข้ามการยิงไปเลย (no-op ตามกฎ)
    }
}