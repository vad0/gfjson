package ser;

import org.agrona.AsciiSequenceView;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

import static ser.GfEncoderTest.encodeGfjson;

@State(Scope.Benchmark)
public class WriteGfJsonBench
{
    private final GfEncoder encoder = new GfEncoder();
    private final AsciiSequenceView string = new AsciiSequenceView();

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public int writeClear()
    {
        final int startOffset = encoder.getOffset();
        encodeGfjson(encoder);
        final int endOffset = encoder.getOffset();
        return endOffset - startOffset;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public String writeAllocate()
    {
        final int startOffset = encoder.getOffset();
        final int length = writeClear();
        encoder.readString(string, startOffset, startOffset + length);
        return string.toString();
    }
}
