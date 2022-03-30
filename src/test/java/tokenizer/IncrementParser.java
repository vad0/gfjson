package tokenizer;

import org.agrona.AsciiSequenceView;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.apache.commons.collections4.trie.AsciiTrie;

public class IncrementParser {
    private static final AsciiTrie<Field> TRIE = new AsciiTrie<>();

    static {
        TRIE.put(string2view("E"), Field.EVENT_TIME);
        TRIE.put(string2view("b"), Field.BID);
        TRIE.put(string2view("a"), Field.ASK);
    }

    public static AsciiSequenceView string2view(final String string) {
        final DirectBuffer buffer = new UnsafeBuffer();
        final byte[] bytes = string.getBytes();
        buffer.wrap(bytes);
        final AsciiSequenceView result = new AsciiSequenceView();
        result.wrap(buffer, 0, bytes.length);
        return result;
    }

    static Field getKey(AsciiSequenceView string) {
        return TRIE.getOrDefault(string, Field.IGNORE);
    }

    static Field getKey1(AsciiSequenceView string) {
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

    static void parseArray(Tokenizer tokenizer, L2Side side) {
        Token token = tokenizer.next();
        Token.START_ARRAY.checkToken(token);

        token = tokenizer.next();
        while (token != Token.END_ARRAY) {
            // we are in quote now
            token = parseQuote(tokenizer, side, token);
        }
    }

    private static Token parseQuote(Tokenizer tokenizer, L2Side side, Token startToken) {
        Token.START_ARRAY.checkToken(startToken);

        Token token = tokenizer.next();
        Token.STRING.checkToken(token);
        double price = tokenizer.doubleFromString();

        token = tokenizer.next();
        Token.STRING.checkToken(token);
        double size = tokenizer.doubleFromString();

        side.addQuote(price, size);

        token = tokenizer.next();
        Token.END_ARRAY.checkToken(token);
        return tokenizer.next();
    }

    public static void parseIncrement(Tokenizer tokenizer, L2Update increment) {
        increment.clear();
        Token token = tokenizer.next();
        Token.START_OBJECT.checkToken(token);

        boolean isTimestampRead = false;
        boolean isBidRead = false;
        boolean isAskRead = false;

        while (true) {
            token = tokenizer.next();
            if (token == Token.END_OBJECT) {
                break;
            }
            Token.STRING.checkToken(token);
            Field key = getKey(tokenizer.getString());
            switch (key) {
                case EVENT_TIME:
                    token = tokenizer.next();
                    Token.LONG.checkToken(token);
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

    enum Field {
        EVENT_TIME,
        BID,
        ASK,
        IGNORE
    }
}
