package com.example.backend.model.ast;

import com.example.backend.model.engine.GameState;
import com.example.backend.model.engine.Unit;
import com.example.backend.model.exception.EvalError;

import java.util.Map;

public record WhileStatement(Expr condition, Statement body) implements Statement {
    @Override
    public void execute(GameState state, Unit currentUnit, Map<String,
            Long> localVars, Map<String, Long> globalVars) throws EvalError {

        int loopCount = 0; // ป้องกัน Infinite Loop (Optional safety)

        // แค่หยุด loop ไม่ต้อง throw err ตาม spec
        while (loopCount < 10000 &&
                condition.eval(state, currentUnit, localVars, globalVars) > 0) {
            body.execute(state, currentUnit, localVars, globalVars);
            loopCount++;
        }
    }
}
