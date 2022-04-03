package ser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.agrona.AsciiSequenceView;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GfEncoderTest
{
    private static final List<String> PARENTS = List.of("Sergey", "Anna");
    private static final BiConsumer<GfEncoder, String> PUT_STRING = GfEncoder::putString;
    private static final ByteBuffer BYTE_BUFFER = ByteBuffer.allocateDirect(1024);

    @Test
    public void testWrite()
    {
        final ObjectMapper mapper = new ObjectMapper();
        final String jackson = jacksonEncode(mapper);

        final var encoder = new GfEncoder();
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
        final var encoder = new GfEncoder();
        final AsciiSequenceView gfjson = new AsciiSequenceView();
        for (int i = 0; i < 100; i++)
        {
            final int startOffset = encoder.getOffset();
            encodeGfjson(encoder);
            final int endOffset = encoder.getOffset();
            encoder.readString(gfjson, startOffset, endOffset);
        }
    }

    public static void encodeGfjson(final GfEncoder encoder)
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
}