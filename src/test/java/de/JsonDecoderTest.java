package de;

import org.junit.jupiter.api.Test;
import uk.co.real_logic.artio.fields.DecimalFloat;
import utils.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonDecoderTest
{
    @Test
    public void parseIncrement()
    {
        final String str = TestUtils.readFile("increment.json");
        final var tokenizer = new JsonDecoder();
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
        final String str = TestUtils.readFile("car.json");
        final var tokenizer = new JsonDecoder();
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

    private static void checkStartArray(final JsonDecoder jsonDecoder)
    {
        assertEquals(Token.START_ARRAY, jsonDecoder.next());
    }

    private static void checkEnd(final JsonDecoder jsonDecoder)
    {
        assertEquals(Token.END, jsonDecoder.next());
    }

    private static void checkEndObject(final JsonDecoder jsonDecoder)
    {
        assertEquals(Token.END_OBJECT, jsonDecoder.next());
    }

    private static void checkEndArray(final JsonDecoder jsonDecoder)
    {
        assertEquals(Token.END_ARRAY, jsonDecoder.next());
    }

    private static void checkStartObject(final JsonDecoder jsonDecoder)
    {
        assertEquals(Token.START_OBJECT, jsonDecoder.next());
    }

    private static void checkLong(final JsonDecoder jsonDecoder, final int expected)
    {
        assertEquals(Token.LONG, jsonDecoder.next());
        assertEquals(expected, jsonDecoder.getLong());
    }

    private static void checkBoolean(final JsonDecoder jsonDecoder, final boolean expected)
    {
        assertEquals(Token.BOOLEAN, jsonDecoder.next());
        assertEquals(expected, jsonDecoder.getBoolean());
    }

    private static void checkFloat(
        final JsonDecoder jsonDecoder,
        final long expectedMantissa,
        final int expectedExponent)
    {
        assertEquals(Token.FLOAT, jsonDecoder.next());
        final DecimalFloat decimalFloat = jsonDecoder.getDecimalFloat();
        assertEquals(expectedMantissa, decimalFloat.value());
        assertEquals(expectedExponent, decimalFloat.scale());
    }

    private static void checkString(final JsonDecoder jsonDecoder, final String expected)
    {
        assertEquals(Token.STRING, jsonDecoder.next());
        assertEquals(expected, jsonDecoder.getString().toString());
    }
}