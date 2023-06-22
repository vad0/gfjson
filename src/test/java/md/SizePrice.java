package md;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class SizePrice
{
    private long lots;
    private double price;
}
