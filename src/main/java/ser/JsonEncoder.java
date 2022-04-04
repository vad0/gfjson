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
    private final MutableAsciiBuffer buffer = new MutableAsciiBuffer();
    private final DecimalFloat decimalFloat = new DecimalFloat();
    @Getter
    private int offset = 0;

    public void putDoubleAsString(final double value)
    {
        putQuote();
        putDouble(value);
        putQuote();
    }

    public <E, A> void encodeArray(
        final A array,
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

    public <T> void encodeArray(
        final BiConsumer<JsonEncoder, T> encodeElement,
        final List<T> array)
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

    public void putKey(final String key)
    {
        putString(key);
        putChar(':');
    }

    public void nextKey(final String key)
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

    public void putString(final String str)
    {
        putQuote();
        putRawString(str);
        putQuote();
    }

    /**
     * Writes string without wrapping quotes. So can be used to write 'true' or 'false'
     */
    private void putRawString(final String str)
    {
        final int length = str.length();
        for (int i = 0; i < length; i++)
        {
            putChar(str.charAt(i));
        }
    }

    public void putLong(final long value)
    {
        if (value == 0)
        {
            putChar('0');
            return;
        }

        // Handle zero
        long v = value;
        if (v < 0)
        {
            putChar('-');
            v = -v;
        }

        // We don't know how many digits will we have to write. So we write them one by one in the reverse order.
        final int startOffset = offset;
        while (v > 0)
        {
            putChar((char)('0' + v % 10));
            v /= 10;
        }
        // We have to reverse the order of the written digits
        final int n = (offset - startOffset) / 2;
        for (int i = 0; i < n; i++)
        {
            final int so = startOffset + i;
            final int eo = offset - 1 - i;
            final byte start = buffer.getByte(so);
            final byte end = buffer.getByte(eo);
            buffer.putByte(so, end);
            buffer.putByte(eo, start);
        }
    }

    public void putDouble(final double value)
    {
        decimalFloat.fromDouble(value);
        putDecimalFloat(decimalFloat);
    }

    private void putDecimalFloat(final DecimalFloat x)
    {
        final int encodedLength = buffer.putFloatAscii(offset, x.value(), x.scale());
        offset += encodedLength;
    }

    public void putBoolean(final boolean value)
    {
        if (value)
        {
            putRawString("true");
        }
        else
        {
            putRawString("false");
        }
    }

    public void readString(final AsciiSequenceView into, final int startInclusive, final int endExclusive)
    {
        into.wrap(buffer, startInclusive, endExclusive - startInclusive);
    }
}
