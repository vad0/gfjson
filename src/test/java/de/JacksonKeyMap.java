package de;

import java.util.Map;

/**
 * This map contains string keys which are useful for struct parsing
 */
public class JacksonKeyMap<T>
{
    private final Map<String, T> map;
    private final T ignore;

    public JacksonKeyMap(final Map<String, T> baseMap, final T ignore)
    {
        this.ignore = ignore;
        map = baseMap;
    }

    public T get(final String string)
    {
        return map.getOrDefault(string, ignore);
    }
}
