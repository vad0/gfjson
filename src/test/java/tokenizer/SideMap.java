package tokenizer;

import lombok.Getter;

import java.util.Objects;

public class SideMap<T>
{
    @Getter
    private final T bid;
    @Getter
    private final T ask;

    public SideMap(final T bid, final T ask)
    {
        this.bid = bid;
        this.ask = ask;
    }

    public T get(final Side side)
    {
        switch (side)
        {
            case BID:
                return bid;
            case ASK:
                return ask;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final SideMap<?> sideMap = (SideMap<?>)o;
        return Objects.equals(bid, sideMap.bid) && Objects.equals(ask, sideMap.ask);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(bid, ask);
    }
}
