package generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
public class Definition
{
    private String name;
    private String packageName;
    private boolean generate;

    public EnumDefinition asEnum()
    {
        return (EnumDefinition)this;
    }

    public StructDefinition asMessage()
    {
        return (StructDefinition)this;
    }
}
