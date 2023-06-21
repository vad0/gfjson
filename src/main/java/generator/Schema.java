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
    private List<Message> messages = new ArrayList<>();

    public Schema addMessage(final Message message)
    {
        messages.add(message);
        return this;
    }

    public Message getByName(final String messageName)
    {
        return messages()
            .stream()
            .filter(m -> m.name().equals(messageName))
            .findFirst()
            .orElseThrow();
    }
}
