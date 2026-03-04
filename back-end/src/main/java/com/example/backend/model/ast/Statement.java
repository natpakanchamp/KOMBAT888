package com.example.backend.model.ast;

import com.example.backend.model.engine.GameState;
import com.example.backend.model.exception.EvalError;

import java.util.Map;

public interface Statement {
    void execute(GameState state, Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError;
}
