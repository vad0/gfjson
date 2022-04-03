package de;

import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class ReadBench
{
    private final Tokenizer tokenizer = new Tokenizer();
    private final L2Update update = new L2Update();
    private String message;

    @Setup
    public void setup() throws IOException
    {
        message = readFile("big_increment.json");
    }

    static String readFile(final String fileName) throws IOException
    {
        final Path path = Paths.get("/home/vadim/IdeaProjects/gfjson/src/test/resources")
            .resolve(fileName);
        return Files.readString(path);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void parseIncrement()
    {
        tokenizer.wrap(message);
        IncrementParser.parseIncrement(tokenizer, update);
    }
}
