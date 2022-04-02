package tokenizer;

import java.util.function.BiConsumer;

public class BinanceParser
{
    static final ParseArrayElement<L2Side> PARSE_QUOTE = BinanceParser::parseQuote;

    public static void parseQuote(final Tokenizer tokenizer, final L2Side side, final Token startToken)
    {
        Token.START_ARRAY.checkToken(startToken);

        Token token = tokenizer.next();
        Token.STRING.checkToken(token);
        final double price = tokenizer.doubleFromString();

        token = tokenizer.next();
        Token.STRING.checkToken(token);
        final double size = tokenizer.doubleFromString();

        side.addQuote(price, size);

        token = tokenizer.next();
        Token.END_ARRAY.checkToken(token);
    }

    public static void parseUpdate(
        final Tokenizer tokenizer,
        final L2Update update,
        final KeyMap<BiConsumer<Tokenizer, L2Update>> actions)
    {
        update.clear();
        tokenizer.parseStruct(actions, update);
    }

    public static void parseEventTime(final Tokenizer tokenizer, final L2Update update)
    {
        final Token token = tokenizer.next();
        Token.LONG.checkToken(token);
        update.timestamp = tokenizer.getLong();
    }

    static void parseBid(final Tokenizer t, final L2Update u)
    {
        t.parseArray(u.sides.getBid(), PARSE_QUOTE);
    }

    public static void parseAsk(final Tokenizer t, final L2Update u)
    {
        t.parseArray(u.sides.getAsk(), PARSE_QUOTE);
    }
}
