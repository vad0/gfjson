package ser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.agrona.AsciiSequenceView;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

import static ser.JsonEncoderTest.encodeGfjson;

public class WriteDummy
{
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonEncoder ENCODER = new JsonEncoder();
    private static final AsciiSequenceView STRING = new AsciiSequenceView();

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public String jackson()
    {
        return JsonEncoderTest.jacksonEncode(MAPPER);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public int gfJsonClear()
    {
        final int startOffset = ENCODER.getOffset();
        encodeGfjson(ENCODER);
        final int endOffset = ENCODER.getOffset();
        return endOffset - startOffset;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public String gfJsonAllocate()
    {
        final int startOffset = ENCODER.getOffset();
        final int length = gfJsonClear();
        ENCODER.readString(STRING, startOffset, startOffset + length);
        return STRING.toString();
    }
}
