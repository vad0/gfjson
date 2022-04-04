package ser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.*;
import org.agrona.AsciiSequenceView;
import org.junit.jupiter.api.Test;
import utils.Utils;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonEncoderTest
{
    private static final List<String> PARENTS = List.of("Sergey", "Anna");
    private static final BiConsumer<JsonEncoder, String> PUT_STRING = JsonEncoder::putString;
    private static final ByteBuffer BYTE_BUFFER = ByteBuffer.allocateDirect(1 << 20);
    private static final FillElement<L2Side, Quote> FILL_QUOTE = (side, index, quote) -> side.getQuote(quote, index);
    private static final BiConsumer<JsonEncoder, Quote> ENCODE_QUOTE = JsonEncoderTest::encodeQuote;
    private static final String DEPTH_UPDATE = "depthUpdate";
    private static final String BNBBTC = "BNBBTC";

    @Test
    public void testWrite()
    {
        final ObjectMapper mapper = new ObjectMapper();
        final String jackson = jacksonEncode(mapper);

        final var encoder = new JsonEncoder();
        final int startOffset = encoder.getOffset();
        encodeGfjson(encoder);
        final int endOffset = encoder.getOffset();
        final AsciiSequenceView gfjson = new AsciiSequenceView();
        encoder.readString(gfjson, startOffset, endOffset);

        assertEquals(gfjson.toString(), jackson);
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
        final var array = node.putArray("parents");
        array.add("Sergey").add("Anna");
        return node.toString();
    }

    public static L2Update readBigIncrement()
    {
        return readIncrement("big_increment.json");
    }

    public static L2Update readIncrement(final String fileName)
    {
        final String increment = Utils.readFile(fileName);
        final JsonDecoder jsonDecoder = new JsonDecoder();
        final L2Update update = new L2Update();
        jsonDecoder.wrap(increment);
        IncrementParser.parseIncrement(jsonDecoder, update);
        return update;
    }

    @Test
    public void testEncodeIncrementGfjson()
    {
        final L2Update update = readBigIncrement();

        final Quote quote = new Quote();
        final JsonEncoder encoder = new JsonEncoder();
        final int startOffset = encoder.getOffset();
        encodeIncrement(encoder, update, quote);
        final int endOffset = encoder.getOffset();

        final AsciiSequenceView string = new AsciiSequenceView();
        encoder.readString(string, startOffset, endOffset);
    }

    @Test
    public void testEncodeIncrementJackson()
    {
        final L2Update update = readBigIncrement();
        final ObjectMapper mapper = new ObjectMapper();
        final Quote quote = new Quote();

        final String string = encodeIncrementJackson(update, mapper, quote);
    }

    public static String encodeIncrementJackson(final L2Update update, final ObjectMapper mapper, final Quote quote)
    {
        final var objectNode = mapper.createObjectNode();
        objectNode.put(IncrementParser.EVENT_TYPE, DEPTH_UPDATE);
        objectNode.put(IncrementParser.SYMBOL, BNBBTC);
        objectNode.put(IncrementParser.FIRST_UPDATE_ID, 157);
        objectNode.put(IncrementParser.LAST_UPDATE_ID, 160);
        objectNode.put(IncrementParser.EVENT_TIME, update.timestamp);
        fillSideJackson(mapper, quote, objectNode.putArray(IncrementParser.BIDS), update.sides.getBid());
        fillSideJackson(mapper, quote, objectNode.putArray(IncrementParser.ASKS), update.sides.getAsk());
        final String string = objectNode.toString();
        return string;
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
            quoteNode.add(String.valueOf(quote.price));
            quoteNode.add(String.valueOf(quote.size));
            bids.add(quoteNode);
        }
    }

    public static void encodeIncrement(final JsonEncoder encoder, final L2Update update, final Quote quote)
    {
        encoder.wrap(BYTE_BUFFER);
        encoder.startObject();

        encoder.putKey(IncrementParser.EVENT_TYPE);
        encoder.putString(DEPTH_UPDATE);

        encoder.nextKey(IncrementParser.SYMBOL);
        encoder.putString(BNBBTC);

        encoder.nextKey(IncrementParser.FIRST_UPDATE_ID);
        encoder.putLong(157);

        encoder.nextKey(IncrementParser.LAST_UPDATE_ID);
        encoder.putLong(160);

        encoder.nextKey(IncrementParser.EVENT_TIME);
        encoder.putLong(update.timestamp);

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
}