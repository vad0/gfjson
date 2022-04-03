package ser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class WriteJacksonBench
{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public String write()
    {
        return GfEncoderTest.jacksonEncode(MAPPER);
    }
}
