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
