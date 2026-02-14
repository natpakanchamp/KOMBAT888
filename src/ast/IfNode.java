package ast;

import exception.EvalError;

import java.util.Map;

public record IfNode(Expr condition, Node thenStatement, Node elseStatement) implements Node {
    @Override
    public void execute(Map<String, Integer> localVars, Map<String, Integer> globalVars) throws EvalError {
        // ในระบบนี้ ค่าที่เป็นบวกถือว่าเป็นจริง (True)
        if (condition.eval(localVars, globalVars) > 0) {
            thenStatement.execute(localVars, globalVars);
        } else {
            elseStatement.execute(localVars, globalVars);
        }
    }
}