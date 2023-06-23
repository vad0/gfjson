package generator;

import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public final class EnumGenerator
    implements AutoCloseable
{
    private final EnumDefinition definition;
    private final Writer writer;

    private EnumGenerator(final Path outputDir, final EnumDefinition definition)
    {
        this.definition = definition;
        final File file = JsonTool.mkdirs(outputDir, definition)
            .resolve(definition.name() + ".java")
            .toFile();
        this.writer = new Writer(file);
    }

    public static void generate(final Path outputDir, final EnumDefinition definition)
    {
        try (var generator = new EnumGenerator(outputDir, definition))
        {
            generator.generateEnum();
        }
    }

    /**
     *
     */
    @SneakyThrows
    public void generateEnum()
    {
        JsonTool.writePackage(definition, writer);

        writer.writeJavadoc(definition);
        writer.printf("public enum " + definition.name());
        writer.startScope();

        final List<EnumValue> values = definition.values();
        final int lastIndex = values.size() - 1;
        for (int i = 0; i < lastIndex; i++)
        {
            final EnumValue value = values.get(i);
            writeEnumValue(value, ",");
        }
        writeEnumValue(values.get(lastIndex), ";");

        writer.endScope();
    }

    private void writeEnumValue(final EnumValue value, final String separator)
    {
        writer.writeJavadoc(value);
        writer.printf("%s%s\n", value.name(), separator);
    }

    @Override
    public void close()
    {
        writer.close();
    }
}
