package ast;

import exception.EvalError;
import engine.*;

import java.util.Map;
import java.util.Random;

public record Variable(String name) implements Expr {
    private static final Random randomGen = new Random();

    @Override
    public long eval(Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {
        // 1. จัดการ Special Variables (Read-only)
        switch (name) {
            case "row": return GameState.getCurrentRow(); // ดึงแถวปัจจุบันของ Minion
            case "col": return GameState.getCurrentCol(); // ดึงคอลัมน์ปัจจุบันของ Minion
            case "random": return randomGen.nextInt(1000); // สุ่มค่า 0-999
            case "Budget": return GameState.getPlayerBudget(); // งบประมาณที่เหลือ
            case "Int": return GameState.getInterestRate(); // อัตราดอกเบี้ย
            case "MaxBudget": return GameState.getMaxBudget(); // งบสูงสุด
            case "SpawnsLeft": return GameState.getRemainingSpawns(); // จำนวนที่เกิดได้อีก
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