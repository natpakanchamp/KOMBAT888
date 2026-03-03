package com.example.backend.model.ast;

import com.example.backend.model.exception.EvalError;

import java.util.Map;

public record WhileStatement(Expr condition, Statement body) implements Statement {
    @Override
    public void execute(Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {
        int loopCount = 0; // ป้องกัน Infinite Loop (Optional safety)

        // [UPDATE] ตามกฎ: Positive (>0) is True
        while (condition.eval(localVars, globalVars) > 0) {
            if (loopCount++ > 1000) throw new EvalError("Infinite loop detected");

            body.execute(localVars, globalVars);
        }
    }
}
