package com.example.backend.model.ast;

import com.example.backend.model.exception.EvalError;

import java.util.Map;

public interface Expr {
    default long eval(Map<String, Long> bindings) throws EvalError {
        // ให้เรียกใช้เมธอด 2 พารามิเตอร์โดยส่ง Map เดียวกันเข้าไปทั้งคู่
        return eval(bindings, bindings);
    }

    // เมธอดหลักที่ต้องใช้ในการประมวลผลกลยุทธ์ Minion
    long eval(Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError;
}
