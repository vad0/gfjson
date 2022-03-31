package tokenizer;

import java.util.function.BiConsumer;

public class BinanceParser {
    static final ParseArrayElement<L2Side> PARSE_QUOTE = BinanceParser::parseQuote;

    public static void parseQuote(Tokenizer tokenizer, L2Side side, Token startToken) {
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
    }

    public static void parseUpdate(Tokenizer tokenizer, L2Update update, KeyMap<BiConsumer<Tokenizer, L2Update>> actions) {
        update.clear();
        tokenizer.parseStruct(actions, update);
    }

    public static void parseEventTime(Tokenizer tokenizer, L2Update update) {
        Token token = tokenizer.next();
        Token.LONG.checkToken(token);
        update.timestamp = tokenizer.getLong();
    }

    static void parseBid(Tokenizer t, L2Update u) {
        t.parseArray(u.sides.getBid(), PARSE_QUOTE);
    }

    public static void parseAsk(Tokenizer t, L2Update u) {
        t.parseArray(u.sides.getAsk(), PARSE_QUOTE);
    }
}
