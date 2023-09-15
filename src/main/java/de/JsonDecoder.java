package de;

import lombok.Getter;
import lombok.SneakyThrows;
import org.agrona.AsciiSequenceView;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.apache.commons.collections4.trie.AsciiKeyAnalyser;
import uk.co.real_logic.artio.fields.DecimalFloat;
import uk.co.real_logic.artio.fields.ReadOnlyDecimalFloat;
import uk.co.real_logic.artio.util.MutableAsciiBuffer;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.function.BiConsumer;

public class JsonDecoder
{
    // By default, checks are applied, but they can be switched off by providing a system property
    public static final boolean APPLY_CHECKS = !Boolean.getBoolean("omit_checks");
    private static final char[] SKIP = new char[]{' ', '\n', ':', ','};
    private static final BiConsumer<JsonDecoder, ?> SKIP_LAMBDA = (t, u) -> t.skipValue();
    private static final long MAX_MANTISSA = DecimalFloat.MAX_VALUE.value() / 10;
    private final DirectBuffer buffer = new UnsafeBuffer();
    private final MutableAsciiBuffer stringBuffer = new MutableAsciiBuffer();
    @Getter
    private final AsciiSequenceView string = new AsciiSequenceView();
    @Getter
    private final DecimalFloat decimalFloat = new DecimalFloat();
    /**
     * Offset of the next token to read
     */
    private int offset;
    private int length;
    private boolean bool;

    /**
     * Parse array of uniform values, e.g. quotes. This method is not suitable if e.g. first element of array is price
     * and second is size.
     */
    public <T> void parseArray(final T structure, final ParseArrayElement<T> parseArrayElement)
    {
        nextStartArray();
        var token = next();
        while (token != Token.END_ARRAY)
        {
            parseArrayElement.parseElement(this, structure, token);
            token = next();
        }
    }

    /**
     * This method parses struct from including initial '{'
     *
     * @param actions   map of actions which should be taken when certain keys are encountered
     * @param structure to fill
     */
    public <T> void parseStruct(final KeyMap<BiConsumer<JsonDecoder, T>> actions, final T structure)
    {
        nextStartObject();
        finishParsingStruct(actions, structure);
    }

    /**
     * This method can be used to continue parsing struct. E.g. when first field contains information about message
     * type. So we should decide on the parsing logic after parsing the first field.
     *
     * @param actions   map of actions which should be taken when certain keys are encountered
     * @param structure to fill
     */
    public <T> void finishParsingStruct(final KeyMap<BiConsumer<JsonDecoder, T>> actions, final T structure)
    {
        while (true)
        {
            final Token token = next();
            if (token == Token.END_OBJECT)
            {
                break;
            }
            Token.STRING.checkToken(token);
            final var key = actions.getKey(getString());
            key.accept(this, structure);
        }
    }

    /**
     * This method parses struct from including initial '{'
     *
     * @param actions map of actions which should be taken when certain keys are encountered
     */
    public void parseStructRunnables(final KeyMap<Runnable> actions)
    {
        nextStartObject();
        finishParsingStructRunnables(actions);
    }

    /**
     * This method can be used to continue parsing struct. E.g. when first field contains information about message
     * type. So we should decide on the parsing logic after parsing the first field.
     *
     * @param actions map of actions which should be taken when certain keys are encountered
     */
    public void finishParsingStructRunnables(final KeyMap<Runnable> actions)
    {
        while (true)
        {
            final Token token = next();
            if (token == Token.END_OBJECT)
            {
                break;
            }
            Token.STRING.checkToken(token);
            final var key = actions.getKey(getString());
            key.run();
        }
    }

    public static <T> BiConsumer<JsonDecoder, T> skip()
    {
        return (BiConsumer<JsonDecoder, T>)SKIP_LAMBDA;
    }

    /**
     * This method skips structure of any complexity
     */
    public void skipValue()
    {
        int startArrayCount = 0;
        int endArrayCount = 0;
        int startObjectCount = 0;
        int endObjectCount = 0;
        do
        {
            final Token token = next();
            switch (token)
            {
                case START_ARRAY:
                    startArrayCount++;
                    break;
                case END_ARRAY:
                    endArrayCount++;
                    break;
                case START_OBJECT:
                    startObjectCount++;
                    break;
                case END_OBJECT:
                    endObjectCount++;
                    break;
            }
        }
        while (startArrayCount > endArrayCount || startObjectCount > endObjectCount);
    }

    public void wrap(final ByteBuffer byteBuffer)
    {
        offset = 0;
        length = byteBuffer.capacity();
        buffer.wrap(byteBuffer);
    }

    public void wrap(final byte[] string)
    {
        offset = 0;
        length = string.length;
        buffer.wrap(string);
    }

    public void wrap(final String string)
    {
        wrap(string.getBytes());
    }

    public void wrap(final AsciiSequenceView string)
    {
        offset = 0;
        length = string.length();
        buffer.wrap(string.buffer(), string.offset(), string.length());
    }

    public Token next()
    {
        while (offset < length)
        {
            final char next = nextChar();
            if (shouldSkip(next))
            {
                continue;
            }
            if (next == '-' || isDigit(next))
            {
                return parseNumber(next);
            }
            switch (next)
            {
                case '"':
                    parseString();
                    return Token.STRING;
                case 'f':
                    checkFalse();
                    bool = false;
                    return Token.BOOLEAN;
                case 't':
                    checkTrue();
                    bool = true;
                    return Token.BOOLEAN;
                case 'n':
                    checkNull();
                    return Token.NULL;
                case 'N':
                    checkNaN();
                    decimalFloat.set(ReadOnlyDecimalFloat.NAN);
                    return Token.FLOAT;
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

    private void checkTrue()
    {
        if (offset + 3 > length)
        {
            throw new TokenException("Not enough capacity to fit 'true'");
        }
        if (nextChar() != 'r' ||
            nextChar() != 'u' ||
            nextChar() != 'e')
        {
            throw new TokenException("Can't parse 'true'");
        }
    }

    private void checkNull()
    {
        if (offset + 3 > length)
        {
            throw new TokenException("Not enough capacity to fit 'null'");
        }
        if (nextChar() != 'u' ||
            nextChar() != 'l' ||
            nextChar() != 'l')
        {
            throw new TokenException("Can't parse 'null'");
        }
    }

    private void checkNaN()
    {
        if (offset + 2 > length)
        {
            throw new TokenException("Not enough capacity to fit 'NaN'");
        }
        if (nextChar() != 'a' ||
            nextChar() != 'N')
        {
            throw new TokenException("Can't parse 'NaN'");
        }
    }

    private void checkFalse()
    {
        if (offset + 4 > length)
        {
            throw new TokenException("Not enough capacity to fit 'false'");
        }
        if (nextChar() != 'a' ||
            nextChar() != 'l' ||
            nextChar() != 's' ||
            nextChar() != 'e')
        {
            throw new TokenException("Can't parse 'false'");
        }
    }

    private char nextChar()
    {
        return (char)buffer.getByte(offset++);
    }

    private void parseString()
    {
        final int stringStart = offset;
        final int maxStringSize = buffer.capacity() - stringStart;
        if (stringBuffer.capacity() < maxStringSize)
        {
            stringBuffer.wrap(new byte[maxStringSize]);
        }
        int stringOffset = 0;
        boolean isEscaped = false;
        while (offset < length)
        {
            final char next = nextChar();
            if (isEscaped)
            {
                isEscaped = false;
            }
            else
            {
                if (next == '"')
                {
                    string.wrap(stringBuffer, 0, stringOffset);
                    return;
                }
                if (next == '\\')
                {
                    isEscaped = true;
                    continue;
                }
            }
            stringBuffer.putChar(stringOffset++, next);
        }
        throw new RuntimeException();
    }

    private Token parseNumber(final char first)
    {
        final int sign;
        long mantissa;
        if (first == '-')
        {
            sign = -1;
            mantissa = 0;
        }
        else
        {
            sign = 1;
            mantissa = getDigit(first);
        }
        // before dot
        while (offset < length)
        {
            final char next = nextChar();
            if (isDigit(next))
            {
                mantissa = mantissa * 10 + getDigit(next);
                continue;
            }
            if (shouldSkip(next))
            {
                // this is not a float, so just return long
                decimalFloat.value(sign * mantissa);
                decimalFloat.scale(0);
                return Token.LONG;
            }
            if (next == '.')
            {
                break;
            }
            if (isEndOfStructure(next))
            {
                offset--;
                decimalFloat.value(sign * mantissa);
                decimalFloat.scale(0);
                return Token.LONG;
            }
            throw new RuntimeException();
        }
        // after dot
        int exponent = 0;
        while (offset < length)
        {
            final char next = nextChar();
            if (isDigit(next))
            {
                if (mantissa <= MAX_MANTISSA)
                {
                    mantissa = mantissa * 10 + getDigit(next);
                    exponent++;
                }
                continue;
            }
            if (shouldSkip(next))
            {
                mantissa *= sign;
                decimalFloat.set(mantissa, exponent);
                return Token.FLOAT;
            }
            if (isEndOfStructure(next))
            {
                offset--;
                mantissa *= sign;
                decimalFloat.set(mantissa, exponent);
                return Token.FLOAT;
            }
            break;
        }
        throw new RuntimeException();
    }

    private static boolean isEndOfStructure(final char ch)
    {
        return ch == ']' || ch == '}';
    }

    @SneakyThrows
    private static void parseNumber(final AsciiSequenceView view, final DecimalFloat number)
    {
        // universal and simple way of doing it is calling 'decimalFloat.fromString(string)', but we want to do it
        // faster
        final int length = view.length();
        if (length == 0)
        {
            throw new ParseException("Empty string", 0);
        }
        final var buffer = view.buffer();
        int offset = view.offset();
        final int end = offset + length;
        final char first = (char)buffer.getByte(offset++);

        final int sign;
        long mantissa;
        if (first == '-')
        {
            sign = -1;
            mantissa = 0;
        }
        else
        {
            sign = 1;
            mantissa = getDigit(first);
        }
        // before dot
        while (offset < end)
        {
            final char next = (char)buffer.getByte(offset++);
            if (isDigit(next))
            {
                if (mantissa > MAX_MANTISSA)
                {
                    throw new ArithmeticException(view.toString());
                }
                mantissa = mantissa * 10 + getDigit(next);
                continue;
            }
            if (next == '.')
            {
                break;
            }
            throw new RuntimeException();
        }
        // after dot
        int exponent = 0;
        for (int i = offset; i < end; i++)
        {
            final char next = (char)buffer.getByte(i);
            if (isDigit(next))
            {
                if (mantissa <= MAX_MANTISSA)
                {
                    mantissa = mantissa * 10 + getDigit(next);
                    exponent++;
                }
            }
            else
            {
                throw new RuntimeException();
            }
        }
        number.set(sign * mantissa, exponent);
    }

    private static boolean shouldSkip(final char next)
    {
        for (final char c : SKIP)
        {
            if (c == next)
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isDigit(final char next)
    {
        return '0' <= next && next <= '9';
    }

    public static int getDigit(final char c)
    {
        return c - '0';
    }

    public long getLong()
    {
        return decimalFloat.value();
    }

    public long nextLong()
    {
        Token.LONG.checkToken(next());
        return getLong();
    }

    public DecimalFloat nextFloat()
    {
        next().checkNumber();
        return getDecimalFloat();
    }

    public void nextStartObject()
    {
        Token.START_OBJECT.checkToken(next());
    }

    public void nextStartArray()
    {
        Token.START_ARRAY.checkToken(next());
    }

    public void nextEndObject()
    {
        Token.END_OBJECT.checkToken(next());
    }

    public void nextEndArray()
    {
        Token.END_ARRAY.checkToken(next());
    }

    public boolean getBoolean()
    {
        return bool;
    }

    public boolean nextBoolean()
    {
        Token.BOOLEAN.checkToken(next());
        return getBoolean();
    }

    public AsciiSequenceView nextString()
    {
        Token.STRING.checkToken(next());
        return getString();
    }

    public AsciiSequenceView nextNullableString()
    {
        final Token token = next();
        if (token == Token.NULL)
        {
            return null;
        }
        Token.STRING.checkToken(token);
        return getString();
    }

    public DecimalFloat decimalFloatFromString()
    {
        parseNumber(string, decimalFloat);
        return decimalFloat;
    }

    public double doubleFromString()
    {
        final DecimalFloat decimalFloat = decimalFloatFromString();
        return decimalFloat.toDouble();
    }

    public long longFromString()
    {
        final DecimalFloat decimalFloat = decimalFloatFromString();
        assert decimalFloat.scale() == 0;
        return decimalFloat.value();
    }

    public double nextDoubleFromString()
    {
        nextString();
        return doubleFromString();
    }

    public long nextLongFromString()
    {
        nextString();
        return longFromString();
    }

    public static void checkString(final AsciiSequenceView expected, final AsciiSequenceView actual)
    {
        assert stringEquals(expected, actual) : actual;
    }

    public static boolean stringEquals(final AsciiSequenceView a, final AsciiSequenceView b)
    {
        return AsciiKeyAnalyser.INSTANCE.compare(a, b) == 0;
    }

    public void checkKey(final AsciiSequenceView expected)
    {
        checkString(expected, nextString());
    }

    public double nextOptionalDoubleFromString(final double defaultValue)
    {
        final var str = nextString();
        if (str.isEmpty())
        {
            return defaultValue;
        }
        return doubleFromString();
    }

    public long nextOptionalLongFromString(final long defaultValue)
    {
        final var str = nextString();
        if (str.isEmpty())
        {
            return defaultValue;
        }
        return longFromString();
    }
}
