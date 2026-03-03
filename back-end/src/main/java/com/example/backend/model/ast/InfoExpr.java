package com.example.backend.model.ast;

import com.example.backend.model.engine.GameState;
import com.example.backend.model.exception.EvalError;

import java.util.Map;

/**
 * ast.Node สำหรับจัดการข้อมูลสภาพแวดล้อม (InfoExpression)
 * รองรับ: ally, opponent, nearby Direction
 */
public record InfoExpr(String type, String direction) implements Expr {

    @Override
    public long eval(GameState state, Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {

        return switch (type) {
            case "ally" ->
                    state.query("ally", null);

            case "opponent" ->
                    state.query("opponent", null);

            case "nearby" ->
                    state.query("nearby", direction);

            default ->
                    throw new EvalError("Unknown info type: " + type);
        };
    }
}