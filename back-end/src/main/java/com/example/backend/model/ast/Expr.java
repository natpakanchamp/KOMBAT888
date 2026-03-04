package com.example.backend.model.ast;

import com.example.backend.model.engine.GameState;
import com.example.backend.model.exception.EvalError;

import java.util.Map;

public interface Expr {
    // เมธอดหลักที่ต้องใช้ในการประมวลผลกลยุทธ์ Minion
    long eval(GameState state, Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError;
}
