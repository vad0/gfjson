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
public class Message
{
    private String name;
    private String packageName;
    private boolean strictOrder;
    private List<Field> fields = new ArrayList<>();

    public Message addField(Field field)
    {
        fields.add(field);
        return this;
    }
}
