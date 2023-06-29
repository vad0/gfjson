package types;

import generator.EnumDefinition;

import java.util.Set;

public class EnumType
    extends FieldType
{
    private final EnumDefinition enumDefinition;

    public EnumType(final EnumDefinition enumDefinition)
    {
        this.enumDefinition = enumDefinition;
    }

    @Override
    public String name()
    {
        return enumDefinition.name();
    }

    @Override
    public Set<String> imports()
    {
        return Set.of(enumDefinition.fullClassName());
    }
}
