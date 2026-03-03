package com.example.backend.model.engine;

import com.example.backend.model.exception.EvalError;

public class StringToDirectionAdapter implements DirectionAdapter {
    @Override
    public DIRECTION adapt(String input) throws EvalError {
        try {
            return DIRECTION.fromString(input);
        } catch (IllegalArgumentException e) {
            // แปลง Exception เป็น exception.EvalError ตามที่ ast.Node ต้องการ
            throw new EvalError("Execution failed: " + e.getMessage());
        }
    }
}
