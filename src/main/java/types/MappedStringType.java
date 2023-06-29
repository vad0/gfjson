package types;

import java.util.Objects;
import java.util.Set;

public class MappedStringType
    extends FieldType
{
    /**
     * Package can be null if a string is mapped to a primitive type.
     */
    private final String packageName;
    /**
     * This field contains either name of a primitive type ("boolean") or a class name ("ArrayList").
     */
    private final String className;

    public MappedStringType(final String packageName, final String className)
    {
        this.packageName = packageName;
        this.className = Objects.requireNonNull(className);
    }

    private boolean isPrimitive()
    {
        return packageName == null;
    }

    @Override
    public String name()
    {
        return className;
    }

    @Override
    public Set<String> imports()
    {
        if (isPrimitive())
        {
            return Set.of();
        }
        return Set.of(packageName + "." + className);
    }
}
