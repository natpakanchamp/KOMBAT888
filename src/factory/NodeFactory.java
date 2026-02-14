package factory;

import ast.*;
import java.util.List;

public class NodeFactory {
    private static NodeFactory instance;

    private NodeFactory() {}
    public static NodeFactory getInstance() {
        if (instance == null) {
            instance = new NodeFactory();
        }
        return instance;
    }
    public BlockNode createBlockNode(List<Node> statements) {
        return new BlockNode(statements);
    }
    public AssignmentNode createAssignmentNode(String name, Expr expression) {
        return new AssignmentNode(name, expression);
    }
    public DoneNode createDoneNode() {
        return new DoneNode();
    }
    public MoveNode createMoveNode(String directionStr) {
        return new MoveNode(directionStr);
    }
    public ShootNode createShootNode(String direction, Expr expenditure) {
        return new ShootNode(direction, expenditure);
    }
    public IfNode createIfNode(Expr condition, Node thenStatement, Node elseStatement) {
        return new IfNode(condition, thenStatement, elseStatement);
    }
    public WhileNode createWhileNode(Expr condition, Node statement) {
        return new WhileNode(condition, statement);
    }
}
