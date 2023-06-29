package types;

import java.util.Set;

public class LongType
    extends FieldType
{
    @Override
    public String name()
    {
        return long.class.getSimpleName();
    }

    @Override
    public Set<String> imports()
    {
        return Set.of();
    }
}
