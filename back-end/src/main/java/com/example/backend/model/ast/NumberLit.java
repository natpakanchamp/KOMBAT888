package com.example.backend.model.ast;

import com.example.backend.model.engine.GameState;

import java.util.Map;

public record NumberLit(long val) implements Expr {
    @Override
    public long eval(GameState state, Map<String, Long> localVars, Map<String, Long> globalVars) {
        return val; // ส่งค่าตัวเลขกลับไปตรงๆ ไม่ต้องคำนวณหรือค้นหาใน Map
    }
}
