package de;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseSnapshotTest
{
    @Test
    public void parseSnapshot()
    {
        final String str = Utils.readFile("snapshot.json");
        final var tokenizer = new JsonDecoder();
        tokenizer.wrap(str);

        final L2Update expected = new L2Update();
        expected.sides.getBid()
            .addQuote(0.00216090, 22.67000000)
            .addQuote(0.00216080, 29.43000000);
        expected.sides.getAsk()
            .addQuote(0.00216100, 6.87000000)
            .addQuote(0.00216150, 15.24000000);
        expected.timestamp = 441791238;

        final L2Update update = new L2Update();
        SnapshotParser.parseSnapshot(tokenizer, update);

        assertEquals(expected, update);
    }

    @Test
    @Disabled
    public void parseBigSnapshot()
    {
        measureParseSnapshot("big_snapshot.json");
    }

    @Test
    @Disabled
    public void parseSmallSnapshot()
    {
        measureParseSnapshot("snapshot.json");
    }

    @SneakyThrows
    public static void measureParseSnapshot(final String fileName)
    {
        final String str = Utils.readFile(fileName);
        final var tokenizer = new JsonDecoder();

        final L2Update update = new L2Update();
        for (int i = 0; i < 1000_000_000; i++)
        {
            final long start = System.nanoTime();
            tokenizer.wrap(str);
            SnapshotParser.parseSnapshot(tokenizer, update);
            final long end = System.nanoTime();
            if (i % 10_000 == 0)
            {
                System.out.println(end - start);
                Thread.sleep(1);
            }
        }
    }
}
