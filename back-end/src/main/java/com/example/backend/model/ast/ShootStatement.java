package com.example.backend.model.ast;

import com.example.backend.model.engine.GameState;
import com.example.backend.model.exception.EvalError;

import java.util.Map;

/**
 * shoot direction expenditure
 */
public record ShootStatement(String direction, Expr expenditure) implements Statement {
    @Override
    public void execute(GameState state, Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {
        // 1. คำนวณค่า x (expenditure) จากนิพจน์
        long x = expenditure.eval(state, localVars, globalVars);
        x = Math.max(0, x); // ป้องกันค่าติดลบ
        long cost = x + 1;

        // 2. ตรวจสอบว่ามีงบประมาณเพียงพอหรือไม่ (ราคารวมคือ x + 1)
        if (state.getPlayerBudget() >= cost) {
            state.pay(cost);
            state.shoot(direction, x);
        }
        // หากเงินไม่พอ ตามกฎคือไม่เกิดอะไรขึ้น (no-op)
    }
}
