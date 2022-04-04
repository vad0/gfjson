package de;

import org.openjdk.jmh.annotations.*;
import utils.Utils;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class ReadBench
{
    private final JsonDecoder jsonDecoder = new JsonDecoder();
    private final L2Update update = new L2Update();
    private final String message = Utils.readFile("big_increment.json");

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void parseIncrement()
    {
        jsonDecoder.wrap(message);
        IncrementParser.parseIncrement(jsonDecoder, update);
    }
}
