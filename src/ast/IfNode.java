package ast;

import exception.EvalError;
import java.util.Map;

public record IfNode(Expr condition, Node thenBlock, Node elseBlock) implements Node {
    @Override
    public void execute(Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {
        long value = condition.eval(localVars, globalVars);

        // [UPDATE] ตามกฎ: Positive (>0) is True, Negative/Zero (<=0) is False
        if (value > 0) {
            thenBlock.execute(localVars, globalVars);
        } else {
            elseBlock.execute(localVars, globalVars);
        }
    }
}