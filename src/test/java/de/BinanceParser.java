package de;

import java.util.function.BiConsumer;

public class BinanceParser
{
    static final ParseArrayElement<L2Side> PARSE_QUOTE = BinanceParser::parseQuote;

    public static void parseQuote(final JsonDecoder jsonDecoder, final L2Side side, final Token startToken)
    {
        Token.START_ARRAY.checkToken(startToken);

        Token token = jsonDecoder.next();
        Token.STRING.checkToken(token);
        final double price = jsonDecoder.doubleFromString();

        token = jsonDecoder.next();
        Token.STRING.checkToken(token);
        final double size = jsonDecoder.doubleFromString();

        side.addQuote(price, size);

        token = jsonDecoder.next();
        Token.END_ARRAY.checkToken(token);
    }

    public static void parseUpdate(
        final JsonDecoder jsonDecoder,
        final L2Update update,
        final KeyMap<BiConsumer<JsonDecoder, L2Update>> actions)
    {
        update.clear();
        jsonDecoder.parseStruct(actions, update);
    }

    public static void parseEventTime(final JsonDecoder jsonDecoder, final L2Update update)
    {
        final Token token = jsonDecoder.next();
        Token.LONG.checkToken(token);
        update.timestamp = jsonDecoder.getLong();
    }

    static void parseBid(final JsonDecoder t, final L2Update u)
    {
        t.parseArray(u.sides.getBid(), PARSE_QUOTE);
    }

    public static void parseAsk(final JsonDecoder t, final L2Update u)
    {
        t.parseArray(u.sides.getAsk(), PARSE_QUOTE);
    }
}
