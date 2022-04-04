package de;

import org.agrona.collections.LongArrayList;

import java.util.Objects;
import java.util.StringJoiner;

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

    public void getQuote(final Quote quote, final int index)
    {
        quote.price = Double.longBitsToDouble(list.getLong(index * 2));
        quote.size = Double.longBitsToDouble(list.getLong(index * 2 + 1));
    }

    public boolean isEmpty()
    {
        return list.isEmpty();
    }

    public int size()
    {
        return list.size() / 2;
    }

    @Override
    public String toString()
    {
        final StringJoiner joiner = new StringJoiner(", ");
        final Quote quote = new Quote();
        for (int i = 0; i < size(); i++)
        {
            getQuote(quote, i);
            joiner.add("[" + quote.toString() + "]");
        }
        return "[" + joiner.toString() + "]";
    }
}
