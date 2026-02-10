import java.util.Map;

/**
 * Node สำหรับจัดการข้อมูลสภาพแวดล้อม (InfoExpression)
 * รองรับ: ally, opponent, nearby Direction
 */
record InfoExpr(String type, String direction) implements Expr {

    @Override
    public int eval(Map<String, Integer> localVars, Map<String, Integer> globalVars) throws EvalError {

        return switch (type) {
            case "ally" ->
                    GameState.query("ally", null);

            case "opponent" ->
                    GameState.query("opponent", null);

            case "nearby" ->
                    GameState.query("nearby", direction);

            default ->
                    throw new EvalError("Unknown info type: " + type);
        };
    }
}