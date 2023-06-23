package generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
@NoArgsConstructor
@AllArgsConstructor
public class EnumValue
    implements HasJavadoc
{
    private String name;
    private String javadoc;

    public EnumValue(final String name)
    {
        this.name = name;
    }
}
