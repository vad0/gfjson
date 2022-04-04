package ser;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.L2Update;
import de.Quote;
import org.agrona.AsciiSequenceView;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

import static ser.JsonEncoderTest.*;

public class WriteBigIncrement
{
    private static final JsonEncoder ENCODER = new JsonEncoder();
    private static final AsciiSequenceView STRING = new AsciiSequenceView();
    private static final Quote QUOTE = new Quote();
    private static final L2Update UPDATE = readBigIncrement();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public String jackson()
    {
        return encodeIncrementJackson(UPDATE, MAPPER, QUOTE);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public int gfJsonClear()
    {
        final int startOffset = ENCODER.getOffset();
        encodeIncrement(ENCODER, UPDATE, QUOTE);
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
