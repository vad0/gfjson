package md;

import de.JsonDecoder;
import de.Array;
import org.junit.jupiter.api.Test;
import utils.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SizePriceTest
{
    @Test
    public void parseArray()
    {
        final var expected = new Array<>(SizePrice::new);
        expected.claimNext()
            .lots(15)
            .price(12.5);
        expected.claimNext()
            .lots(16)
            .price(19.5);

        final var actual = new Array<>(SizePrice::new);
        final var str = TestUtils.readFile("md/size_price_array.json");
        final var jsonDecoder = new JsonDecoder();
        jsonDecoder.wrap(str);
        final var sizePriceDecoder = new SizePriceDecoder();
        jsonDecoder.parseArray(actual, sizePriceDecoder);

        assertEquals(expected, actual);
    }
}
