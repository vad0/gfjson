package de;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.SneakyThrows;

import java.util.function.BiConsumer;

public class BinanceParser
{
    static final ParseArrayElement<L2Side> PARSE_QUOTE = BinanceParser::parseQuote;

    public static void parseQuote(final JsonDecoder jsonDecoder, final L2Side side, final Token startToken)
    {
        Token.START_ARRAY.checkToken(startToken);
        final double price = jsonDecoder.nextDoubleFromString();
        final double size = jsonDecoder.nextDoubleFromString();
        side.addQuote(price, size);
        jsonDecoder.nextEndArray();
    }

    public static void parseUpdate(
        final JsonDecoder jsonDecoder,
        final L2Update update,
        final KeyMap<BiConsumer<JsonDecoder, L2Update>> actions)
    {
        update.clear();
        jsonDecoder.parseStruct(actions, update);
    }

    @SneakyThrows
    public static void parseEventTimeJackson(final JsonParser parser, final L2Update update)
    {
        final var token = parser.nextToken();
        JacksonUtils.checkToken(JsonToken.VALUE_NUMBER_INT, token);
        update.timestamp = parser.getLongValue();
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
