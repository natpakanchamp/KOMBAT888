package ast;

import exception.EvalError;

import java.util.Map;

public record WhileNode(Expr condition, Node statement) implements Node {
    @Override
    public void execute(Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {
        int counter = 0;
        // ทำงานเมื่อ condition > 0 และวนไม่เกิน 10,000 รอบ
        while (condition.eval(localVars, globalVars) > 0 && counter < 10000) {
            statement.execute(localVars, globalVars);
            counter++;
        }
    }
}