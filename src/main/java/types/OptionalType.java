package types;

import java.util.Set;

/**
 * This is a wrapper around a bse type which assumes that underlying value can be null.
 */
public class OptionalType
    extends FieldType
{
    private final FieldType baseType;

    public OptionalType(final FieldType baseType)
    {
        this.baseType = baseType;
    }

    @Override
    public String name()
    {
        return baseType.name();
    }

    @Override
    public Set<String> imports()
    {
        return baseType.imports();
    }
}
