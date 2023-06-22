package generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * This class describes some class: its name, package, fields and attributed needed for code generation.
 */
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
