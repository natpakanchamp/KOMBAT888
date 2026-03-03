package com.example.backend.model.ast;

import com.example.backend.model.engine.GameState;
import com.example.backend.model.exception.EvalError;

import java.util.Map;
import java.util.Set;

public record AssignmentStatement(String name, Expr expression) implements Statement {
    @Override
    public void execute(GameState state, Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {
        // 1. ตรวจสอบ Special Variables (Read-only)
        // ถ้าเป็น row, col, Budget, Int, MaxBudget, SpawnsLeft, random ให้ทำ no-op
        if (isSpecialVariable(name)) {
            return;
        }

        // 2. คำนวณค่าจาก Expression
        long value = expression.eval(state, localVars, globalVars);

        // 3. บันทึกค่าตามกฎ Case Sensitivity
        if (Character.isUpperCase(name.charAt(0))) {
            globalVars.put(name, value); // ตัวพิมพ์ใหญ่เป็น Global
        } else {
            localVars.put(name, value); // ตัวพิมพ์เล็กเป็น Local
        }
    }

    private boolean isSpecialVariable(String n) {
        return Set.of("row", "col", "Budget", "Int", "MaxBudget", "SpawnsLeft", "random").contains(n);
    }
}
