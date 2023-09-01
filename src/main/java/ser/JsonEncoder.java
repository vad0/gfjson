package ser;

import lombok.Getter;
import org.agrona.AsciiSequenceView;
import uk.co.real_logic.artio.fields.DecimalFloat;
import uk.co.real_logic.artio.util.MutableAsciiBuffer;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiConsumer;

public class JsonEncoder
{
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private final MutableAsciiBuffer buffer = new MutableAsciiBuffer();
    @Getter
    private final DecimalFloat decimalFloat = new DecimalFloat();
    @Getter
    private int offset = 0;

    public int advanceOffset(final int diff)
    {
        return offset += diff;
    }

    public void putDoubleAsString(final double value)
    {
        putQuote();
        putDouble(value);
        putQuote();
    }

    public void putByteBufferAsBase64(final ByteBuffer bufferToEncode)
    {
        putByteBufferAsBase64(bufferToEncode,
            bufferToEncode.position(),
            bufferToEncode.limit() - bufferToEncode.position());
    }

    public void putByteBufferAsBase64(final ByteBuffer bufferToEncode, final int startIdx, final int srcLen)
    {
        putQuote();
        final var encodedLength = B64Encoder.encode(startIdx, srcLen, offset, bufferToEncode, buffer);
        advanceOffset(encodedLength);
        putQuote();
    }

    public <E, A> void encodeArray(final A array,
        final int arraySize,
        final E element,
        final FillElement<A, E> fillElement,
        final BiConsumer<JsonEncoder, E> encodeElement)
    {
        startArray();
        if (arraySize == 0)
        {
            endArray();
            return;
        }
        fillElement.fillElement(array, 0, element);
        encodeElement.accept(this, element);
        for (int i = 1; i < arraySize; i++)
        {
            putComma();
            fillElement.fillElement(array, i, element);
            encodeElement.accept(this, element);
        }
        endArray();
    }

    public <T> void encodeArray(final BiConsumer<JsonEncoder, T> encodeElement, final List<T> array)
    {
        startArray();
        final int size = array.size();
        if (size == 0)
        {
            endArray();
            return;
        }
        encodeElement.accept(this, array.get(0));
        for (int i = 1; i < size; i++)
        {
            putComma();
            encodeElement.accept(this, array.get(i));
        }
        endArray();
    }

    public void wrap(final ByteBuffer byteBuffer)
    {
        buffer.wrap(byteBuffer);
        offset = 0;
    }

    public void wrap(final ByteBuffer byteBuffer, final int offset)
    {
        buffer.wrap(byteBuffer);
        this.offset = offset;
    }

    private void putChar(final char c)
    {
        buffer.putCharAscii(offset++, c);
    }

    public void startObject()
    {
        putChar('{');
    }

    public void endObject()
    {
        putChar('}');
    }

    public void startArray()
    {
        putChar('[');
    }

    public void endArray()
    {
        putChar(']');
    }

    public void putKey(final CharSequence key)
    {
        putString(key);
        putChar(':');
    }

    public void nextKey(final CharSequence key)
    {
        putComma();
        putKey(key);
    }

    public void putComma()
    {
        putChar(',');
    }

    public void putQuote()
    {
        putChar('"');
    }

    public void putString(final CharSequence str)
    {
        putQuote();
        putRawString(str);
        putQuote();
    }

    /**
     * Writes string without wrapping quotes. So can be used to write 'true' or 'false'
     */
    public void putRawString(final CharSequence str)
    {
        offset += buffer.putStringWithoutLengthAscii(offset, str);
    }

    public void putLong(final long value)
    {
        final int encodedLength = buffer.putLongAscii(offset, value);
        offset += encodedLength;
    }

    public void putDouble(final double value)
    {
        final boolean success = decimalFloat.fromDouble(value);
        if (!success)
        {
            throw new NumberFormatException("Can't represent " + value + " as DecimalFloat");
        }
        putDecimalFloat(decimalFloat);
    }

    public void putDecimalFloat(final DecimalFloat x)
    {
        if (x.isNaNValue())
        {
            final int encodedLength = buffer.putStringWithoutLengthAscii(offset, "NaN");
            offset += encodedLength;
            return;
        }
        final int encodedLength = buffer.putFloatAscii(offset, x.value(), x.scale());
        offset += encodedLength;
    }

    public void putBoolean(final boolean value)
    {
        if (value)
        {
            putRawString(TRUE);
        }
        else
        {
            putRawString(FALSE);
        }
    }

    public void readString(final AsciiSequenceView into, final int startInclusive, final int endExclusive)
    {
        into.wrap(buffer, startInclusive, endExclusive - startInclusive);
    }

    public void nextLine()
    {
        putChar('\n');
    }
}
