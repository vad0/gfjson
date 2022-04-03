package de;

import org.agrona.collections.LongArrayList;

import java.util.Objects;

public class L2Side
{
    private final LongArrayList list = new LongArrayList();

    public void addQuote(final Quote quote)
    {
        addQuote(quote.price, quote.size);
    }

    public L2Side addQuote(final double price, final double size)
    {
        list.addLong(Double.doubleToLongBits(price));
        list.addLong(Double.doubleToLongBits(size));
        return this;
    }

    public void clear()
    {
        list.clear();
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
        final L2Side l2Side = (L2Side)o;
        return Objects.equals(list, l2Side.list);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(list);
    }
}
