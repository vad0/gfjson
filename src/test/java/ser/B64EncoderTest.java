package ser;

import org.junit.jupiter.api.Test;
import uk.co.real_logic.artio.util.MutableAsciiBuffer;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class B64EncoderTest
{
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
}