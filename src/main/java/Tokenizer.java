import lombok.Getter;
import org.agrona.AsciiSequenceView;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

public class Tokenizer {
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
    private boolean bool;

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
            if (next == '-' || isDigit(next)) {
                return parseNumber(next);
            }
            if (next == 'f') {
                checkFalse();
                bool = false;
                return Token.BOOLEAN;
            }
            if (next == 't') {
                checkTrue();
                bool = true;
                return Token.BOOLEAN;
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
                    throw new TokenException(offset - 1, next);
            }
        }
        return Token.END;
    }

    private void checkTrue() {
        if (offset + 3 > buffer.capacity()) {
            throw new TokenException("Not enough capacity to fit 'true'");
        }
        if ((char) buffer.getByte(offset++) != 'r' ||
                (char) buffer.getByte(offset++) != 'u' ||
                (char) buffer.getByte(offset++) != 'e') {
            throw new TokenException("Can't parse 'true'");
        }
    }

    private void checkFalse() {
        if (offset + 4 > buffer.capacity()) {
            throw new TokenException("Not enough capacity to fit 'false'");
        }
        if ((char) buffer.getByte(offset++) != 'a' ||
                (char) buffer.getByte(offset++) != 'l' ||
                (char) buffer.getByte(offset++) != 's' ||
                (char) buffer.getByte(offset++) != 'e') {
            throw new TokenException("Can't parse 'false'");
        }
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

    private Token parseNumber(char first) {
        int sign;
        if (first == '-') {
            sign = -1;
            mantissa = 0;
        } else {
            sign = 1;
            mantissa = getLongFromChar(first);
        }
        // before dot
        while (offset < buffer.capacity() - 1) {
            char next = (char) buffer.getByte(offset++);
            if (isDigit(next)) {
                mantissa = mantissa * 10 + getLongFromChar(next);
                continue;
            }
            if (shouldSkip(next)) {
                // this is not a float, so just return long
                mantissa *= sign;
                return Token.LONG;
            }
            if (next != '.') {
                throw new RuntimeException();
            }
            break;
        }
        // after dot
        exponent = 0;
        while (offset < buffer.capacity() - 1) {
            char next = (char) buffer.getByte(offset++);
            if (isDigit(next)) {
                mantissa = mantissa * 10 + getLongFromChar(next);
                exponent--;
                continue;
            }
            if (shouldSkip(next)) {
                mantissa *= sign;
                return Token.FLOAT;
            }
            break;
        }
        throw new RuntimeException();
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
        return '0' <= next && next <= '9';
    }

    private static long getLongFromChar(final char c) {
        return c - '0';
    }

    public AsciiSequenceView getString() {
        return string.wrap(buffer, stringStart, offset - stringStart - 1);
    }

    public long getLong() {
        return mantissa;
    }

    public boolean getBoolean() {
        return bool;
    }
}
