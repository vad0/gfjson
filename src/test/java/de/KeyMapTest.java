package de;

import org.agrona.AsciiSequenceView;
import org.agrona.collections.MutableInteger;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.NoSuchElementException;

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
        map.put(KeyMap.string2view("key"), v1);
    }

    @Test
    public void testGetters()
    {
        final var map = new KeyMap<MutableInteger>();
        map.put(KeyMap.string2view("123"), new MutableInteger(1));
        assertEquals(1, map.size());
        assertEquals(1, map.values().size());
        assertEquals(1, map.map().values().size());
        assertNull(map.emptyValue());
        assertEquals(new MutableInteger(1), map.getNotEmpty(KeyMap.string2view("123")));
        assertThrows(NoSuchElementException.class, () -> map.getNotEmpty(KeyMap.string2view("23")));
    }

    @Test
    public void computeIfAbsent()
    {
        final var map = new KeyMap<String>();
        final String string = "test";
        for (int i = 0; i < 2; i++)
        {
            final var value = map.computeIfAbsent(KeyMap.string2view(string), AsciiSequenceView::toString);
            assertEquals(string, value);
        }
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
        assertEquals(expected.get(k), actual.get(k));
    }

    public enum ExampleEnum
    {
        ONE,
        TWO
    }
}