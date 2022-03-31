package tokenizer;

import lombok.Getter;
import org.agrona.AsciiSequenceView;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import uk.co.real_logic.artio.fields.DecimalFloat;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

public class Tokenizer {
    public static final boolean APPLY_CHECKS = true;
    private static final char[] SKIP = new char[]{' ', '\n', ':', ','};
    private final DirectBuffer buffer = new UnsafeBuffer();
    @Getter
    private final AsciiSequenceView string = new AsciiSequenceView();
    @Getter
    private final DecimalFloat decimalFloat = new DecimalFloat();
    /**
     * Offset of the next token to read
     */
    private int offset;
    private boolean bool;

    /**
     * @param actions   map of actions which should be taken when certain keys are encountered
     * @param structure to fill
     */
    public <T> void parseStruct(KeyMap<BiConsumer<Tokenizer, T>> actions, T structure) {
        Token token = next();
        Token.START_OBJECT.checkToken(token);
        while (true) {
            token = next();
            if (token == Token.END_OBJECT) {
                break;
            }
            Token.STRING.checkToken(token);
            var key = actions.getKey(getString());
            key.accept(this, structure);
        }
    }

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
        int stringStart = offset;
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
        long mantissa;
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
                decimalFloat.set(mantissa, 0);
                return Token.LONG;
            }
            if (next != '.') {
                throw new RuntimeException();
            }
            break;
        }
        // after dot
        int exponent = 0;
        while (offset < buffer.capacity() - 1) {
            char next = (char) buffer.getByte(offset++);
            if (isDigit(next)) {
                mantissa = mantissa * 10 + getLongFromChar(next);
                exponent++;
                continue;
            }
            if (shouldSkip(next)) {
                mantissa *= sign;
                decimalFloat.set(mantissa, exponent);
                return Token.FLOAT;
            }
            break;
        }
        throw new RuntimeException();
    }

    private static void parseNumber(AsciiSequenceView view, DecimalFloat number) {
        // universal and simple way of doing it is calling 'decimalFloat.fromString(string)', but we want to do it faster
        var buffer = view.buffer();
        int offset = view.offset();
        int end = offset + view.length();
        char first = (char) buffer.getByte(offset++);

        int sign;
        long mantissa;
        if (first == '-') {
            sign = -1;
            mantissa = 0;
        } else {
            sign = 1;
            mantissa = getLongFromChar(first);
        }
        // before dot
        while (offset < end) {
            char next = (char) buffer.getByte(offset++);
            if (isDigit(next)) {
                mantissa = mantissa * 10 + getLongFromChar(next);
                continue;
            }
            if (next == '.') {
                break;
            }
            throw new RuntimeException();
        }
        // after dot
        int exponent = 0;
        for (int i = offset; i < end; i++) {
            char next = (char) buffer.getByte(i);
            if (isDigit(next)) {
                mantissa = mantissa * 10 + getLongFromChar(next);
                exponent++;
            } else {
                throw new RuntimeException();
            }
        }
        // It is recommended to call number.set(sign * mantissa, exponent), but we want to do it faster
        number.value(sign * mantissa);
        number.scale(exponent);
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

    public long getLong() {
        return decimalFloat.value();
    }

    public boolean getBoolean() {
        return bool;
    }

    public DecimalFloat decimalFloatFromString() {
//        return decimalFloat.fromString(string);
        parseNumber(string, decimalFloat);
        return decimalFloat;
    }

    public double doubleFromString() {
        final DecimalFloat decimalFloat = decimalFloatFromString();
        return decimalFloat.toDouble();
    }
}
