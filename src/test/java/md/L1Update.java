package md;

import lombok.Data;
import lombok.experimental.Accessors;
import org.agrona.collections.MutableInteger;

@Data
@Accessors(fluent = true)
public class L1Update
{
    private long updateId;
    private double bestBidPrice;
    private MutableInteger symbol;
    private TimeInForce timeInForce;
    private boolean isFast;
}
