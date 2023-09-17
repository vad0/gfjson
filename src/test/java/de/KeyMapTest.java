package de;

import org.agrona.collections.MutableInteger;
import org.apache.commons.collections4.trie.AsciiKeyAnalyser;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KeyMapTest
{
    @Test
    public void deepCopy()
    {
        final var a = KeyMap.string2view("asdf");
        final var b = KeyMap.deepCopy(a);
        assertTrue(AsciiKeyAnalyser.stringEquals(a, b));
        assertNotSame(a, b);
        final var c = KeyMap.string2view("sdfg");
        assertFalse(AsciiKeyAnalyser.stringEquals(a, c));
        assertFalse(AsciiKeyAnalyser.stringEquals(b, c));
    }

    @Test
    public void testPut()
    {
        final var map = new KeyMap<MutableInteger>(Map.of(), null);
        final var v1 = new MutableInteger(5);
        final var old1 = map.put(KeyMap.string2view("key"), v1);
        assertNull(old1);
        final var old2 = map.put(KeyMap.string2view("key"), new MutableInteger(3));
        assertEquals(v1, old2);
    }

    @Test
    public void forEnum()
    {
        final var actual = KeyMap.forEnum(ExampleEnum.class);
        final var expected = new KeyMap<>(
            Map.of("ONE", ExampleEnum.ONE, "TWO", ExampleEnum.TWO),
            null);
        check(actual, expected, "ONE");
        check(actual, expected, "TWO");
        check(actual, expected, "THREE");
    }

    private static void check(final KeyMap<ExampleEnum> actual, final KeyMap<ExampleEnum> expected, final String key)
    {
        final var k = KeyMap.string2view(key);
        assertEquals(expected.getKey(k), actual.getKey(k));
    }

    public enum ExampleEnum
    {
        ONE,
        TWO
    }
}