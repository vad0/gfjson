package tokenizer;

import java.util.Map;
import java.util.function.BiConsumer;

public class IncrementParser {
    private static final KeyMap<BiConsumer<Tokenizer, L2Update>> ACTIONS = new KeyMap<>(Map.of(
            "E", BinanceParser::parseEventTime,
            "b", BinanceParser::parseBid,
            "a", BinanceParser::parseAsk),
            Tokenizer.skip());

    public static void parseIncrement(Tokenizer tokenizer, L2Update increment) {
        BinanceParser.parseUpdate(tokenizer, increment, ACTIONS);
    }
}
