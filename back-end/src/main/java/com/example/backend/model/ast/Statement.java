package com.example.backend.model.ast;

import com.example.backend.model.exception.EvalError;

import java.util.Map;

public interface Statement {
    void execute(Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError;
}
