package ser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.*;
import org.agrona.AsciiSequenceView;
import org.agrona.collections.MutableLong;
import org.agrona.collections.MutableReference;
import org.junit.jupiter.api.Test;
import utils.TestUtils;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonEncoderTest
{
    private static final List<String> PARENTS = List.of("Sergey", "Anna");
    private static final BiConsumer<JsonEncoder, String> PUT_STRING = JsonEncoder::putString;
    private static final ByteBuffer BYTE_BUFFER = ByteBuffer.allocateDirect(1 << 20);
    private static final FillElement<L2Side, Quote> FILL_QUOTE = (side, index, quote) -> side.getQuote(quote, index);
    private static final BiConsumer<JsonEncoder, Quote> ENCODE_QUOTE = JsonEncoderTest::encodeQuote;
    private static final String DEPTH_UPDATE = "depthUpdate";
    private static final String BNBBTC = "BNBBTC";
    private static final String BIG_INCREMENT_JSON = "big_increment.json";
    private static final String INCREMENT_JSON = "increment.json";

    @Test
    public void testWrite()
    {
        final String jackson = jacksonEncode(new ObjectMapper());
        check(jackson, JsonEncoderTest::encodeGfjson);
    }

    @Test
    public void manyWrites()
    {
        final var encoder = new JsonEncoder();
        final AsciiSequenceView gfjson = new AsciiSequenceView();
        for (int i = 0; i < 100; i++)
        {
            final int startOffset = encoder.getOffset();
            encodeGfjson(encoder);
            final int endOffset = encoder.getOffset();
            encoder.readString(gfjson, startOffset, endOffset);
        }
    }

    public static void encodeGfjson(final JsonEncoder encoder)
    {
        encoder.wrap(BYTE_BUFFER);

        encoder.startObject();

        encoder.putKey("worker");
        encoder.putString("Ivan");

        encoder.nextKey("age");
        encoder.putLong(25);

        encoder.nextKey("weight");
        encoder.putDouble(75.4);

        encoder.nextKey("is male");
        encoder.putBoolean(true);

        encoder.nextKey("is married");
        encoder.putBoolean(false);

        encoder.nextKey("pets");
        encoder.encodeArray(PUT_STRING, List.of());

        encoder.nextKey("parents");
        encoder.encodeArray(PUT_STRING, PARENTS);

        encoder.endObject();
    }

    public static String jacksonEncode(final ObjectMapper mapper)
    {
        final ObjectNode node = mapper.createObjectNode();
        node.put("worker", "Ivan");
        node.put("age", 25);
        node.put("weight", 75.4);
        node.put("is male", true);
        node.put("is married", false);
        node.putArray("pets");
        final var array = node.putArray("parents");
        array.add("Sergey").add("Anna");
        return node.toString();
    }

    public static L2Update readBigIncrement()
    {
        return readIncrement(BIG_INCREMENT_JSON);
    }

    public static L2Update readSmallIncrement()
    {
        return readIncrement("increment.json");
    }

    public static L2Update readIncrement(final String fileName)
    {
        final String increment = TestUtils.readFile(fileName);
        final JsonDecoder jsonDecoder = new JsonDecoder();
        final L2Update update = new L2Update();
        jsonDecoder.wrap(increment);
        IncrementParser.parseIncrementGfJson(jsonDecoder, update);
        return update;
    }

    @Test
    public void testFalse()
    {
        final Consumer<JsonEncoder> encode = e -> e.putBoolean(false);
        check("false", encode);
    }

    @Test
    public void testTrue()
    {
        final Consumer<JsonEncoder> encode = e -> e.putBoolean(true);
        check("true", encode);
    }

    @Test
    public void testLong()
    {
        final long value = -123456;
        check(String.valueOf(value), e -> e.putLong(value));
    }

    @Test
    public void testEncodeString()
    {
        final Consumer<JsonEncoder> encode = e -> e.putDoubleAsString(1.1);
        check("\"1.1\"", encode);
    }

    @Test
    public void testEncodeEmptyArrayList()
    {
        final Consumer<JsonEncoder> encode = e -> e.encodeArray(TestUtils.doNothing(), List.of());
        check("[]", encode);
    }

    @Test
    public void testEncodeArrayList()
    {
        final Consumer<JsonEncoder> encode = e -> e.encodeArray((enc, i) -> enc.putLong(i), List.of(1, 2));
        check("[1,2]", encode);
    }

    @Test
    public void testEncodeEmptyArray2()
    {
        final MutableReference<String> reference = new MutableReference<>();
        final FillElement<List<MutableReference<String>>, MutableReference<String>> fill = (a, i, e) ->
        {
        };
        final Consumer<JsonEncoder> encode = e -> e.encodeArray(List.of(), 0, reference, fill, TestUtils.doNothing());
        check("[]", encode);
    }

    @Test
    public void testEncodeArrayList2()
    {
        final MutableLong reference = new MutableLong();
        final FillElement<List<Long>, MutableLong> fill = (a, i, e) -> e.set(a.get(i));
        final BiConsumer<JsonEncoder, MutableLong> encodeElement = (enc, ml) -> enc.putLong(ml.get());
        final List<Long> list = List.of(1L, 2L);
        final Consumer<JsonEncoder> encode = e -> e.encodeArray(
            list,
            list.size(),
            reference,
            fill,
            encodeElement);
        check("[1,2]", encode);
    }

    private static void check(final String expected, final Consumer<JsonEncoder> encode)
    {
        final JsonEncoder encoder = new JsonEncoder();
        final AsciiSequenceView string = new AsciiSequenceView();
        encoder.wrap(BYTE_BUFFER);
        final int startOffset = encoder.getOffset();
        encode.accept(encoder);
        final int endOffset = encoder.getOffset();
        encoder.readString(string, startOffset, endOffset);
        final String actual = string.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void testEncodeIncrementGfjson()
    {
        final L2Update update = readSmallIncrement();
        final String increment = TestUtils.readFile(INCREMENT_JSON);
        final Quote quote = new Quote();
        final String expected = increment
            .replace("\n", "")
            .replace(" ", "");
        check(expected, e -> encodeIncrement(e, update, quote));
    }

    @Test
    public void testEncodeIncrementJackson()
    {
        final L2Update update = readSmallIncrement();
        final ObjectMapper mapper = new ObjectMapper();
        final Quote quote = new Quote();
        final String increment = TestUtils.readFile(INCREMENT_JSON);
        final String expected = increment
            .replace("\n", "")
            .replace(" ", "");
        final String string = encodeIncrementJackson(update, mapper, quote);
        assertEquals(expected, string);
    }

    public static String encodeIncrementJackson(final L2Update update, final ObjectMapper mapper, final Quote quote)
    {
        final var objectNode = mapper.createObjectNode();
        objectNode.put(IncrementParser.EVENT_TYPE, DEPTH_UPDATE);
        objectNode.put(IncrementParser.EVENT_TIME, update.timestamp);
        objectNode.put(IncrementParser.SYMBOL, BNBBTC);
        objectNode.put(IncrementParser.FIRST_UPDATE_ID, 157);
        objectNode.put(IncrementParser.LAST_UPDATE_ID, 160);
        fillSideJackson(mapper, quote, objectNode.putArray(IncrementParser.BIDS), update.sides.getBid());
        fillSideJackson(mapper, quote, objectNode.putArray(IncrementParser.ASKS), update.sides.getAsk());
        return objectNode.toString();
    }

    private static void fillSideJackson(
        final ObjectMapper mapper,
        final Quote quote,
        final com.fasterxml.jackson.databind.node.ArrayNode bids,
        final L2Side side)
    {
        for (int i = 0; i < side.size(); i++)
        {
            side.getQuote(quote, i);
            final var quoteNode = mapper.createArrayNode();
            quoteNode.add(double2string(quote.price));
            quoteNode.add(double2string(quote.size));
            bids.add(quoteNode);
        }
    }

    private static String double2string(final double value)
    {
        final int intValue = (int)value;
        if (intValue == value)
        {
            return String.valueOf(intValue);
        }
        return String.valueOf(value);
    }

    public static void encodeIncrement(final JsonEncoder encoder, final L2Update update, final Quote quote)
    {
        encoder.wrap(BYTE_BUFFER);
        encoder.startObject();

        encoder.putKey(IncrementParser.EVENT_TYPE);
        encoder.putString(DEPTH_UPDATE);

        encoder.nextKey(IncrementParser.EVENT_TIME);
        encoder.putLong(update.timestamp);

        encoder.nextKey(IncrementParser.SYMBOL);
        encoder.putString(BNBBTC);

        encoder.nextKey(IncrementParser.FIRST_UPDATE_ID);
        encoder.putLong(157);

        encoder.nextKey(IncrementParser.LAST_UPDATE_ID);
        encoder.putLong(160);

        encoder.nextKey(IncrementParser.BIDS);
        encodeSide(encoder, update.sides.getBid(), quote);

        encoder.nextKey(IncrementParser.ASKS);
        encodeSide(encoder, update.sides.getAsk(), quote);

        encoder.endObject();
    }

    private static void encodeSide(final JsonEncoder encoder, final L2Side side, final Quote quote)
    {
        encoder.encodeArray(side, side.size(), quote, FILL_QUOTE, ENCODE_QUOTE);
    }

    private static void encodeQuote(final JsonEncoder encoder, final Quote quote)
    {
        encoder.startArray();
        encoder.putDoubleAsString(quote.price);
        encoder.putComma();
        encoder.putDoubleAsString(quote.size);
        encoder.endArray();
    }

    @Test
    public void testNextLine()
    {
        check("{\n}", encoder ->
        {
            encoder.startObject();
            encoder.nextLine();
            encoder.endObject();
        });
    }

    @Test
    public void testDecimalFloat()
    {
        check("1234.56", encoder ->
        {
            final var df = encoder.getDecimalFloat();
            df.set(123456, 2);
            encoder.putDecimalFloat(df);
        });
    }

    @Test
    public void putNaN()
    {
        check("NaN", encoder ->
        {
            final var df = encoder.getDecimalFloat();
            df.fromDouble(Double.NaN);
            encoder.putDecimalFloat(df);
        });
    }

    @Test
    public void putPositiveInfinity()
    {
        assertThrows(NumberFormatException.class, () ->
        {
            final var encoder = new JsonEncoder();
            encoder.putDouble(Double.POSITIVE_INFINITY);
        });
    }

    @Test
    public void putNegativeInfinity()
    {
        assertThrows(NumberFormatException.class, () ->
        {
            final var encoder = new JsonEncoder();
            encoder.putDouble(Double.NEGATIVE_INFINITY);
        });
    }
}