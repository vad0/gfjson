package generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
public class Schema
{
    private List<EnumDefinition> enums = new ArrayList<>();
    private List<StructDefinition> structs = new ArrayList<>();

    public Schema addEnum(final Definition definition)
    {
        return addEnum((EnumDefinition)definition);
    }

    public Schema addEnum(final EnumDefinition definition)
    {
        enums.add(definition);
        return this;
    }

    public Schema addMessage(final Definition message)
    {
        return addMessage((StructDefinition)message);
    }

    public Schema addMessage(final StructDefinition structDefinition)
    {
        structs.add(structDefinition);
        return this;
    }

    public StructDefinition structByName(final String messageName)
    {
        return structs()
            .stream()
            .filter(m -> m.name().equals(messageName))
            .findFirst()
            .orElseThrow();
    }

    public EnumDefinition enumByName(final String enumName)
    {
        return enums()
            .stream()
            .filter(m -> m.name().equals(enumName))
            .findFirst()
            .orElseThrow();
    }

    public EnumDefinition enumByMappedClass(final String mappedClass)
    {
        final int lastDot = mappedClass.lastIndexOf('.');
        final String packageName = mappedClass.substring(0, lastDot);
        final String simpleClassName = mappedClass.substring(lastDot + 1);
        return enums()
            .stream()
            .filter(m -> m.packageName().equals(packageName))
            .filter(m -> m.name().equals(simpleClassName))
            .findFirst()
            .orElseThrow();
    }
}
