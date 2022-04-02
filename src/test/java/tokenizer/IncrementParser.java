package tokenizer;

import java.util.Map;
import java.util.function.BiConsumer;

public class IncrementParser
{
    private static final KeyMap<BiConsumer<Tokenizer, L2Update>> ACTIONS = initKeyMap();

    private static KeyMap<BiConsumer<Tokenizer, L2Update>> initKeyMap()
    {
        final Map<String, BiConsumer<Tokenizer, L2Update>> actions = Map.of(
            "E", BinanceParser::parseEventTime,
            "b", BinanceParser::parseBid,
            "a", BinanceParser::parseAsk);
        return new KeyMap<>(actions, Tokenizer.skip());
    }

    public static void parseIncrement(final Tokenizer tokenizer, final L2Update increment)
    {
        BinanceParser.parseUpdate(tokenizer, increment, ACTIONS);
    }
}
