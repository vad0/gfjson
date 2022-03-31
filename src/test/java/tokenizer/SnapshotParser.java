package tokenizer;

import java.util.Map;
import java.util.function.BiConsumer;

public class SnapshotParser {
    private static final KeyMap<BiConsumer<Tokenizer, L2Update>> ACTIONS = new KeyMap<>(Map.of(
            "lastUpdateId", BinanceParser::parseEventTime,
            "bids", BinanceParser::parseBid,
            "asks", BinanceParser::parseAsk),
            (t, u) -> t.next());

    public static void parseSnapshot(Tokenizer tokenizer, L2Update snapshot) {
        BinanceParser.parseUpdate(tokenizer, snapshot, ACTIONS);
    }
}
