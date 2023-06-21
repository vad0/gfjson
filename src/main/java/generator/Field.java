package generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
public class Field
{
    private String key;
    private String name;
    private Type type;
    private boolean constant;
    private boolean ignored;
    private String description;
    private Object expected;
    private String mappedClass;
    private Object details;

    /**
     * Function to convert camel case string to snake case string
     */
    public static String camelToSnake(final String str)
    {
        // Empty String
        StringBuilder result = new StringBuilder();

        // Append first character(in lower case)
        // to result string
        final char c = str.charAt(0);
        result.append(Character.toLowerCase(c));

        // Traverse the string from
        // ist index to last index
        for (int i = 1; i < str.length(); i++)
        {
            final char ch = str.charAt(i);

            // Check if the character is an upper case then append '_' and such character (in lower case) to result
            // string
            if (Character.isUpperCase(ch))
            {
                result.append('_').append(Character.toLowerCase(ch));
            }
            else
            {
                // If the character is lower case then add such character into result string
                result.append(ch);
            }
        }
        return result.toString();
    }

    boolean isMappedString()
    {
        return type() == Type.STRING && !constant() && !ignored();
    }

    public String screamingSnakeName()
    {
        return camelToSnake(name()).toUpperCase();
    }

    public String mapName()
    {
        return name + "Map";
    }

    public String mappedClassSimpleName()
    {
        final String[] split = mappedClass.split("\\.");
        return split[split.length - 1];
    }

    public enum Type
    {
        STRING(String.class),
        LONG(long.class),
        QUOTED_DOUBLE(double.class);

        private final Class<?> type;

        Type(final Class<?> type)
        {
            this.type = type;
        }

        public String typeName()
        {
            return type.getSimpleName();
        }
    }
}
