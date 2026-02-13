import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExprParser implements Parser {
    private Tokenizer tkz;
    private static final Set<String> ACTION = Set.of("done", "move", "shoot");
    private static final Set<String> DIRECTION = Set.of("up", "down", "upleft", "upright", "downleft", "downright");
    public ExprParser(Tokenizer tkz) {
        this.tkz = tkz;
    }
    public static boolean isDirection(String str) {
        return str != null && DIRECTION.contains(str);
    }
    public static boolean isNumeric(String str) {
        return str != null && str.matches("\\d+");
    }
    public static boolean isIdentifier(String str) {
        return str != null && str.matches("[a-zA-Z][a-zA-Z0-9]*");
    }
    @Override
    public Node parse() throws CheckException, SyntaxError {
        // parseStrategy จะทำหน้าที่รวบรวม Statement+ ทั้งหมดมาเป็น BlockNode
        return parseStrategy();
    }
    private Node parseStrategy() throws CheckException, SyntaxError {
        List<Node> statements = new ArrayList<>();
        // วนลูปอ่านจนกว่า Token จะหมด เพื่อรองรับ Statement+
        while (tkz.hasNextToken()) {
            statements.add(parseStatement());
        }
        // ตรวจสอบกฎ "+" (ต้องมีอย่างน้อย 1 Statement)
        if (statements.isEmpty()) {
            throw new SyntaxError("Strategy requires at least one statement");
        }
        return new BlockNode(statements);
    }
    private Node parseStatement() throws CheckException, SyntaxError {
        if (tkz.peek("{")) return parseBlockStatement();
        else if (tkz.peek("if")) return parseIfStatement();
        else if (tkz.peek("while"))  return parseWhileStatement();
        return parseCommand();
    }
    private Node parseCommand() throws CheckException, SyntaxError {
        if(!ACTION.contains(tkz.peek())) {
            return parseAssignmentStatement();
        }
        return parseActionCommand();
    }
    private Node parseAssignmentStatement() throws CheckException, SyntaxError {
        String identifier = tkz.consume();
        tkz.consume("=");
        Expr e = parseExpression();
        return new AssignmentNode(identifier, e);
    }
    private Node parseActionCommand() throws CheckException, SyntaxError {
        if(tkz.peek("done")) {
            tkz.consume("done");
            return new DoneNode();
        }
        else if(tkz.peek("move")) return parseMoveCommand();
        else if(tkz.peek("shoot")) return parseAttackCommand();
        throw new SyntaxError("Unknown action: " + tkz.peek());
    }
    private Node parseMoveCommand() throws CheckException, SyntaxError{
        tkz.consume("move");
        String dir = parseDirection();
        return new MoveNode(dir);
    }
    private Node parseAttackCommand() throws CheckException, SyntaxError {
        tkz.consume("shoot");
        String dir = parseDirection();
        Expr e = parseExpression();
        return new ShootNode(dir, e);
    }
    private String parseDirection() throws CheckException, SyntaxError {
        String dir = tkz.peek();
        if (isDirection(dir)) {
            return tkz.consume();
        }
        throw new SyntaxError("Invalid direction: " + dir);
    }
    private Node parseBlockStatement () throws CheckException, SyntaxError {
        List<Node> statements = new ArrayList<>();
        tkz.consume("{");
        while (tkz.hasNextToken() && !tkz.peek("}")) {
            statements.add(parseStatement());
        }
        tkz.consume("}");
        return new BlockNode(statements);
    }
    private Node parseIfStatement() throws CheckException, SyntaxError {
        tkz.consume("if");
        tkz.consume("(");
        Expr e = parseExpression();
        tkz.consume(")");
        tkz.consume("then");
        Node nThen = parseStatement();
        tkz.consume("else");
        Node nElse = parseStatement();
        return new IfNode(e, nThen, nElse);
    }
    private Node parseWhileStatement() throws CheckException, SyntaxError {
        tkz.consume("while");
        tkz.consume("(");
        Expr e = parseExpression();
        tkz.consume(")");
        Node n = parseStatement();
        return new WhileNode(e, n);
    }
    private Expr parseExpression() throws CheckException, SyntaxError {
        Expr t = parseTerm();
        while(tkz.peek("+") || tkz.peek("-")) {
            if(tkz.peek().equals("+")) {
                tkz.consume();
                t = new BinaryArithExpr(t, "+", parseTerm());
            } else {
                tkz.consume();
                t = new BinaryArithExpr(t, "-", parseTerm());
            }
        }
        return t;
    }
    private Expr parseTerm() throws CheckException, SyntaxError {
        Expr f = parseFactor();
        while(tkz.peek("*") || tkz.peek("/") || tkz.peek("%")) {
            if(tkz.peek().equals("*")) {
                tkz.consume();
                f = new BinaryArithExpr(f, "*", parseFactor());
            } else if(tkz.peek("/")) {
                tkz.consume();
                f = new BinaryArithExpr(f, "/", parseFactor());
            } else {
                tkz.consume();
                f = new BinaryArithExpr(f, "%", parseFactor());
            }
        }
        return f;
    }
    private Expr parseFactor() throws CheckException, SyntaxError {
        Expr p = parsePower();
        if(tkz.peek("^")) {
            tkz.consume();
            p = new BinaryArithExpr(p, "^", parseFactor());
        }
        return p;
    }
    private Expr parsePower() throws CheckException, SyntaxError {
        if(isNumeric(tkz.peek())) {
            String n = tkz.consume();
            return new IntLit(Integer.parseInt(n));
        } else if(isIdentifier(tkz.peek())) {
            String v = tkz.consume();
            return new Variable(v);
        } else if(tkz.peek().equals("(")) {
            tkz.consume("(");
            Expr e =  parseExpression();
            tkz.consume(")");
            return e;
        }
        return parseInfoExpression();
    }
    private Expr parseInfoExpression() throws CheckException, SyntaxError {
        if(tkz.peek("ally")) {
            tkz.consume();
            return new InfoExpr("ally", null);
        } else if(tkz.peek("opponent")) {
            tkz.consume();
            return new InfoExpr("opponent", null);
        } else if(tkz.peek("nearby")) {
            tkz.consume();
            return new InfoExpr("nearby", parseDirection());
        }
        throw new SyntaxError("Unknown info expression: " + tkz.peek());
    }
}