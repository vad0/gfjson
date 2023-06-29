package types;

import java.util.Set;

public class StringType
    extends FieldType
{
    @Override
    public String name()
    {
        return String.class.getSimpleName();
    }

    @Override
    public Set<String> imports()
    {
        return Set.of();
    }
}
