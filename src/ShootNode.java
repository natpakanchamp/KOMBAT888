import java.util.Map;

/**
 * shoot direction expenditure
 */
public record ShootNode(String direction, Expr expenditure) implements Node {
    @Override
    public void execute(Map<String, Integer> localVars, Map<String, Integer> globalVars) throws EvalError {
        // 1. คำนวณค่า x (expenditure) จากนิพจน์
        int x = expenditure.eval(localVars, globalVars);
        x = Math.max(0, x); // ป้องกันค่าติดลบ
        int cost = x + 1;

        // 2. ตรวจสอบว่ามีงบประมาณเพียงพอหรือไม่ (ราคารวมคือ x + 1)
        if (GameState.getPlayerBudget() >= cost) {
            GameState.pay(cost);
            GameState.shoot(direction, x);
        }
        // หากเงินไม่พอ ตามกฎคือไม่เกิดอะไรขึ้น (no-op)
    }
}