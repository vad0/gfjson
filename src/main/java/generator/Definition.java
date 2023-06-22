package generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * This is a parent class for description of generated structs and enums.
 */
@Data
@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
public class Definition
{
    private String name;
    private String packageName;
    private boolean generate;

    public String fullClassName()
    {
        return packageName + '.' + name;
    }

    public EnumDefinition asEnum()
    {
        return (EnumDefinition)this;
    }

    public StructDefinition asMessage()
    {
        return (StructDefinition)this;
    }

    public String decoderName()
    {
        return name() + "Decoder";
    }
}
