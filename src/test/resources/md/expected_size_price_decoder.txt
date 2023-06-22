package md;

import de.JsonDecoder;
import de.KeyMap;
import de.ParseArrayElement;
import de.Token;
import de.Array;
import org.agrona.AsciiSequenceView;


public class SizePriceDecoder
    implements ParseArrayElement<Array<SizePrice>>
{
    private static final AsciiSequenceView LOTS = KeyMap.string2view("l");
    private static final AsciiSequenceView PRICE = KeyMap.string2view("p");


    public void parse(final JsonDecoder decoder, final SizePrice struct)
    {
        decoder.nextStartObject();
        finishParsing(decoder, struct);
    }

    private void finishParsing(final JsonDecoder decoder, final SizePrice struct)
    {
        decoder.checkKey(LOTS);
        struct.lots(decoder.nextLong());

        decoder.checkKey(PRICE);
        struct.price(decoder.nextDoubleFromString());

        decoder.nextEndObject();
    }

    @Override
    public void parseElement(final JsonDecoder jsonDecoder, final Array<SizePrice> array, final Token firstToken)
    {
        Token.START_OBJECT.checkToken(firstToken);
        final var struct = array.claimNext();
        finishParsing(jsonDecoder, struct);
    }
}
