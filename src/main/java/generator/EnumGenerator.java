package generator;

import lombok.SneakyThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class EnumGenerator
    implements AutoCloseable
{
    private final EnumDefinition definition;
    private final Writer writer;

    public EnumGenerator(final Schema schema, final String outputDir, final String enumName)
    {
        this.definition = schema.enumByName(enumName);
        final Path dir = Paths.get(outputDir).resolve(definition.packageName());
        dir.toFile().mkdirs();
        this.writer = new Writer(dir.resolve(enumName + ".java").toFile());
    }

    public static void generate(final Schema schema, final String outputDir, final String enumName)
    {
        try (final var generator = new EnumGenerator(schema, outputDir, enumName))
        {
            generator.generateEnum();
        }
    }

    @SneakyThrows
    public void generateEnum()
    {
        Generator.writePackage(definition, writer);

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
