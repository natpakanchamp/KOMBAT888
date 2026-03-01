package ast;

import exception.EvalError;
import engine.*;

import java.util.Map;

public record MoveNode(String directionStr) implements Node {
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