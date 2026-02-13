import java.util.List;
import java.util.Map;

public record BlockNode(List<Node> statements) implements Node {
    @Override
    public void execute(Map<String, Integer> localVars, Map<String, Integer> globalVars) throws EvalError {
        for (Node statement : statements) {
            statement.execute(localVars, globalVars);
        }
    }
}