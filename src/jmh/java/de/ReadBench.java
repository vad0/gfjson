package de;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.*;
import utils.TestUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class ReadBench
{
    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final JsonDecoder jsonDecoder = new JsonDecoder();
    private final L2Update update = new L2Update();
    private final String message = TestUtils.readFile("big_increment.json");

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void parseIncrement()
    {
        jsonDecoder.wrap(message);
        IncrementParser.parseIncrementGfJson(jsonDecoder, update);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void parseIncrementJackson() throws IOException
    {
        final JsonParser parser = JSON_FACTORY.createParser(message);
        JacksonIncrementParser.parseIncrement(parser, update);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void parseIncrementTree()
    {
        JacksonIncrementParser.parseIncrementTree(MAPPER, message, update);
    }
}
