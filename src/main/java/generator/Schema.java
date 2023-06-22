package generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * This class describes all structs and enums which should be generated or at least referenced in a generated code.
 */
@Data
@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
public class Schema
{
    private List<EnumDefinition> enums = new ArrayList<>();
    private List<StructDefinition> structs = new ArrayList<>();
    private List<String> arrays = new ArrayList<>();

    public Schema addEnum(final EnumDefinition definition)
    {
        enums.add(definition);
        return this;
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
        return enums()
            .stream()
            .filter(m -> m.fullClassName().equals(mappedClass))
            .findFirst()
            .orElseThrow();
    }
}
