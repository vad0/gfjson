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
public class EnumDefinition
    extends Definition
{
    private List<EnumValue> values = new ArrayList<>();
}
