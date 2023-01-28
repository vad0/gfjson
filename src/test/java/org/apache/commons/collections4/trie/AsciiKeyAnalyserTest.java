package org.apache.commons.collections4.trie;

import de.KeyMap;
import org.agrona.AsciiSequenceView;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AsciiKeyAnalyserTest
{
    @Test
    public void testBitsPerElement()
    {
        final var analyser = new AsciiKeyAnalyser();
        assertEquals(16, analyser.bitsPerElement());
    }

    @Test
    public void testLengthInBits()
    {
        final var analyser = new AsciiKeyAnalyser();
        final var key = KeyMap.string2view("123");
        assertEquals(48, analyser.lengthInBits(key));
    }

    @Test
    public void testIsBitSetNull()
    {
        final var analyser = new AsciiKeyAnalyser();
        assertFalse(analyser.isBitSet(null, 5, 6));
    }

    @Test
    public void testIsBitSetTooHigh()
    {
        final var analyser = new AsciiKeyAnalyser();
        final var key = KeyMap.string2view("123");
        assertFalse(analyser.isBitSet(key, 6, 5));
    }

    @Test
    public void testIsBitSetNormal()
    {
        final var analyser = new AsciiKeyAnalyser();
        final var key = KeyMap.string2view("123");
        assertFalse(analyser.isBitSet(key, 3, 4));
    }

    @Test
    public void testIsPrefix()
    {
        final var analyser = new AsciiKeyAnalyser();
        final var a = KeyMap.string2view("123");
        final var b = KeyMap.string2view("1245");
        final int bits = analyser.bitsPerElement();
        assertTrue(analyser.isPrefix(a, 0, bits * 2, b));
        assertFalse(analyser.isPrefix(a, 0, bits * 3, b));
        assertTrue(analyser.isPrefix(b, 0, bits * 2, a));
        assertFalse(analyser.isPrefix(b, 0, bits * 3, a));
    }

    @Test
    public void testIsPrefixFail()
    {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
        {
            final var analyser = new AsciiKeyAnalyser();
            final var a = KeyMap.string2view("123");
            final var b = KeyMap.string2view("1245");
            final int wrongBits = analyser.bitsPerElement() - 1;
            analyser.isPrefix(a, wrongBits, wrongBits * 2, b);
        });
    }

    @Test
    public void testCompare()
    {
        final var analyser = new AsciiKeyAnalyser();
        final var a = KeyMap.string2view("123");
        final var b = KeyMap.string2view("1245");
        assertEquals(0, analyser.compare(a, a));
        assertEquals(1, analyser.compare(b, a));
        assertEquals(-1, analyser.compare(a, b));
    }

    @Test
    public void testCompareSubstring()
    {
        final var analyser = new AsciiKeyAnalyser();
        final var a = KeyMap.string2view("124");
        final var b = KeyMap.string2view("1245");
        assertEquals(0, analyser.compare(a, a));
        assertEquals(1, analyser.compare(b, a));
        assertEquals(-1, analyser.compare(a, b));
    }

    @Test
    public void testBitIndexWrongOffsets()
    {
        assertThrows(IllegalArgumentException.class, () ->
        {
            final var analyser = new AsciiKeyAnalyser();
            final var a = KeyMap.string2view("124");
            final var b = KeyMap.string2view("1245");
            analyser.bitIndex(a, 0, 3, b, 0, 4);
        });
    }

    @Test
    public void testBitIndex()
    {
        final var analyser = new AsciiKeyAnalyser();
        final var a = KeyMap.string2view("123");
        final var b = KeyMap.string2view("1245");
        final int bits = analyser.bitsPerElement();
        assertEquals(45, analyser.bitIndex(a, 0, a.length() * bits, b, 0, b.length() * bits));
        assertEquals(10, analyser.bitIndex(a, 0, a.length() * bits, null, 0, b.length() * bits));
        assertEquals(10, analyser.bitIndex(a, 0, 0, b, 0, b.length() * bits));
        assertEquals(-2, analyser.bitIndex(a, 0, a.length() * bits, a, 0, a.length() * bits));
    }

    @Test
    public void testBitIndexAllZeros()
    {
        final var analyser = new AsciiKeyAnalyser();
        final var a = new AsciiSequenceView(new UnsafeBuffer(new byte[16], 0, 16), 0, 16);
        final var b = new AsciiSequenceView(new UnsafeBuffer(new byte[16], 0, 16), 0, 16);
        final int bits = analyser.bitsPerElement();
        assertEquals(-1, analyser.bitIndex(a, 0, a.length() * bits, b, 0, b.length() * bits));
    }

    @Test
    public void stringEquals()
    {
        final var x = KeyMap.string2view("ONE");
        final var y = KeyMap.string2view("ONE");
        assertTrue(AsciiKeyAnalyser.stringEquals(x, y));
    }

    @Test
    public void stringNotEquals()
    {
        final var x = KeyMap.string2view("ONE");
        final var y = KeyMap.string2view("TWO");
        assertFalse(AsciiKeyAnalyser.stringEquals(x, y));
    }
}