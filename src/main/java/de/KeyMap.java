package de;

import org.agrona.AsciiSequenceView;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.apache.commons.collections4.trie.AsciiTrie;

import java.util.HashMap;
import java.util.Map;

/**
 * This map contains string keys which are useful for struct parsing
 */
public class KeyMap<T>
{
    private final Map<AsciiSequenceView, T> map = new AsciiTrie<>();
    private final T ignore;

    public KeyMap(final Map<String, T> baseMap, final T ignore)
    {
        this.ignore = ignore;
        baseMap.forEach((key, value) -> map.put(string2view(key), value));
    }

    public static AsciiSequenceView string2view(final String string)
    {
        final DirectBuffer buffer = new UnsafeBuffer();
        final byte[] bytes = string.getBytes();
        buffer.wrap(bytes);
        final AsciiSequenceView result = new AsciiSequenceView();
        result.wrap(buffer, 0, bytes.length);
        return result;
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

    public T getKey(final AsciiSequenceView string)
    {
        return map.getOrDefault(string, ignore);
    }
}
