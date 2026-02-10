import java.util.Map;

record BinaryArithExpr(
        Expr left, String op, Expr right)
        implements Expr {

    @Override
    public int eval(Map<String, Integer> localVars, Map<String, Integer> globalVars) throws EvalError {
        // ส่งต่อทั้ง localVars และ globalVars ลงไปยัง Node ลูกทั้งสองข้าง
        int lv = left.eval(localVars, globalVars);
        int rv = right.eval(localVars, globalVars);

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
            case "^" -> (int) Math.pow(lv, rv);
            default -> throw new EvalError("Unknown operator: " + op);
        };
    }
}