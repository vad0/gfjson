package de;

import org.agrona.collections.MutableInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayTest
{
    @Test
    public void testAdd()
    {
        final var array = new Array<>(MutableInteger::new);
        assertThrows(AssertionError.class, () -> array.get(0));

        final int n = 3;
        for (int i = 0; i < n; i++)
        {
            array.claimNext();
        }

        for (int i = 0; i < 3; i++)
        {
            array.get(i);
        }

        array.init();
        assertThrows(AssertionError.class, () -> array.get(0));
    }

    @Test
    public void testHashCode()
    {
        final var array = new Array<>(MutableInteger::new);
        assertEquals(Integer.hashCode(0), array.hashCode());
    }

    @Test
    public void testEquals1()
    {
        final var array = new Array<>(MutableInteger::new);
        array.claimNext().set(10);
        assertEquals(1, array.size());
        assertEquals(array, array, array.toString());
        assertFalse(array.equals(null));
        assertFalse(array.equals(new MutableInteger(5)));

        final var other = new Array<>(MutableInteger::new);
        assertNotEquals(array, other);

        other.claimNext().increment();
        assertNotEquals(array, other);

        other.init();
        other.claimNext().set(10);
        assertEquals(array, other);
    }
}