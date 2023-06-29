package types;

import de.Array;

import java.util.HashSet;
import java.util.Set;

public class ArrayType
    extends FieldType
{
    private final FieldType baseType;

    public ArrayType(final FieldType baseType)
    {
        this.baseType = baseType;
    }

    @Override
    public String name()
    {
        return "Array<" + baseType.name() + ">";
    }

    @Override
    public Set<String> imports()
    {
        final Set<String> res = new HashSet<>();
        res.add(Array.class.getCanonicalName());
        res.addAll(baseType.imports());
        return res;
    }
}
