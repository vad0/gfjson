import lombok.Getter;
import org.agrona.AsciiSequenceView;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

public class Tokenizer {
    private static final int NOT_INITIALIZED = -1;
    private static final char[] SKIP = new char[]{' ', '\n', ':', ','};
    private final DirectBuffer buffer = new UnsafeBuffer();
    private final AsciiSequenceView string = new AsciiSequenceView();
    /**
     * Offset of the next token to read
     */
    private int offset;
    private int stringStart;
    @Getter
    private long mantissa;
    @Getter
    private int exponent;

    public void wrap(ByteBuffer byteBuffer) {
        buffer.wrap(byteBuffer);
        offset = 0;
    }

    public void wrap(String string) {
        buffer.wrap(string.getBytes());
        offset = 0;
    }

    public Token next() {
        while (offset < buffer.capacity()) {
            char next = (char) buffer.getByte(offset++);
            if (next == '"') {
                parseString();
                return Token.STRING;
            }
            if (shouldSkip(next)) {
                continue;
            }
            if (isDigit(next)) {
                return parseNumber(next);
            }
            switch (next) {
                case '{':
                    return Token.START_OBJECT;
                case '}':
                    return Token.END_OBJECT;
                case '[':
                    return Token.START_ARRAY;
                case ']':
                    return Token.END_ARRAY;
                default:
                    throw new RuntimeException("Unexpected char: '" + next + "'");
            }
        }
        return Token.END;
    }

    private static boolean shouldSkip(char next) {
        for (char c : SKIP) {
            if (c == next) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDigit(char next) {
        return '0' <= next && '9' >= next;
    }

    private Token parseNumber(char first) {
        mantissa = getLongFromChar(first);
        exponent = 0;
        // before dot
        while (offset < buffer.capacity() - 1) {
            char next = (char) buffer.getByte(offset++);
            if (isDigit(next)) {
                mantissa = mantissa * 10 + getLongFromChar(next);
                continue;
            }
            if (shouldSkip(next)) {
                // this is not a float, so just return long
                return Token.LONG;
            }
        }
        throw new RuntimeException();
    }

    private static long getLongFromChar(final char c) {
        return c - '0';
    }

    private void parseString() {
        stringStart = offset;
        while (offset < buffer.capacity() - 1) {
            char next = (char) buffer.getByte(offset++);
            if (next == '"') {
                string.wrap(buffer, stringStart, offset - stringStart - 1);
                return;
            }
        }
        throw new RuntimeException();
    }

    public AsciiSequenceView getString() {
        return string.wrap(buffer, stringStart, offset - stringStart - 1);
    }

    public long getLong() {
        return mantissa;
    }
}
