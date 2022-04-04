package de;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.SneakyThrows;

import java.util.Map;
import java.util.function.BiConsumer;

import static de.IncrementParser.*;
import static de.JacksonUtils.parseArray;

public class JacksonIncrementParser
{
    static final JacksonUtils.JacksonParseArrayElement<L2Side> PARSE_QUOTE = JacksonIncrementParser::parseQuote;
    private static final BiConsumer<JsonParser, ?> SKIP_LAMBDA = (parser, u) -> JacksonUtils.skipValue(parser);
    private static final JacksonKeyMap<BiConsumer<JsonParser, L2Update>> ACTIONS = initKeyMapJackson();

    @SneakyThrows
    private static <T> BiConsumer<JsonParser, L2Update> skip()
    {
        return (BiConsumer<JsonParser, L2Update>)SKIP_LAMBDA;
    }

    private static JacksonKeyMap<BiConsumer<JsonParser, L2Update>> initKeyMapJackson()
    {
        final Map<String, BiConsumer<JsonParser, L2Update>> actions = Map.of(
            EVENT_TIME, BinanceParser::parseEventTimeJackson,
            BIDS, JacksonIncrementParser::parseBid,
            ASKS, JacksonIncrementParser::parseAsk);
        return new JacksonKeyMap<>(actions, JacksonIncrementParser.skip());
    }

    public static void parseIncrement(final JsonParser jsonDecoder, final L2Update increment)
    {
        parseUpdateJackson(jsonDecoder, increment, ACTIONS);
    }

    @SneakyThrows
    public static void parseQuote(final JsonParser jsonDecoder, final L2Side side, final JsonToken startToken)
    {
        JacksonUtils.checkToken(JsonToken.START_ARRAY, startToken);

        var token = jsonDecoder.nextToken();
        JacksonUtils.checkToken(JsonToken.VALUE_STRING, token);
        final double price = jsonDecoder.getValueAsDouble();

        token = jsonDecoder.nextToken();
        JacksonUtils.checkToken(JsonToken.VALUE_STRING, token);
        final double size = jsonDecoder.getValueAsDouble();

        side.addQuote(price, size);

        token = jsonDecoder.nextToken();
        JacksonUtils.checkToken(JsonToken.END_ARRAY, token);
    }

    static void parseBid(final JsonParser t, final L2Update u)
    {
        parseArray(t, u.sides.getBid(), PARSE_QUOTE);
    }

    static void parseAsk(final JsonParser t, final L2Update u)
    {
        parseArray(t, u.sides.getAsk(), PARSE_QUOTE);
    }

    public static void parseUpdateJackson(
        final JsonParser jsonDecoder,
        final L2Update update,
        final JacksonKeyMap<BiConsumer<JsonParser, L2Update>> actions)
    {
        update.clear();
        JacksonUtils.parseStruct(jsonDecoder, actions, update);
    }

    @SneakyThrows
    public static void parseIncrementTree(final ObjectMapper mapper, final String string, final L2Update update)
    {
        final var tree = mapper.readTree(string);
        update.clear();
        update.timestamp = tree.get(EVENT_TIME).longValue();
        fillSide((ArrayNode)tree.get(BIDS), update.sides.getBid());
        fillSide((ArrayNode)tree.get(ASKS), update.sides.getAsk());
    }

    private static void fillSide(final ArrayNode quotes, final L2Side side)
    {
        for (final var quote : quotes)
        {
            final ArrayNode quoteArray = (ArrayNode)quote;
            final double price = quoteArray.get(0).asDouble();
            final double size = quoteArray.get(1).asDouble();
            side.addQuote(price, size);
        }
    }
}
