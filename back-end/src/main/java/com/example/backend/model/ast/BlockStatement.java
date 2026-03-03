package com.example.backend.model.ast;

import com.example.backend.model.engine.GameState;
import com.example.backend.model.exception.EvalError;

import java.util.List;
import java.util.Map;

public record BlockStatement(List<Statement> statements) implements Statement {
    @Override
    public void execute(GameState state, Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {
        for (Statement statement : statements) {
            statement.execute(state, localVars, globalVars);
        }
    }
}
