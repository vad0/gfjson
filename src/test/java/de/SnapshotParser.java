package de;

import java.util.Map;
import java.util.function.BiConsumer;

public class SnapshotParser
{
    private static final KeyMap<BiConsumer<JsonDecoder, L2Update>> ACTIONS = initKeyMap();

    private static KeyMap<BiConsumer<JsonDecoder, L2Update>> initKeyMap()
    {
        final Map<String, BiConsumer<JsonDecoder, L2Update>> actions = Map.of(
            "lastUpdateId", BinanceParser::parseEventTime,
            "bids", BinanceParser::parseBid,
            "asks", BinanceParser::parseAsk);
        return new KeyMap<>(actions, JsonDecoder.skip());
    }

    public static void parseSnapshot(final JsonDecoder jsonDecoder, final L2Update snapshot)
    {
        BinanceParser.parseUpdate(jsonDecoder, snapshot, ACTIONS);
    }
}
