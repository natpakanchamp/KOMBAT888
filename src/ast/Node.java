package ast;

import exception.*;

import java.util.Map;

public interface Node {
    void execute(Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError;
}