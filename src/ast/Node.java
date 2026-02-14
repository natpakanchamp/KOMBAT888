package ast;

import exception.*;

import java.util.Map;

public interface Node {
    void execute(Map<String, Integer> localVars, Map<String, Integer> globalVars) throws EvalError;
}