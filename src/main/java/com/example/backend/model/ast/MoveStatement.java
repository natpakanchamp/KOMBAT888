package com.example.backend.model.ast;

import com.example.backend.model.engine.DIRECTION;
import com.example.backend.model.engine.DirectionAdapter;
import com.example.backend.model.engine.GameState;
import com.example.backend.model.engine.StringToDirectionAdapter;
import com.example.backend.model.exception.EvalError;

import java.util.Map;

public record MoveStatement(String directionStr) implements Statement {
    // สร้าง Adapter ไว้ใช้งาน (หรือใช้ Dependency Injection ก็ได้)
    private static final DirectionAdapter adapter = new StringToDirectionAdapter();

    @Override
    public void execute(Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {
        // ขั้นตอน 1: Adapt ข้อมูล
        DIRECTION dir = adapter.adapt(directionStr);

        // ขั้นตอน 2: Business Logic
        GameState.pay(1L);
        GameState.move(dir.name());
    }
}
