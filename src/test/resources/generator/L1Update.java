package generator;

import org.agrona.collections.MutableInteger;

import java.util.Objects;


public class L1Update
{
    /**
     * order book updateId
     */
    private long updateId;
    /**
     * best bid price
     */
    private double bestBidPrice;
    private MutableInteger symbol;
    private TimeInForce timeInForce;
    private boolean isFast;

    public long updateId()
    {
        return updateId;
    }

    public double bestBidPrice()
    {
        return bestBidPrice;
    }

    public MutableInteger symbol()
    {
        return symbol;
    }

    public TimeInForce timeInForce()
    {
        return timeInForce;
    }

    public boolean isFast()
    {
        return isFast;
    }

    public L1Update updateId(final long updateId)
    {
        this.updateId = updateId;
        return this;
    }

    public L1Update bestBidPrice(final double bestBidPrice)
    {
        this.bestBidPrice = bestBidPrice;
        return this;
    }

    public L1Update symbol(final MutableInteger symbol)
    {
        this.symbol = symbol;
        return this;
    }

    public L1Update timeInForce(final TimeInForce timeInForce)
    {
        this.timeInForce = timeInForce;
        return this;
    }

    public L1Update isFast(final boolean isFast)
    {
        this.isFast = isFast;
        return this;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (o == this)
        {
            return true;
        }
        if (!(o instanceof final L1Update other))
        {
            return false;
        }
        if (updateId() != other.updateId())
        {
            return false;
        }
        if (Double.compare(bestBidPrice(), other.bestBidPrice()) != 0)
        {
            return false;
        }
        if (!Objects.equals(symbol(), other.symbol()))
        {
            return false;
        }
        if (timeInForce() != other.timeInForce())
        {
            return false;
        }
        if (isFast() != other.isFast())
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 59;
        int result = 1;
        result = result * prime + Long.hashCode(updateId);
        result = result * prime + Double.hashCode(bestBidPrice);
        result = result * prime + Objects.hashCode(symbol);
        result = result * prime + Objects.hashCode(timeInForce);
        result = result * prime + Boolean.hashCode(isFast);
        return result;
    }

    @Override
    public String toString()
    {
        return "L1Update(updateId=" + updateId() +
            ", bestBidPrice=" + bestBidPrice() +
            ", symbol=" + symbol() +
            ", timeInForce=" + timeInForce() +
            ", isFast=" + isFast() +
            ")";
    }
}
