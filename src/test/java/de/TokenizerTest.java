package de;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import uk.co.real_logic.artio.fields.DecimalFloat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenizerTest
{
    @Test
    public void parseIncrement()
    {
        final String str = readFile("increment.json");
        final var tokenizer = new Tokenizer();
        tokenizer.wrap(str);

        checkStartObject(tokenizer);
        checkString(tokenizer, "e");
        checkString(tokenizer, "depthUpdate");
        checkString(tokenizer, "E");
        checkLong(tokenizer, 123456789);
        checkString(tokenizer, "s");
        checkString(tokenizer, "BNBBTC");
        checkString(tokenizer, "U");
        checkLong(tokenizer, 157);
        checkString(tokenizer, "u");
        checkLong(tokenizer, 160);
        checkString(tokenizer, "b");
        checkStartArray(tokenizer);
        checkStartArray(tokenizer);
        checkString(tokenizer, "0.0024");
        checkString(tokenizer, "10");
        checkEndArray(tokenizer);
        checkEndArray(tokenizer);
        checkString(tokenizer, "a");
        checkStartArray(tokenizer);
        checkStartArray(tokenizer);
        checkString(tokenizer, "0.0026");
        checkString(tokenizer, "100");
        checkEndArray(tokenizer);
        checkEndArray(tokenizer);
        checkEndObject(tokenizer);
        checkEnd(tokenizer);
    }

    @Test
    public void parseCar()
    {
        final String str = readFile("car.json");
        final var tokenizer = new Tokenizer();
        tokenizer.wrap(str);

        checkStartObject(tokenizer);
        checkString(tokenizer, "engine");
        checkStartObject(tokenizer);
        checkString(tokenizer, "cylinders");
        checkLong(tokenizer, 4);
        checkString(tokenizer, "horse power");
        checkFloat(tokenizer, 1234, 1);
        checkEndObject(tokenizer);
        checkString(tokenizer, "is electric");
        checkBoolean(tokenizer, false);
        checkString(tokenizer, "is petrol");
        checkBoolean(tokenizer, true);
        checkString(tokenizer, "min temperature");
        checkLong(tokenizer, -30);
        checkString(tokenizer, "max temperature");
        checkLong(tokenizer, 50);
        checkEndObject(tokenizer);
        checkEnd(tokenizer);
    }

    private static void checkStartArray(final Tokenizer tokenizer)
    {
        assertEquals(Token.START_ARRAY, tokenizer.next());
    }

    private static void checkEnd(final Tokenizer tokenizer)
    {
        assertEquals(Token.END, tokenizer.next());
    }

    private static void checkEndObject(final Tokenizer tokenizer)
    {
        assertEquals(Token.END_OBJECT, tokenizer.next());
    }

    private static void checkEndArray(final Tokenizer tokenizer)
    {
        assertEquals(Token.END_ARRAY, tokenizer.next());
    }

    private static void checkStartObject(final Tokenizer tokenizer)
    {
        assertEquals(Token.START_OBJECT, tokenizer.next());
    }

    @SneakyThrows
    static String readFile(final String fileName)
    {
        final Path relative = Paths.get("src", "test", "resources", fileName);
        final Path absolute = relative.toAbsolutePath();
        return Files.readString(absolute);
    }

    private static void checkLong(final Tokenizer tokenizer, final int expected)
    {
        assertEquals(Token.LONG, tokenizer.next());
        assertEquals(expected, tokenizer.getLong());
    }

    private static void checkBoolean(final Tokenizer tokenizer, final boolean expected)
    {
        assertEquals(Token.BOOLEAN, tokenizer.next());
        assertEquals(expected, tokenizer.getBoolean());
    }

    private static void checkFloat(final Tokenizer tokenizer, final long expectedMantissa, final int expectedExponent)
    {
        assertEquals(Token.FLOAT, tokenizer.next());
        final DecimalFloat decimalFloat = tokenizer.getDecimalFloat();
        assertEquals(expectedMantissa, decimalFloat.value());
        assertEquals(expectedExponent, decimalFloat.scale());
    }

    private static void checkString(final Tokenizer tokenizer, final String expected)
    {
        assertEquals(Token.STRING, tokenizer.next());
        assertEquals(expected, tokenizer.getString().toString());
    }
}