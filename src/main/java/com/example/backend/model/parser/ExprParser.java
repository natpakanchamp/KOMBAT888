package com.example.backend.model.parser;

import com.example.backend.model.ast.*;
import com.example.backend.model.exception.*;
import com.example.backend.model.factory.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExprParser implements Parser {
    private Tokenizer tkz;
    private ExprFactory eFact;
    private StatementFactory nFact;

    // คำสั่ง Action
    private static final Set<String> ACTION = Set.of("done", "move", "shoot");
    // ทิศทาง
    private static final Set<String> DIRECTION = Set.of("up", "down", "upleft", "upright", "downleft", "downright");
    // คำสงวน (ห้ามตั้งเป็นชื่อตัวแปร)
    private static final Set<String> RESERVED_WORDS = Set.of(
            "ally", "done", "down", "downleft", "downright", "else",
            "if", "move", "nearby", "opponent", "shoot", "then",
            "up", "upleft", "upright", "while"
    );

    public ExprParser(Tokenizer tkz) {
        this.tkz = tkz;
        eFact = ExprFactory.getInstance();
        nFact = StatementFactory.getInstance();
    }

    // Helper Methods
    public static boolean isDirection(String str) { return str != null && DIRECTION.contains(str); }
    public static boolean isNumeric(String str) { return str != null && str.matches("\\d+"); }
    public static boolean isIdentifier(String str) { return str != null && str.matches("[a-zA-Z][a-zA-Z0-9]*"); }

    @Override
    public Statement parse() throws CheckException, SyntaxError { return parseStrategy(); }

    // Strategy → Statement+
    private Statement parseStrategy() throws CheckException, SyntaxError {
        List<Statement> statements = new ArrayList<>();
        while (tkz.hasNextToken()) {
            statements.add(parseStatement());
        }
        if (statements.isEmpty()) throw new SyntaxError("Strategy requires at least one statement");
        return nFact.createBlockStatement(statements);
    }

    // Statement → Command | BlockStatement | IfStatement | WhileStatement
    private Statement parseStatement() throws CheckException, SyntaxError {
        if (tkz.peek("{")) return parseBlockStatement();
        else if (tkz.peek("if")) return parseIfStatement();
        else if (tkz.peek("while")) return parseWhileStatement();
        return parseCommand();
    }

    // Command → AssignmentStatement | ActionCommand
    private Statement parseCommand() throws CheckException, SyntaxError {
        if (!ACTION.contains(tkz.peek())) return parseAssignmentStatement();
        return parseActionCommand();
    }

    // AssignmentStatement → <identifier> = Expression
    private Statement parseAssignmentStatement() throws CheckException, SyntaxError {
        String identifier = tkz.consume();
        if (RESERVED_WORDS.contains(identifier)) {
            throw new SyntaxError("Cannot use reserved word '" + identifier + "' as variable name");
        }
        tkz.consume("=");
        Expr e = parseExpression();
        return nFact.createAssignmentStatement(identifier, e);
    }

    // ActionCommand → done | MoveCommand | AttackCommand
    private Statement parseActionCommand() throws CheckException, SyntaxError {
        if (tkz.peek("done")) {
            tkz.consume("done");
            return nFact.createDoneStatement();
        } else if (tkz.peek("move")) return parseMoveCommand();
        else if (tkz.peek("shoot")) return parseAttackCommand();
        throw new SyntaxError("Unknown action: " + tkz.peek());
    }

    // MoveCommand → move Direction
    private Statement parseMoveCommand() throws CheckException, SyntaxError {
        tkz.consume("move");
        String dir = parseDirection();
        return nFact.createMoveStatement(dir);
    }

    // AttackCommand → shoot Direction Expression
    private Statement parseAttackCommand() throws CheckException, SyntaxError {
        tkz.consume("shoot");
        String dir = parseDirection();
        Expr e = parseExpression();
        return nFact.createShootStatement(dir, e);
    }

    // Direction
    private String parseDirection() throws CheckException, SyntaxError {
        String dir = tkz.peek();
        if (isDirection(dir)) return tkz.consume();
        throw new SyntaxError("Invalid direction: " + dir);
    }

    // BlockStatement → { Statement* }
    private Statement parseBlockStatement() throws CheckException, SyntaxError {
        List<Statement> statements = new ArrayList<>();
        tkz.consume("{");
        while (tkz.hasNextToken() && !tkz.peek("}")) {
            statements.add(parseStatement());
        }
        tkz.consume("}");
        return nFact.createBlockStatement(statements);
    }

    // IfStatement → if ( Expression ) then Statement else Statement
    private Statement parseIfStatement() throws CheckException, SyntaxError {
        tkz.consume("if");
        tkz.consume("(");
        Expr e = parseExpression();
        tkz.consume(")");
        tkz.consume("then");
        Statement nThen = parseStatement();
        tkz.consume("else");
        Statement nElse = parseStatement();
        return nFact.createIfStatement(e, nThen, nElse);
    }

    // WhileStatement → while ( Expression ) Statement
    private Statement parseWhileStatement() throws CheckException, SyntaxError {
        tkz.consume("while");
        tkz.consume("(");
        Expr e = parseExpression();
        tkz.consume(")");
        Statement n = parseStatement();
        return nFact.createWhileStatement(e, n);
    }

    // --- Expression Parsing (Strict Grammar) ---

    // Expression → Expression + Term | Expression - Term | Term
    private Expr parseExpression() throws CheckException, SyntaxError {
        Expr t = parseTerm();
        while (tkz.peek("+") || tkz.peek("-")) {
            String op = tkz.consume();
            t = eFact.createBinaryArithExpr(t, op, parseTerm());
        }
        return t;
    }

    // Term → Term * Factor | Term / Factor | Term % Factor | Factor
    private Expr parseTerm() throws CheckException, SyntaxError {
        Expr f = parseFactor();
        while (tkz.peek("*") || tkz.peek("/") || tkz.peek("%")) {
            String op = tkz.consume();
            f = eFact.createBinaryArithExpr(f, op, parseFactor());
        }
        return f;
    }

    // Factor → Power ^ Factor | Power
    private Expr parseFactor() throws CheckException, SyntaxError {
        Expr p = parsePower();
        if (tkz.peek("^")) {
            tkz.consume();
            // [Fix] Right-associative: ต้องเรียก parseFactor (ตัวเอง) ซ้ำ
            p = eFact.createBinaryArithExpr(p, "^", parseFactor());
        }
        return p;
    }

    // Power → <number> | <identifier> | ( Expression ) | InfoExpression
    private Expr parsePower() throws CheckException, SyntaxError {
        if (isNumeric(tkz.peek())) {
            return eFact.createNumberLit(Long.parseLong(tkz.consume()));
        }
        // [Fix] ต้องเช็ค InfoExpression (nearby, ally, opponent) ก่อน Identifier!!
        else if (tkz.peek("nearby") || tkz.peek("ally") || tkz.peek("opponent")) {
            return parseInfoExpression();
        }
        else if (isIdentifier(tkz.peek())) {
            return eFact.createVariable(tkz.consume());
        }
        else if (tkz.peek("(")) {
            tkz.consume("(");
            Expr e = parseExpression();
            tkz.consume(")");
            return e;
        }
        throw new SyntaxError("Unexpected token in expression: " + tkz.peek());
    }

    // InfoExpression → ally | opponent | nearby Direction
    private Expr parseInfoExpression() throws CheckException, SyntaxError {
        if (tkz.peek("ally")) {
            tkz.consume();
            return eFact.createInfoExpr("ally", null);
        } else if (tkz.peek("opponent")) {
            tkz.consume();
            return eFact.createInfoExpr("opponent", null);
        } else if (tkz.peek("nearby")) {
            tkz.consume();
            // nearby กิน token ที่เป็น Direction เข้าไปด้วย
            return eFact.createInfoExpr("nearby", parseDirection());
        }
        throw new SyntaxError("Unknown info expression: " + tkz.peek());
    }
}
