package de;

import org.agrona.AsciiSequenceView;
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
        final var decoder = new JsonDecoder();
        decoder.wrap(str);

        decoder.nextStartObject();
        checkString(decoder, "e");
        checkString(decoder, "depthUpdate");
        checkString(decoder, "E");
        checkLong(decoder, 123456789);
        checkString(decoder, "s");
        checkString(decoder, "BNBBTC");
        checkString(decoder, "U");
        checkLong(decoder, 157);
        checkString(decoder, "u");
        checkLong(decoder, 160);
        checkString(decoder, "b");
        decoder.nextStartArray();
        decoder.nextStartArray();
        checkString(decoder, "0.0024");
        checkString(decoder, "10");
        decoder.nextEndArray();
        decoder.nextEndArray();
        checkString(decoder, "a");
        decoder.nextStartArray();
        decoder.nextStartArray();
        checkString(decoder, "0.0026");
        checkString(decoder, "100");
        decoder.nextEndArray();
        decoder.nextEndArray();
        decoder.nextEndObject();
        checkEnd(decoder);
    }

    @Test
    public void parseCar()
    {
        final String str = TestUtils.readFile("car.json");
        final var decoder = new JsonDecoder();
        decoder.wrap(str);

        decoder.nextStartObject();
        checkString(decoder, "engine");
        decoder.nextStartObject();
        checkString(decoder, "cylinders");
        checkLong(decoder, 4);
        checkString(decoder, "horse power");
        checkFloat(decoder, 1234, 1);
        checkString(decoder, "volume");
        checkFloat(decoder, 2, 0);
        decoder.nextEndObject();
        checkString(decoder, "is electric");
        checkBoolean(decoder, false);
        checkString(decoder, "is petrol");
        checkBoolean(decoder, true);
        checkString(decoder, "min temperature");
        checkLong(decoder, -30);
        checkString(decoder, "max temperature");
        checkLong(decoder, 50);
        checkString(decoder, "license");
        checkNullableString(decoder, null);
        decoder.nextEndObject();
        checkEnd(decoder);
    }

    private static void checkEnd(final JsonDecoder jsonDecoder)
    {
        assertEquals(Token.END, jsonDecoder.next());
    }

    private static void checkLong(final JsonDecoder jsonDecoder, final long expected)
    {
        assertEquals(expected, jsonDecoder.nextLong());
    }

    private static void checkBoolean(final JsonDecoder jsonDecoder, final boolean expected)
    {
        assertEquals(expected, jsonDecoder.nextBoolean());
    }

    private static void checkFloat(
        final JsonDecoder jsonDecoder,
        final long expectedMantissa,
        final int expectedExponent)
    {
        final DecimalFloat decimalFloat = jsonDecoder.nextFloat();
        assertEquals(expectedMantissa, decimalFloat.value());
        assertEquals(expectedExponent, decimalFloat.scale());
    }

    private static void checkString(final JsonDecoder jsonDecoder, final String expected)
    {
        assertEquals(expected, jsonDecoder.nextString().toString());
    }

    private static void checkNullableString(final JsonDecoder jsonDecoder, final String expected)
    {
        final AsciiSequenceView string = jsonDecoder.nextNullableString();
        if (expected == null)
        {
            assertNull(string);
        }
        else
        {
            assertEquals(expected, string.toString());
        }
    }

    @Test
    public void testParseFloatInvalid()
    {
        final String string = "{1.2";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        decoder.nextStartObject();
        assertThrows(RuntimeException.class, decoder::next);
    }

    @Test
    public void testParseLongInvalid()
    {
        final String string = "{1a";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        decoder.nextStartObject();
        assertThrows(RuntimeException.class, decoder::next);
    }

    @Test
    public void testParseNegativeLong()
    {
        final String string = "[-5]";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        decoder.nextStartArray();
        checkLong(decoder, -5);
        decoder.nextEndArray();
    }

    @Test
    public void testParseNegativeFloat()
    {
        final String string = "[-5.100]";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        decoder.nextStartArray();
        checkFloat(decoder, -51, 1);
        decoder.nextEndArray();
    }

    @Test
    public void testParseNegativeFloatFromString()
    {
        final String string = "[\"-5.100\"]";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        decoder.nextStartArray();
        decoder.nextString();
        final DecimalFloat df = decoder.decimalFloatFromString();
        assertEquals(-51, df.value());
        assertEquals(1, df.scale());
        decoder.nextEndArray();
    }

    @Test
    public void testParseInvalidFloatFromString1()
    {
        final String string = "\"-5a.100\"";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        decoder.nextString();
        assertThrows(RuntimeException.class, decoder::decimalFloatFromString);
    }

    @Test
    public void testParseInvalidFloatFromString2()
    {
        final String string = "\"-5.10a0\"";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        decoder.nextString();
        assertThrows(RuntimeException.class, decoder::decimalFloatFromString);
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
        assertFalse(decoder.nextBoolean());
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
    public void testShortNull()
    {
        final String string = "nul";
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
    public void testWrongNull()
    {
        final String string = "{\"1\":nulll}";
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
    public void testWrongNull2()
    {
        final String string = "{\"1\":nul}";
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
        final var parsed = decoder.nextDoubleFromString();
        final var expected = 1.2;
        assertEquals(expected, parsed);
    }

    @Test
    public void testNullableString()
    {
        final String string = "{\"k\": \"1.2\", \"v\": null}";
        final JsonDecoder decoder = new JsonDecoder();
        decoder.wrap(string);
        decoder.nextStartObject();
        checkString(decoder, "k");
        checkNullableString(decoder, "1.2");
        checkString(decoder, "v");
        checkNullableString(decoder, null);
        decoder.nextEndObject();
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
        final var parsed = decoder.nextString().toString();
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
        final BiConsumer<JsonDecoder, Map<String, Object>> parseA = (dec, m) -> m.put("a", dec.nextLong());
        final BiConsumer<JsonDecoder, Map<String, Object>> parseC = (dec, m) -> m.put("c", dec.nextString().toString());
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

    @Test
    public void testCheckKeySuccess()
    {
        final var decoder = new JsonDecoder();
        decoder.wrap("{\"a\",1}");
        decoder.nextStartObject();
        decoder.checkKey(KeyMap.string2view("a"));
        assertEquals(1, decoder.nextLong());
        decoder.nextEndObject();
    }

    @Test
    public void escape()
    {
        final String str = TestUtils.readFile("escape.json");
        final var decoder = new JsonDecoder();
        decoder.wrap(str);
        decoder.nextStartObject();
        checkString(decoder, "topic");
        checkString(decoder, "md");
        checkString(decoder, "value");
        checkString(
            decoder,
            "{\"@timestamp\":1652429935.521021,\"log\":\"{\\\"t\\\":\\\"i2\\\",\\\"o\\\":1652429935519264064}");
        checkString(decoder, "offset");
        checkLong(decoder, 13015);
        decoder.nextEndObject();
    }

    @Test
    public void escapeWrong()
    {
        final String str = TestUtils.readFile("escape_wrong.json");
        final var decoder = new JsonDecoder();
        decoder.wrap(str);
        decoder.nextStartObject();
        checkString(decoder, "value");
        assertThrows(RuntimeException.class, decoder::next);
    }

    @Test
    public void nested()
    {
        final String str = TestUtils.readFile("nested.json");
        final var decoder = new JsonDecoder();
        decoder.wrap(str);
        decoder.nextStartObject();
        checkString(decoder, "value");
        checkString(decoder, "{\"log\":1652429935519264064}");

        final AsciiSequenceView string = decoder.getString();
        {
            final var subDecoder = new JsonDecoder();
            subDecoder.wrap(string);
            subDecoder.nextStartObject();
            checkString(subDecoder, "log");
            checkLong(subDecoder, 1652429935519264064L);
            subDecoder.nextEndObject();
        }

        decoder.nextEndObject();
    }
}