package de;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * This array increases its capacity when needed. It never nullifies its elements. The workflow should be as follows.
 * Call {@link #init()}.
 * Then get elements from the array by repeatedly calling {@link #claimNext()}.
 * Then use these elements, but don't store links to them in any places.
 * <p>
 * When the next message arrives repeat this procedure. Note that you will get exact same objects as before, but now
 * they will have new contents.
 */
public class Array<T>
{
    private final ArrayList<T> array = new ArrayList<>();
    private final Supplier<T> constructor;
    private int size;

    public Array(final Supplier<T> constructor)
    {
        this.constructor = constructor;
    }

    /**
     * This method should be called when we parse a message and want to add a new element. This method returns the next
     * object to fill. Ownership of it is not returned.
     */
    public T claimNext()
    {
        if (array.size() == size)
        {
            array.add(constructor.get());
        }
        return array.get(size++);
    }

    /**
     * This method is called when we already parsed a message.
     */
    public T get(final int index)
    {
        assert index < size;
        return array.get(index);
    }

    public void init()
    {
        size = 0;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final var other = (Array<T>)o;
        if (size != other.size)
        {
            return false;
        }
        for (int i = 0; i < size; i++)
        {
            if (!get(i).equals(other.get(i)))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return Integer.hashCode(size);
    }
}
