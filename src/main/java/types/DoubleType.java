package types;

import java.util.Set;

public class DoubleType
    extends FieldType
{
    @Override
    public String name()
    {
        return double.class.getSimpleName();
    }

    @Override
    public Set<String> imports()
    {
        return Set.of();
    }
}
