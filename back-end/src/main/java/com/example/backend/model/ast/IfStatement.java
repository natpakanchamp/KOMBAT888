package com.example.backend.model.ast;

import com.example.backend.model.engine.GameState;
import com.example.backend.model.engine.Unit;
import com.example.backend.model.exception.EvalError;

import java.util.Map;

public record IfStatement(Expr condition, Statement thenBlock, Statement elseBlock) implements Statement {
    @Override
    public void execute(GameState state, Unit currentUnit, Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {
        long value = condition.eval(state, currentUnit, localVars, globalVars);

        // [UPDATE] ตามกฎ: Positive (>0) is True, Negative/Zero (<=0) is False
        if (value > 0) {
            thenBlock.execute(state, currentUnit, localVars, globalVars);
        } else {
            elseBlock.execute(state, currentUnit, localVars, globalVars);
        }
    }
}
