package de;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyMapTest
{
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