import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ExprTokenizer implements Tokenizer {
    private String src, next;  private int pos;
    private static final Set<String> RESERVED_WORDS = Set.of(
            "ally", "done", "down", "downleft", "downright", "else",
            "if", "move", "nearby", "opponent", "shoot", "then",
            "up", "upleft", "upright", "while"
    );
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "(?<NUMBER>\\d+)|(?<IDENTIFIER>[a-zA-Z][a-zA-Z0-9]*)|(?<OPERATOR>[+\\-*/%^=])|(?<DELIMITER>[(){}])"
    );
    public ExprTokenizer(String src) {
        this.src = src;  pos = 0;
        computeNext();
    }
    @Override
    public boolean hasNextToken()
    { return next != null; }
    public void checkNextToken() {
        if (!hasNextToken()) throw new
                NoSuchElementException("no more tokens");
    }
    @Override
    public String peek() {
        checkNextToken();
        return next;
    }
    @Override
    public String consume() {
        checkNextToken();
        String result = next;
        computeNext();
        return result;
    }
    private void computeNext() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
        if (pos == src.length()) {
            next = null;
            return;
        }
        Matcher matcher = TOKEN_PATTERN.matcher(src);
        matcher.region(pos, src.length()); // กำหนดขอบเขตการหา
        if (matcher.lookingAt()) {
            String tokenText = matcher.group();
            if (matcher.group("IDENTIFIER") != null) {
                // ถ้าเป็นคำ ให้เช็คว่าเป็น Reserved Word หรือไม่
                if (RESERVED_WORDS.contains(tokenText)) {
                    next = tokenText; // เป็นคำสั่ง เช่น move, if
                } else {
                    next = tokenText; // เป็นชื่อตัวแปรทั่วไป
                }
            } else {
                // เป็น NUMBER, OPERATOR หรือ DELIMITER
                next = tokenText;
            }
            pos = matcher.end(); // เลื่อนตำแหน่ง pos ไปยังจุดสิ้นสุดของ Token ที่พบ
        } else {
            // 5. กรณีเจอตัวอักษรที่ไม่อยู่ในนิยาม
            throw new RuntimeException("Lexical Error: Unknown character at " + pos + ": " + src.charAt(pos));
        }
    }
    /** Returns true if
     *  the next token (if any) is s. */
    public boolean peek(String s) {
        if(!hasNextToken()) return false;
        return peek().equals(s);
    }
    public void consume(String s)
            throws CheckException {
        if(peek(s)) consume();
        else throw new CheckException(s + " expected");
    }
}
