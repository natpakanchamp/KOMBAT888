package com.example.backend.model.factory;

import com.example.backend.model.ast.*;

import java.util.List;

public class StatementFactory {
    private static StatementFactory instance;

    private StatementFactory() {}
    public static StatementFactory getInstance() {
        if (instance == null) {
            instance = new StatementFactory();
        }
        return instance;
    }
    public BlockStatement createBlockStatement(List<Statement> statements) {
        return new BlockStatement(statements);
    }
    public AssignmentStatement createAssignmentStatement(String name, Expr expression) {
        return new AssignmentStatement(name, expression);
    }
    public DoneStatement createDoneStatement() {
        return new DoneStatement();
    }
    public MoveStatement createMoveStatement(String directionStr) {
        return new MoveStatement(directionStr);
    }
    public ShootStatement createShootStatement(String direction, Expr expenditure) {
        return new ShootStatement(direction, expenditure);
    }
    public IfStatement createIfStatement(Expr condition, Statement thenStatement, Statement elseStatement) {
        return new IfStatement(condition, thenStatement, elseStatement);
    }
    public WhileStatement createWhileStatement(Expr condition, Statement statement) {
        return new WhileStatement(condition, statement);
    }
}
