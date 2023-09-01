package ser;

import uk.co.real_logic.artio.util.MutableAsciiBuffer;

import java.nio.ByteBuffer;

/**
 * Base64 encoding in GC-free way
 */
public class B64Encoder
{
    private static final String BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    private static final ByteBuffer BASE64_CHARS_BUFFER = ByteBuffer.wrap(BASE64_CHARS.getBytes());
    private static final byte PAD_BYTE = '=';

    /**
     * Encode given buffer with base64 value of source buffer
     * <a href="https://en.wikibooks.org/wiki/Algorithm_Implementation/Miscellaneous/Base64"/>
     * This implementation is a mix of Java and C++ implementations
     */
    public static int encode(
        final int srcStartPos,
        final int srcLen,
        final int dstStartPos,
        final ByteBuffer source,
        final MutableAsciiBuffer destination)
    {
        int srcIdx = srcStartPos;

        int destIdx = dstStartPos;

        final int lastSourceIdx = srcLen + srcStartPos;
        while (srcIdx < srcLen)
        {
            if ((lastSourceIdx - srcIdx) < 3)
            {
                break;
            }

            int n = source.get(srcIdx) << 16;
            n += source.get(srcIdx + 1) << 8;
            n += source.get(srcIdx + 2);

            final int n1 = (n >> 18) & 63;
            final int n2 = (n >> 12) & 63;
            final int n3 = (n >> 6) & 63;
            final int n4 = n & 63;

            final byte c1 = BASE64_CHARS_BUFFER.get(n1);
            final byte c2 = BASE64_CHARS_BUFFER.get(n2);
            final byte c3 = BASE64_CHARS_BUFFER.get(n3);
            final byte c4 = BASE64_CHARS_BUFFER.get(n4);

            destination.putByte(destIdx++, c1);
            destination.putByte(destIdx++, c2);
            destination.putByte(destIdx++, c3);
            destination.putByte(destIdx++, c4);

            srcIdx += 3;
        }

        final int remainder = srcLen % 3;

        if (remainder == 1)
        {
            destIdx = writeLastOneChar(srcIdx, destIdx, source, destination);
        }
        else if (remainder == 2)
        {
            destIdx = writeLastTwoChars(srcIdx, destIdx, source, destination);
        }
        else
        {
            assert remainder == 0;
        }

        return destIdx - dstStartPos;
    }

    private static int writeLastOneChar(
        final int srcIdx,
        final int destIdxParam,
        final ByteBuffer src,
        final MutableAsciiBuffer dst)
    {
        var result = destIdxParam;
        final int n = src.get(srcIdx) << 16;

        final var n1 = (n >> 18) & 63;
        final var n2 = (n >> 12) & 63;

        final var c1 = BASE64_CHARS_BUFFER.get(n1);
        final var c2 = BASE64_CHARS_BUFFER.get(n2);

        dst.putByte(result++, c1);
        dst.putByte(result++, c2);
        dst.putByte(result++, PAD_BYTE);
        dst.putByte(result++, PAD_BYTE);

        return result;
    }

    private static int writeLastTwoChars(
        final int srcIdx,
        final int destIdx,
        final ByteBuffer src,
        final MutableAsciiBuffer dst)
    {
        var result = destIdx;
        int n = src.get(srcIdx) << 16;
        n += src.get(srcIdx + 1) << 8;

        final var n1 = (n >> 18) & 63;
        final var n2 = (n >> 12) & 63;
        final var n3 = (n >> 6) & 63;

        final var c1 = BASE64_CHARS_BUFFER.get(n1);
        final var c2 = BASE64_CHARS_BUFFER.get(n2);
        final var c3 = BASE64_CHARS_BUFFER.get(n3);

        dst.putByte(result++, c1);
        dst.putByte(result++, c2);
        dst.putByte(result++, c3);
        dst.putByte(result++, PAD_BYTE);

        return result;
    }
}
