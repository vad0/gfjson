package de;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseIncrementTest
{
    @Test
    public void parseIncrement()
    {
        parseIncrement("increment.json");
    }

    @Test
    public void parseIncrementUnused()
    {
        parseIncrement("increment_unused.json");
    }

    private static void parseIncrement(final String fileName)
    {
        final String str = TestUtils.readFile(fileName);
        final var tokenizer = new JsonDecoder();
        tokenizer.wrap(str);

        final L2Update expected = new L2Update();
        expected.sides.getBid().addQuote(0.0024, 10);
        expected.sides.getAsk().addQuote(0.0026, 100);
        expected.timestamp = 123456789;

        final L2Update update = new L2Update();
        IncrementParser.parseIncrementGfJson(tokenizer, update);

        assertEquals(expected, update);
    }

    @Test
    public void parseIncrementJackson()
    {
        parseIncrementJackson("increment.json");
    }

    @Test
    public void parseIncrementUnusedJackson()
    {
        parseIncrementJackson("increment_unused.json");
    }

    @SneakyThrows
    private static void parseIncrementJackson(final String fileName)
    {
        final String str = TestUtils.readFile(fileName);

        final L2Update update = new L2Update();
        final JsonFactory jsonFactory = new JsonFactory();
        final JsonParser parser = jsonFactory.createParser(str);
        JacksonIncrementParser.parseIncrement(parser, update);

        final L2Update expected = new L2Update();
        expected.sides.getBid().addQuote(0.0024, 10);
        expected.sides.getAsk().addQuote(0.0026, 100);
        expected.timestamp = 123456789;

        assertEquals(expected, update);
    }

    @Test
    public void parseIncrementTree()
    {
        parseIncrementTree("increment.json");
    }

    @Test
    public void parseIncrementUnusedTree()
    {
        parseIncrementTree("increment_unused.json");
    }

    private static void parseIncrementTree(final String fileName)
    {
        final String str = TestUtils.readFile(fileName);

        final L2Update update = new L2Update();
        JacksonIncrementParser.parseIncrementTree(new ObjectMapper(), str, update);

        final L2Update expected = new L2Update();
        expected.sides.getBid().addQuote(0.0024, 10);
        expected.sides.getAsk().addQuote(0.0026, 100);
        expected.timestamp = 123456789;

        assertEquals(expected, update);
    }

    @SneakyThrows
    @Disabled
    @Test
    public void parseBigIncrement()
    {
        measureParseIncrement("big_increment.json");
    }

    @SneakyThrows
    @Disabled
    @Test
    public void parseSmallIncrement()
    {
        measureParseIncrement("increment.json");
    }

    @SneakyThrows
    public static void measureParseIncrement(final String fileName)
    {
        final String str = TestUtils.readFile(fileName);
        final var tokenizer = new JsonDecoder();

        final L2Update update = new L2Update();
        for (int i = 0; i < 1000_000_000; i++)
        {
            final long start = System.nanoTime();
            tokenizer.wrap(str);
            IncrementParser.parseIncrementGfJson(tokenizer, update);
            final long end = System.nanoTime();
            if (i % 100_000 == 0)
            {
                System.out.println(end - start);
                Thread.sleep(1);
            }
        }
    }
}
