package benglang;

import static benglang.TokenType.*;

import java.util.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("nahole", ELSE);
        keywords.put("bhul", FALSE);
        keywords.put("jokhon", FOR);
        keywords.put("kormo", FUN);
        keywords.put("jodi", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("lekho", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("thik", TRUE);
        keywords.put("dhoro", VAR);
        keywords.put("jotokhon", WHILE);

    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while(!isAtEnd()) {
            // at the beginning of a lexeme
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '/':
                if(match('/')) {
                    // find if it goes till end of line, like a comment
                    while(peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;
            default:
            if(isDigit(c)) {
                number();
            } else if (isAlpha(c)) {
                identifier();
            } else {
                Main.error(line, "Unexpected character.");
            }
                break;
        }
    }

    private void identifier() {
        while(isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if(type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while(isDigit(peek())) advance();

        // account for fractional part
        if(peek()=='.' && isDigit(peekNext())) {
            advance();

            while(isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(
                    source.substring(start, current)));
    }

    private void string() {
        while(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()) {
            Main.error(line, "Unterminated string");
            return;
        }

        // for the closing "
        advance();

        //Trimming surrounding quotes
        String value = source.substring(start+1, current-1);
        addToken(STRING, value);
    }

    private boolean match(char expected) {
        if(isAtEnd())
            return false;
        if(source.charAt(current) != expected)
            return false;
        
        current++;
        return true;
    }

    private char peek() {
        if(isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if(current+1 >= source.length())
            return '\0';
        return source.charAt(current+1); 
    }

    private boolean isAlpha(char c) {
        return (c>='a' && c<='z') || 
               (c>='A' && c<='Z') ||
               c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c>='0' && c<='9';
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}
