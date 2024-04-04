package de;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.agrona.AsciiSequenceView;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import java.util.*;
import java.util.function.Function;

/**
 * This map contains string keys which are useful for struct parsing
 */
@Getter
@Accessors(fluent = true)
public class KeyMap<T>
{
    private final Map<AsciiSequenceView, T> map = new AsciiTrie<>();
    private final T emptyValue;

    public KeyMap()
    {
        this(Map.of(), null);
    }

    public KeyMap(final Map<String, T> baseMap, final T emptyValue)
    {
        this.emptyValue = emptyValue;
        baseMap.forEach((key, value) -> map.put(string2view(key), value));
    }

    public static AsciiSequenceView string2view(final String string)
    {
        final DirectBuffer buffer = new UnsafeBuffer();
        final byte[] bytes = string.getBytes();
        buffer.wrap(bytes);
        return new AsciiSequenceView(buffer, 0, bytes.length);
    }

    public static <T extends Enum<T>> KeyMap<T> forEnum(final Class<T> k)
    {
        final Map<String, T> map = new HashMap<>();
        for (final var e : k.getEnumConstants())
        {
            map.put(e.toString(), e);
        }
        return new KeyMap<>(map, null);
    }

    public T get(final AsciiSequenceView string)
    {
        return map.getOrDefault(string, emptyValue);
    }

    public T getNotEmpty(final AsciiSequenceView string)
    {
        final var result = get(string);
        if (Objects.equals(result, emptyValue))
        {
            throw new NoSuchElementException(string.toString());
        }
        return result;
    }

    /**
     * One can put any key only once. If you want to rewrite values for a particular key, then use mutable value like
     * {@link org.agrona.collections.MutableInteger}.
     */
    public void put(final AsciiSequenceView string, final T value)
    {
        final var v = map.get(string);
        assert v == emptyValue;
        map.put(deepCopy(string), value);
    }

    public T computeIfAbsent(final AsciiSequenceView string, final Function<AsciiSequenceView, T> getValue)
    {
        var value = map.get(string);
        if (!Objects.equals(value, emptyValue))
        {
            return value;
        }
        value = getValue.apply(string);
        assert !Objects.equals(value, emptyValue) : value + " " + emptyValue;
        put(string, value);
        return value;
    }

    public Collection<T> values()
    {
        return map.values();
    }

    public int size()
    {
        return map.size();
    }

    public static AsciiSequenceView deepCopy(final AsciiSequenceView str)
    {
        return string2view(str.toString());
    }
}
