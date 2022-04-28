package de;

import java.util.Map;
import java.util.function.BiConsumer;

public class IncrementParser
{
    public static final String EVENT_TYPE = "e";
    public static final String SYMBOL = "s";
    public static final String EVENT_TIME = "E";
    public static final String FIRST_UPDATE_ID = "U";
    public static final String LAST_UPDATE_ID = "u";
    public static final String BIDS = "b";
    public static final String ASKS = "a";
    private static final KeyMap<BiConsumer<JsonDecoder, L2Update>> ACTIONS = initKeyMap();

    private static KeyMap<BiConsumer<JsonDecoder, L2Update>> initKeyMap()
    {
        final Map<String, BiConsumer<JsonDecoder, L2Update>> actions = Map.of(
            EVENT_TIME, (jsonDecoder, update) -> update.timestamp = jsonDecoder.nextLong(),
            BIDS, BinanceParser::parseBid,
            ASKS, BinanceParser::parseAsk);
        return new KeyMap<>(actions, JsonDecoder.skip());
    }

    public static void parseIncrementGfJson(final JsonDecoder jsonDecoder, final L2Update increment)
    {
        BinanceParser.parseUpdate(jsonDecoder, increment, ACTIONS);
    }
}
