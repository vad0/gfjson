import lombok.SneakyThrows;
import org.agrona.AsciiSequenceView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseIncrementTest {
    @Test
    public void parseIncrement() {
        String str = TokenizerTest.readFile("increment.json");
        var tokenizer = new Tokenizer();
        tokenizer.wrap(str);

        final L2Update expected = new L2Update();
        expected.sides.getBid().addQuote(0.0024, 10);
        expected.sides.getAsk().addQuote(0.0026, 100);
        expected.timestamp = 123456789;

        final L2Update update = new L2Update();
        parseIncrement(tokenizer, update);

        assertEquals(expected, update);
    }

    @SneakyThrows
    @Test
    public void parseBigIncrement() {
        String str = TokenizerTest.readFile("big_increment.json");
        var tokenizer = new Tokenizer();

        final L2Update update = new L2Update();
        for (int i = 0; i < 1000_000; i++) {
            final long start = System.nanoTime();
            tokenizer.wrap(str);
            parseIncrement(tokenizer, update);
            final long end = System.nanoTime();
            if (i % 100 == 0) {
                System.out.println(end - start);
                if (i % 1_000 == 0) {
                    Thread.sleep(1);
                }
            }
        }
    }

    private static void parseIncrement(Tokenizer tokenizer, L2Update increment) {
        increment.clear();
        Token token = tokenizer.next();
        checkToken(token, Token.START_OBJECT);

        boolean isTimestampRead = false;
        boolean isBidRead = false;
        boolean isAskRead = false;

        while (true) {
            token = tokenizer.next();
            if (token == Token.END_OBJECT) {
                break;
            }
            checkToken(token, Token.STRING);
            Field key = getKey(tokenizer.getString());
            switch (key) {
                case EVENT_TIME:
                    token = tokenizer.next();
                    checkToken(token, Token.LONG);
                    increment.timestamp = tokenizer.getLong();
                    isTimestampRead = true;
                    break;
                case BID:
                    parseArray(tokenizer, increment.sides.getBid());
                    isBidRead = true;
                    break;
                case ASK:
                    parseArray(tokenizer, increment.sides.getAsk());
                    isAskRead = true;
                    break;
                default:
                    // ignore value
                    tokenizer.next();
            }
        }

        if (isTimestampRead && isBidRead && isAskRead) {
            return;
        }
        throw new RuntimeException();
    }

    private static void parseArray(Tokenizer tokenizer, L2Side side) {
        Token token = tokenizer.next();
        checkToken(token, Token.START_ARRAY);

        token = tokenizer.next();
        while (token != Token.END_ARRAY) {
            // we are in quote now
            token = parseQuote(tokenizer, side, token);
        }
    }

    private static Token parseQuote(Tokenizer tokenizer, L2Side side, Token startToken) {
        checkToken(startToken, Token.START_ARRAY);

        Token token = tokenizer.next();
        checkToken(token, Token.STRING);
        double price = tokenizer.doubleFromString();

        token = tokenizer.next();
        checkToken(token, Token.STRING);
        double size = tokenizer.doubleFromString();

        side.addQuote(price, size);

        token = tokenizer.next();
        checkToken(token, Token.END_ARRAY);
        return tokenizer.next();
    }

    private static Field getKey(AsciiSequenceView string) {
        assert string.length() == 1 : string;
        switch (string.charAt(0)) {
            case 'E':
                return Field.EVENT_TIME;
            case 'b':
                return Field.BID;
            case 'a':
                return Field.ASK;
            default:
                return Field.IGNORE;
        }
    }

    private static void checkToken(Token token, final Token expected) {
        assert token == expected : token;
    }

    enum Field {
        EVENT_TIME,
        BID,
        ASK,
        IGNORE
    }
}
