package types;

import java.util.Set;

public abstract class FieldType
{
    /**
     * This is a type name without package prefix, as we use it in code. E.g. "boolean" or "ArrayList".
     */
    public abstract String name();

    /**
     * This method returns a list of classes that should be imported if we use this type. In case of primitives we
     * don't import anything. But if we have a mapped string or {@link de.Array}, then we will have to import something.
     */
    public abstract Set<String> imports();
}
