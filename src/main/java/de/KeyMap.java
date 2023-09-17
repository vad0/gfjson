package de;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.agrona.AsciiSequenceView;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.apache.commons.collections4.trie.AsciiTrie;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This map contains string keys which are useful for struct parsing
 */
@Getter
@Accessors(fluent = true)
public class KeyMap<T>
{
    private final Map<AsciiSequenceView, T> map = new AsciiTrie<>();
    private T emptyValue;

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
        return getKey(string);
    }

    public T getKey(final AsciiSequenceView string)
    {
        return map.getOrDefault(string, emptyValue);
    }

    public T put(final AsciiSequenceView string, final T value)
    {
        return map.put(string, value);
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
