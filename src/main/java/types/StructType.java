package types;

import generator.StructDefinition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StructType
    extends FieldType
{
    private final StructDefinition structDefinition;
    private final List<FieldType> fieldTypes;

    public StructType(final StructDefinition structDefinition, final List<FieldType> fieldTypes)
    {
        this.structDefinition = structDefinition;
        this.fieldTypes = fieldTypes;
    }

    @Override
    public String name()
    {
        return structDefinition.name();
    }

    @Override
    public Set<String> imports()
    {
        final Set<String> res = new HashSet<>();
        for (final var fieldType : fieldTypes)
        {
            res.addAll(fieldType.imports());
        }
        return res;
    }
}
