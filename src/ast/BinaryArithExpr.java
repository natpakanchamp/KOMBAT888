package ast;

import exception.EvalError;

import java.util.Map;

public record BinaryArithExpr(
        Expr left, String op, Expr right)
        implements Expr {

    @Override
    public long eval(Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {
        // ส่งต่อทั้ง localVars และ globalVars ลงไปยัง ast.Node ลูกทั้งสองข้าง
        long lv = left.eval(localVars, globalVars);
        long rv = right.eval(localVars, globalVars);

        return switch (op) {
            case "+" -> lv + rv;
            case "-" -> lv - rv;
            case "*" -> lv * rv;
            case "/" -> {
                if (rv == 0) throw new EvalError("Division by zero");
                yield lv / rv;
            }
            case "%" -> {
                if (rv == 0) throw new EvalError("Division by zero");
                yield lv % rv;
            }
            case "^" -> (long) Math.pow(lv, rv);
            default -> throw new EvalError("Unknown operator: " + op);
        };
    }
}