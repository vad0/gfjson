package generator;

import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class EnumGenerator
    implements AutoCloseable
{
    private final EnumDefinition definition;
    private final Writer writer;

    public EnumGenerator(final Schema schema, final Path outputDir, final String enumName)
    {
        this.definition = schema.enumByName(enumName);
        final File file = JsonTool.mkdirs(outputDir, definition)
            .resolve(enumName + ".java")
            .toFile();
        this.writer = new Writer(file);
    }

    public static void generate(final Schema schema, final Path outputDir, final String enumName)
    {
        try (final var generator = new EnumGenerator(schema, outputDir, enumName))
        {
            generator.generateEnum();
        }
    }

    @SneakyThrows
    public void generateEnum()
    {
        JsonTool.writePackage(definition, writer);

        writer.printf("public enum " + definition.name());
        writer.startScope();

        final List<String> values = definition.values();
        final int lastIndex = values.size() - 1;
        for (int i = 0; i < lastIndex; i++)
        {
            final var value = values.get(i);
            writer.printf("%s,\n", value);
        }
        writer.printf("%s;\n", values.get(lastIndex));

        writer.endScope();
    }

    @Override
    public void close()
    {
        writer.close();
    }
}
