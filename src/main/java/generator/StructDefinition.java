package generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
@EqualsAndHashCode(callSuper = true)
public class StructDefinition
    extends Definition
{
    private boolean strictOrder;
    private List<Field> fields = new ArrayList<>();

    public StructDefinition addField(final Field field)
    {
        fields.add(field);
        return this;
    }
}
