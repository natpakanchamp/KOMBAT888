package com.example.backend.model.ast;

import com.example.backend.model.engine.GameState;
import com.example.backend.model.engine.Unit;
import com.example.backend.model.exception.EvalError;

import java.util.Map;
import java.util.Random;

public record Variable(String name) implements Expr {
    private static final Random randomGen = new Random();

    @Override
    public long eval(GameState state, Unit currentUnit, Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {
        // 1. จัดการ Special Variables (Read-only)
        switch (name) {
            case "row":
                return currentUnit.getRow(); // ดึงจากยูนิตที่กำลังรันคำสั่ง
            case "col":
                return currentUnit.getCol(); // ดึงจากยูนิตที่กำลังรันคำสั่ง
            case "random":
                return randomGen.nextInt(1000);
            case "Budget":
                // เช็คว่าเป็นของใครแล้วดึงกระเป๋าเงินให้ถูกคน
                return currentUnit.getOwner() == 1 ? state.getP1Budget() : state.getP2Budget();
            case "Int":
                return Math.round(state.getCurrentInterestPctForPlayer(currentUnit.getOwner()));
            case "MaxBudget":
                return state.getMaxBudget();
            case "SpawnsLeft":
                // แยกโควต้าการเกิดตามทีม
                return currentUnit.getOwner() == 1 ? state.getP1RemainingSpawns() : state.getP2RemainingSpawns();
        }

        // 2. จัดการตัวแปรทั่วไป (คืนค่า 0 หากยังไม่มีการกำหนดค่า)
        if (Character.isUpperCase(name.charAt(0))) {
            // ตัวพิมพ์ใหญ่เป็น Global Shared
            return globalVars.getOrDefault(name, 0L);
        } else {
            // ตัวพิมพ์เล็กเป็น Local Private
            return localVars.getOrDefault(name, 0L);
        }
    }
}