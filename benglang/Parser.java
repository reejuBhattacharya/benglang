package benglang;

import static benglang.TokenType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

class Parser {

    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Expr expression() {
        return assignment();
    }

    private Stmt declaration() {
        try {
            if(match(VAR))  return varDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if(match(FOR)) return forStatement();
        if(match(IF)) return ifStatement();
        if(match(PRINT)) return printStatement();
        if(match(WHILE)) return whileStatement();
        if(match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "'jokhon' er pore '(' thaka dorkar");

        Stmt initializer;
        if(match(SEMICOLON)) 
            initializer = null;
        else if(match(VAR))
            initializer = varDeclaration();
        else 
            initializer = expressionStatement();
        

        Expr condition = null;
        if(!check(SEMICOLON)) 
            condition = expression();
        
        consume(SEMICOLON, "loop condition er age ';' thaka dorkar");

        Expr increment = null;
        if(!check(RIGHT_PAREN)) 
            increment = expression();
        
        consume(RIGHT_PAREN, "'for' er sheshe ')' thaka dorkar");
        Stmt body = statement();

        if(increment != null) {
            body = new Stmt.Block(
                Arrays.asList(
                    body, 
                    new Stmt.Expression(increment)
                )
            );
        }

        if(condition == null)
            condition = new Expr.Literal(true);
        
        body = new Stmt.While(condition, body);

        if(initializer != null)
            body = new Stmt.Block(Arrays.asList(initializer, body));
        

        return body;
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "'jotokhon' er por '(' dorkar.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "condition er por ')' dorkar");
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "jodi r por '(' dorkar.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "condition er por ')' dorkar.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE))
            elseBranch = statement();
        

        return new Stmt.If(condition, thenBranch, elseBranch);
    }


    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "';' ta nei");
        return new Stmt.Print(value);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Variable er naam nei");

        Expr initializer = null;
        if(match(EQUAL))
            initializer = expression();
        
        consume(SEMICOLON, "variable lekhar por ';' dorkar.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "';' ta nei");
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "block er sheshe '}' dorkar.");
        return statements;
    }

    private Expr assignment() {
        Expr expr = or();

        if(match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if(expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "bhul bhabe assign korcho");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while(match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while(match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while(match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while(match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while(match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if(match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if(!check(RIGHT_PAREN)) {
            do {
                if(arguments.size() >= 255) {
                    error(peek(), "eto gulo argument cholbe na");
                }
                arguments.add(expression());
            } while(match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, 
                                "argument er pore ')' dorkar.");
        
        return new Expr.Call(callee, paren, arguments);
    }

    private Expr call() {
        Expr expr = primary();

        while(true) {
            if(match(LEFT_PAREN)) 
                expr = finishCall(expr);
            else
                break;
        }

        return expr;
    }

    private Expr primary() {
        if(match(FALSE))
            return new Expr.Literal(false);
        if(match(TRUE)) 
            return new Expr.Literal(true);
        if(match(NIL)) 
            return new Expr.Literal(null);

        if(match(NUMBER, STRING))
            return new Expr.Literal(previous().literal);

        if(match(IDENTIFIER))
            return new Expr.Variable(previous());

        if(match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "')' ta nei");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "expression ta nei");
    }

    private boolean match(TokenType... types) {
        for(TokenType type: types) {
            if(check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if(check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if(isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if(!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token previous() {
        return tokens.get(current-1);
    }

    private Token peek() {
        return tokens.get(current);
    }

    private ParseError error(Token token, String message) {
        Main.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while(!isAtEnd()) {
            if(previous().type == SEMICOLON)
                return;
            
            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

}
