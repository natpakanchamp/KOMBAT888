package com.example.backend.model.ast;

import com.example.backend.model.engine.*;
import com.example.backend.model.exception.EvalError;

import java.util.Map;

public record MoveStatement(String directionStr) implements Statement {
    private static final DirectionAdapter adapter = new StringToDirectionAdapter();

    @Override
    public void execute(GameState state, Unit currentUnit,
                        Map<String, Long> localVars, Map<String,
                    Long> globalVars) throws EvalError {
        // ขั้นตอน 1: Adapt ข้อมูลทิศทาง
        DIRECTION dir = adapter.adapt(directionStr);

        long cost = 1L; // ค่าใช้จ่ายในการเดิน
        boolean isPaid = false;

        // ขั้นตอน 2: เช็คเงินว่าพอไหม และหักเงินให้ถูกทีม
        if (currentUnit.getOwner() == 1 && state.getP1Budget() >= cost) {
            state.setP1Budget((int) (state.getP1Budget() - cost));
            isPaid = true;
        } else if (currentUnit.getOwner() == 2 && state.getP2Budget() >= cost) {
            state.setP2Budget((int) (state.getP2Budget() - cost));
            isPaid = true;
        }

        // ขั้นตอน 3: ถ้าจ่ายเงินสำเร็จ ค่อยขยับตัว
        if (isPaid) {
            state.move(currentUnit, dir.name());
        }
    }
}