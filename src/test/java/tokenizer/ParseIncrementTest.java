package tokenizer;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseIncrementTest {
    @Test
    public void parseIncrement() {
        parseIncrement("increment.json");
    }

    @Test
    public void parseIncrementUnused() {
        parseIncrement("increment_unused.json");
    }

    private static void parseIncrement(String fileName) {
        String str = TokenizerTest.readFile(fileName);
        var tokenizer = new Tokenizer();
        tokenizer.wrap(str);

        final L2Update expected = new L2Update();
        expected.sides.getBid().addQuote(0.0024, 10);
        expected.sides.getAsk().addQuote(0.0026, 100);
        expected.timestamp = 123456789;

        final L2Update update = new L2Update();
        IncrementParser.parseIncrement(tokenizer, update);

        assertEquals(expected, update);
    }

    @SneakyThrows
    @Disabled
    @Test
    public void parseBigIncrement() {
        measureParseIncrement("big_increment.json");
    }

    @SneakyThrows
    @Disabled
    @Test
    public void parseSmallIncrement() {
        measureParseIncrement("increment.json");
    }

    @SneakyThrows
    public void measureParseIncrement(final String fileName) {
        String str = TokenizerTest.readFile(fileName);
        var tokenizer = new Tokenizer();

        final L2Update update = new L2Update();
        for (int i = 0; i < 1000_000_000; i++) {
            final long start = System.nanoTime();
            tokenizer.wrap(str);
            IncrementParser.parseIncrement(tokenizer, update);
            final long end = System.nanoTime();
            if (i % 100_000 == 0) {
                System.out.println(end - start);
                Thread.sleep(1);
            }
        }
    }
}
