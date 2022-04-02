package tokenizer;

import java.util.Map;
import java.util.function.BiConsumer;

public class SnapshotParser
{
    private static final KeyMap<BiConsumer<Tokenizer, L2Update>> ACTIONS = initKeyMap();

    private static KeyMap<BiConsumer<Tokenizer, L2Update>> initKeyMap()
    {
        final Map<String, BiConsumer<Tokenizer, L2Update>> actions = Map.of(
            "lastUpdateId", BinanceParser::parseEventTime,
            "bids", BinanceParser::parseBid,
            "asks", BinanceParser::parseAsk);
        return new KeyMap<>(actions, Tokenizer.skip());
    }

    public static void parseSnapshot(final Tokenizer tokenizer, final L2Update snapshot)
    {
        BinanceParser.parseUpdate(tokenizer, snapshot, ACTIONS);
    }
}
