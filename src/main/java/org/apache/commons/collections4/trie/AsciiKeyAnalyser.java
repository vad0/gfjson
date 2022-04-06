package org.apache.commons.collections4.trie;

import org.agrona.AsciiSequenceView;

public class AsciiKeyAnalyser
    extends KeyAnalyzer<AsciiSequenceView>
{
    public static final AsciiKeyAnalyser INSTANCE = new AsciiKeyAnalyser();
    public static final int LENGTH = Character.SIZE;
    private static final int MSB = 0x8000;

    @Override
    public int bitsPerElement()
    {
        return LENGTH;
    }

    @Override
    public int lengthInBits(final AsciiSequenceView key)
    {
        return key != null ? key.length() * LENGTH : 0;
    }

    @Override
    public boolean isBitSet(final AsciiSequenceView key, final int bitIndex, final int lengthInBits)
    {
        if (key == null || bitIndex >= lengthInBits)
        {
            return false;
        }

        final int index = bitIndex / LENGTH;
        final int bit = bitIndex % LENGTH;

        return (key.charAt(index) & (MSB >>> bit)) != 0;
    }

    @Override
    public int bitIndex(
        final AsciiSequenceView key,
        final int offsetInBits,
        final int lengthInBits,
        final AsciiSequenceView other,
        final int otherOffsetInBits,
        final int otherLengthInBits)
    {
        boolean allNull = true;

        if (offsetInBits % LENGTH != 0 || otherOffsetInBits % LENGTH != 0 ||
            lengthInBits % LENGTH != 0 || otherLengthInBits % LENGTH != 0)
        {
            throw new IllegalArgumentException("The offsets and lengths must be at Character boundaries");
        }

        final int beginIndex1 = offsetInBits / LENGTH;
        final int beginIndex2 = otherOffsetInBits / LENGTH;

        final int endIndex1 = beginIndex1 + lengthInBits / LENGTH;
        final int endIndex2 = beginIndex2 + otherLengthInBits / LENGTH;

        final int length = Math.max(endIndex1, endIndex2);

        // Look at each character, and if they're different
        // then figure out which bit makes the difference
        // and return it.
        char k, f;
        for (int i = 0; i < length; i++)
        {
            final int index1 = beginIndex1 + i;
            final int index2 = beginIndex2 + i;

            if (index1 >= endIndex1)
            {
                k = 0;
            }
            else
            {
                k = key.charAt(index1);
            }

            if (other == null || index2 >= endIndex2)
            {
                f = 0;
            }
            else
            {
                f = other.charAt(index2);
            }

            if (k != f)
            {
                final int x = k ^ f;
                return i * LENGTH + Integer.numberOfLeadingZeros(x) - LENGTH;
            }

            if (k != 0)
            {
                allNull = false;
            }
        }

        // All bits are 0
        if (allNull)
        {
            return KeyAnalyzer.NULL_BIT_KEY;
        }

        // Both keys are equal
        return KeyAnalyzer.EQUAL_BIT_KEY;
    }

    @Override
    public boolean isPrefix(
        final AsciiSequenceView prefix,
        final int offsetInBits,
        final int lengthInBits,
        final AsciiSequenceView key)
    {
        if (offsetInBits % LENGTH != 0 || lengthInBits % LENGTH != 0)
        {
            throw new IllegalArgumentException(
                "Cannot determine prefix outside of Character boundaries");
        }

        final int offset = offsetInBits / LENGTH;
        final int length = lengthInBits / LENGTH;

        for (int i = 0; i < length; i++)
        {
            final char prefixChar = prefix.charAt(offset + i);
            final char keyChar = key.charAt(i);
            if (prefixChar != keyChar)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compare(final AsciiSequenceView a, final AsciiSequenceView b)
    {
        final int aCapacity = a.length();
        final int bCapacity = b.length();
        final int aOffset = a.offset();
        final int bOffset = b.offset();

        for (int i = 0, length = Math.min(aCapacity, bCapacity); i < length; i++)
        {
            final int cmp = Byte.compare(
                a.buffer().getByte(aOffset + i),
                b.buffer().getByte(bOffset + i));

            if (0 != cmp)
            {
                return cmp;
            }
        }

        if (aCapacity != bCapacity)
        {
            return aCapacity - bCapacity;
        }
        return 0;
    }
}
