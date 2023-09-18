package ser;

import org.junit.jupiter.api.Test;
import uk.co.real_logic.artio.util.MutableAsciiBuffer;

import java.nio.ByteBuffer;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

class B64EncoderTest
{

    public static final String BUFFER_WITH_SIGN_EXTEND = "WgAAAMAckNoDGhhZKHARZM" +
        "+BgIKCAOhTV1DPVVNES1pUX1RPxJOBwMqAyoDKgDi2HXAMsSRS3vwEObaGhc/OzLAxNjmwJQTB/AQ5jowxNzC1Jm+O" +
        "/AQ5jcyxMTcwtiZ9vfwEObXMsDE3MbEnRtn8BDmOzLExNzO0Kgyh/AQ5towxNzO3Kjiy/AQ5tYwxNzW1LGXh/AQ5towxNza0LXDc" +
        "/AQ5tYwxNze0Lwr7/AQ5tsywMTc3ti8krfwEOY+MMTgwuDJY9/wEOY3MsTE4MLkzC6r8BDm1zLAxODGyMzTI/AQ5jowxODG4NDWZ" +
        "/AQ5jYwxODOzNnHt/AQ5jsyxMTgztDcHsvwEObbMsDE4NbA4bZD8BDmPjDE4NbY5Nt38BDmUzLExODW3OULA/AQ5vYwxODa0OhC6" +
        "/AQ5vMywMTg2uTpP+/wEOZPMsTE4N7A6Wtj8BDm7zLAxODezOnzW/AQ5lIwxODiwO1n+/Tj1zLExODixO2ac" +
        "/Tj53LAxOTCwHXAUmQNe4PwEOZOMMTkwtgQ09f049cyxMTkysRYu0fwEObmMMTkyshY+jP04+YwxOTK3FwiE/AQ5ucywMTkzsBc6" +
        "/PwEOZGMMTk1sBoJifwEOZPMsTE5NbEaF4f9OPnMsDE5NbMaNNP9OPXMsTE5NbQaRd38BDm5zLAxOTW3GnHh/AQ5kYwxOTaxGzSo" +
        "/Tj1zLExOTayG0LN/Tj5zLAxOTe4HUbC/AQ5kcyxMTk3uR1SyvwEObmMMTk5tx9C6PwEObjMsDIwMLEfftT8BDmPzLEyMDCyIA2m" +
        "/AQ5t4wyMDC0IC6m/AQ5towyMDC3IFrW/AQ5t8ywMjAxsCB+9vwEOZDMsTIwMbMhI+r8BDm4jDIwMbYhYYn8BDm33rAyNjK2HXMTgSt58" +
        "/wEOamHhcyxMTU2tBdktfwEObfMsDE1NrgYF7H8BDmRzLExNTa5GCao/AQ5uIwxNTezGF/P/AQ5ucywMTU3uBkhkv049cyxMTU4txow1f04" +
        "+cywMTU5txtEx/wEOZHMsTE1ObgbUOX8BDm4zLAxNjC1HCmG/AQ5kMyxMTYwthw39vwEObeMMTYwuRxc7" +
        "/wEObjMsDE2MbMdFab8BDmNzLExNjG0HSPn/AQ5tcywMTYxuB1WsvwEOYzMsTE2MbkdaNn8BDm0zLAxNjK0Hiqt/AQ5jYwxNjK4HmjG" +
        "/AQ5jMyxMTY1tSFdt/wEObPMsDE2NrIiOYD8BDmOzLExNjazIkPQ/AQ5towxNje2I1vz/AQ5tcywMTY4tSRHmfwEOY/8BDmFrfwCeP+VAAAA";

    @Test
    public void testBase64Encode()
    {
        final var source = ByteBuffer.wrap("some\t\ntext".getBytes());
        final var dest = new MutableAsciiBuffer(new byte[30]);

        final var len = B64Encoder.encode(0, source.limit(), 0, source, dest);
        final var outStr = dest.getAscii(0, len);

        assertEquals("c29tZQkKdGV4dA==", outStr);
    }

    @Test
    public void testBase64Encode2()
    {
        final var source = ByteBuffer.wrap("some\t\ntex".getBytes());
        final var dest = new MutableAsciiBuffer(new byte[30]);

        final var len = B64Encoder.encode(0, source.limit(), 0, source, dest);
        final var outStr = dest.getAscii(0, len);

        assertEquals("c29tZQkKdGV4", outStr);
    }

    @Test
    public void testBase64Encode3()
    {
        final var source = ByteBuffer.wrap("some\t\nte".getBytes());
        final var dest = new MutableAsciiBuffer(new byte[30]);

        final var len = B64Encoder.encode(0, source.limit(), 0, source, dest);
        final var outStr = dest.getAscii(0, len);

        assertEquals("c29tZQkKdGU=", outStr);
    }

    @Test
    public void testBase64EncodeWithOffsets()
    {
        final var source = ByteBuffer.wrap("some\t\ntext".getBytes());
        final var dest = new MutableAsciiBuffer(new byte[30]);

        final var len = B64Encoder.encode(2, source.limit() - 4, 2, source, dest);
        final var outStr = dest.getAscii(2, len);

        assertEquals("bWUJCnRl", outStr);
    }


    @Test
    public void testBase64EncoderBug1()
    {
        final var src = Base64.getDecoder().decode(BUFFER_WITH_SIGN_EXTEND);

        final var dest = new MutableAsciiBuffer(new byte[1500]);
        final var ourEncodedLen = B64Encoder.encode(0, src.length, 0, ByteBuffer.wrap(src), dest);
        final var ourEncodedStr = dest.getAscii(0, ourEncodedLen);

        assertEquals(BUFFER_WITH_SIGN_EXTEND, ourEncodedStr);
    }
}