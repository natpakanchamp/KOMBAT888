import java.util.Map;

interface Node {
    void execute(Map<String, Integer> localVars, Map<String, Integer> globalVars) throws EvalError;
}