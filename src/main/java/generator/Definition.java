package generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * This is a parent class for description of generated structs and enums.
 */
@Data
@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
public class Definition
    implements HasJavadoc
{
    private String name;
    private String packageName;
    private List<Generate> generate;
    private String javadoc;

    public boolean generatePojo()
    {
        return generate.contains(Generate.POJO);
    }

    public boolean generateDecoder()
    {
        return generate.contains(Generate.DECODER);
    }

    public String fullClassName()
    {
        return packageName + '.' + name;
    }

    public EnumDefinition asEnum()
    {
        return (EnumDefinition)this;
    }

    public StructDefinition asStruct()
    {
        return (StructDefinition)this;
    }

    public String decoderName()
    {
        return name() + "Decoder";
    }
}
