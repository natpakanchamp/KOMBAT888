package factory;

import ast.*;

public class ExprFactory {
    private static ExprFactory instance;

    private ExprFactory() {}
    public static ExprFactory getInstance() {
        if (instance == null) {
            instance = new ExprFactory();
        }
        return instance;
    }
    public BinaryArithExpr createBinaryArithExpr(Expr left, String op, Expr right) {
        return new BinaryArithExpr(left, op, right);
    }
    public IntLit createIntLit(int n) {
        return new IntLit(n);
    }
    public Variable createVariable(String name) {
        return new Variable(name);
    }
    public InfoExpr createInfoExpr(String type, String direction) {
        return new InfoExpr(type, direction);
    }
}
