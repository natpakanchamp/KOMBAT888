package com.example.backend.model.ast;

import com.example.backend.model.engine.GameState;
import com.example.backend.model.engine.Unit;
import com.example.backend.model.exception.EvalError;
import java.util.Map;

public record ShootStatement(String direction, Expr expenditure) implements Statement {

    @Override
    public void execute(GameState state, Unit currentUnit, Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {
        long x = expenditure.eval(state, currentUnit, localVars, globalVars);
        x = Math.max(0, x);

        // FIX #1: ลบ logic หักเงินออกจากที่นี่
        // ให้ state.shoot() จัดการผ่าน pay() เพียงที่เดียว
        // ป้องกันการหักเงิน 2 รอบ
        state.shoot(currentUnit, direction, x);
    }
}