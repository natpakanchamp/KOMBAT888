package ast;

import exception.EvalError;
import engine.GameState;

import java.util.Map;

/**
 * ast.Node สำหรับจัดการข้อมูลสภาพแวดล้อม (InfoExpression)
 * รองรับ: ally, opponent, nearby Direction
 */
public record InfoExpr(String type, String direction) implements Expr {

    @Override
    public long eval(Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {

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