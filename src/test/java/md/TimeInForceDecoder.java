package md;

import de.KeyMap;
import org.agrona.AsciiSequenceView;

public class TimeInForceDecoder
{
    private static final KeyMap<TimeInForce> MAP = KeyMap.forEnum(TimeInForce.class);

    public static TimeInForce parse(AsciiSequenceView string)
    {
        return MAP.getKey(string);
    }
}
