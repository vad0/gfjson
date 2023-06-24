package generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * This class describes a field in a struct: name, type, etc. Not all fields are mandatory.
 */
@Data
@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
public class Field
    implements HasJavadoc
{
    private String key;
    private String name;
    private Type type;
    private boolean constant;
    private boolean ignored;
    private String javadoc;
    private Object expected;
    private String mappedClass;
    private Object details;

    String getFieldType()
    {
        switch (type())
        {
            case BOOLEAN ->
            {
                return boolean.class.getSimpleName();
            }
            case LONG ->
            {
                return long.class.getSimpleName();
            }
            case QUOTED_DOUBLE ->
            {
                return double.class.getSimpleName();
            }
            case ENUM ->
            {
                return mappedClassSimpleName();
            }
        }
        assert isMappedString() : this;
        return mappedClassSimpleName();
    }

    public String expectedString()
    {
        return String.valueOf(expected);
    }

    /**
     * Function to convert camel case string to snake case string
     */
    public static String camelToSnake(final String str)
    {
        final var result = new StringBuilder();

        // Append first character(in lower case) to result string
        final char c = str.charAt(0);
        result.append(Character.toLowerCase(c));

        // Traverse the string from ist index to last index
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
        return type() == Type.STRING && isGenerated();
    }

    boolean isGenerated()
    {
        return !constant() && !ignored();
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
        BOOLEAN,
        ENUM,
        STRING,
        LONG,
        QUOTED_DOUBLE;
    }
}
