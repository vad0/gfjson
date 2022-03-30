package tokenizer;

import java.util.Objects;

public class L2Update {
    private static final long NOT_INITIALIZED = -1;
    final SideMap<L2Side> sides = new SideMap<>(new L2Side(), new L2Side());
    long timestamp;

    public void clear() {
        timestamp = NOT_INITIALIZED;
        sides.getBid().clear();
        sides.getAsk().clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        L2Update update = (L2Update) o;
        return timestamp == update.timestamp && Objects.equals(sides, update.sides);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sides, timestamp);
    }
}
