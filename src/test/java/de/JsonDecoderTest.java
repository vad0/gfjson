package de;

import org.junit.jupiter.api.Test;
import ser.JsonEncoder;
import uk.co.real_logic.artio.fields.DecimalFloat;
import utils.TestUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testParseFloatInvalid()
    {
        final String string = "{1.2";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        final var token = decoder.next();
        Token.START_OBJECT.checkToken(token);
        assertThrows(RuntimeException.class, decoder::next);
    }

    @Test
    public void testParseLongInvalid()
    {
        final String string = "{1a";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        final var token = decoder.next();
        Token.START_OBJECT.checkToken(token);
        assertThrows(RuntimeException.class, decoder::next);
    }

    @Test
    public void testParseInvalidStruct()
    {
        final String string = "{\"1\":#}";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        assertThrows(TokenException.class, () ->
        {
            for (int i = 0; i < 5; i++)
            {
                decoder.next();
            }
        });
    }

    @Test
    public void testFalse()
    {
        final String string = "false";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        final var token = decoder.next();
        Token.BOOLEAN.checkToken(token);
        assertFalse(decoder.getBoolean());
    }

    @Test
    public void testShortFalse()
    {
        final String string = "fals";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        assertThrows(TokenException.class, decoder::next);
    }

    @Test
    public void testShortTrue()
    {
        final String string = "tru";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        assertThrows(TokenException.class, decoder::next);
    }

    @Test
    public void testWrongFalse()
    {
        final String string = "{\"1\":fald}";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        assertThrows(TokenException.class, () ->
        {
            for (int i = 0; i < 5; i++)
            {
                decoder.next();
            }
        });
    }

    @Test
    public void testWrongTrue()
    {
        final String string = "{\"1\":trie}";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        assertThrows(TokenException.class, () ->
        {
            for (int i = 0; i < 5; i++)
            {
                decoder.next();
            }
        });
    }

    @Test
    public void testParseNumberString()
    {
        final String string = "\"1.2\"";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        final var token = decoder.next();
        Token.STRING.checkToken(token);
        final var parsed = decoder.doubleFromString();
        final var expected = 1.2;
        assertEquals(expected, parsed);
    }

    @Test
    public void testParseString()
    {
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final JsonEncoder encoder = new JsonEncoder();
        encoder.wrap(byteBuffer);
        encoder.putString("abc");

        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(byteBuffer);
        final var token = decoder.next();
        Token.STRING.checkToken(token);
        final var parsed = decoder.getString().toString();
        assertEquals("abc", parsed);
    }

    @Test
    public void testInvalidString()
    {
        final String string = "\"1.2";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        assertThrows(RuntimeException.class, decoder::next);
    }

    @Test
    public void testParseArray()
    {
        final String stringArray = "[1.2,2.3,3.4]";
        final var expected = List.of(1.2, 2.3, 3.4);
        checkDoubleArray(stringArray, expected);
    }

    @Test
    public void testParseArray2()
    {
        final String stringArray = "[0,2.3,5]";
        final List<Double> expected = List.of(0.0, 2.3, 5.0);
        checkDoubleArray(stringArray, expected);
    }

    private static void checkDoubleArray(
        final String stringArray,
        final List<Double> expected)
    {
        final ParseArrayElement<List<Double>> parseElement = (dec, struct, firstToken) ->
        {
            assert firstToken == Token.FLOAT || firstToken == Token.LONG;
            final double value = dec.getDecimalFloat().toDouble();
            struct.add(value);
        };
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(stringArray);
        final List<Double> toFill = new ArrayList<>();
        decoder.parseArray(toFill, parseElement);
        assertEquals(expected, toFill);
    }

    @Test
    public void testParseStruct()
    {
        final String string = "{\"a\":5,\"b\":[false,{\"e\":1}],\"c\":\"d\"}";
        final BiConsumer<JsonDecoder, Map<String, Object>> parseA = (dec, m) ->
        {
            final var token = dec.next();
            Token.LONG.checkToken(token);
            final long value = dec.getLong();
            m.put("a", value);
        };
        final BiConsumer<JsonDecoder, Map<String, Object>> parseC = (dec, m) ->
        {
            final var token = dec.next();
            Token.STRING.checkToken(token);
            final var value = dec.getString().toString();
            m.put("c", value);
        };
        final KeyMap<BiConsumer<JsonDecoder, Map<String, Object>>> keyMap = new KeyMap<>(
            Map.of("a", parseA, "c", parseC),
            JsonDecoder.skip());
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        final Map<String, Object> toFill = new HashMap<>();
        decoder.parseStruct(keyMap, toFill);
        final Map<String, Object> expected = Map.of(
            "a", 5L,
            "c", "d");
        assertEquals(expected, toFill);
    }
}