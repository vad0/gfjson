package types;

import java.util.Set;

public class BooleanType
    extends FieldType
{
    @Override
    public String name()
    {
        return boolean.class.getSimpleName();
    }

    @Override
    public Set<String> imports()
    {
        return Set.of();
    }
}
