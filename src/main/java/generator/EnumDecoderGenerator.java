package generator;

import de.KeyMap;
import org.agrona.AsciiSequenceView;

import java.io.File;
import java.nio.file.Path;

public class EnumDecoderGenerator
    implements AutoCloseable
{
    private final EnumDefinition definition;
    private final Writer writer;

    public EnumDecoderGenerator(final Schema schema, final Path outputDir, final String enumName)
    {
        this.definition = schema.enumByName(enumName);
        final File file = JsonTool.mkdirs(outputDir, definition)
            .resolve(definition.decoderName() + ".java")
            .toFile();
        this.writer = new Writer(file);
    }

    public static void generate(final Schema schema, final Path outputDir, final String enumName)
    {
        try (final var generator = new EnumDecoderGenerator(schema, outputDir, enumName))
        {
            generator.generateDecoder();
        }
    }

    private void generateDecoder()
    {
        JsonTool.writePackage(definition, writer);

        writeImports();

        writer.printf("public class %s", definition.decoderName());
        writer.startScope();

        writeStaticFields();

        writeParseMethod();

        writer.endScope();
    }

    private void writeParseMethod()
    {
        writer.printf("public static %s parse(AsciiSequenceView string)", definition.name());
        writer.startScope();
        writer.printf("return MAP.getKey(string);\n");
        writer.endScope();
    }

    private void writeStaticFields()
    {
        writer.printf(
            "private static final KeyMap<%s> MAP = KeyMap.forEnum(%s.class);",
            definition.name(),
            definition.name());
        writer.println();
        writer.println();
    }

    private void writeImports()
    {
        writer.importClass(KeyMap.class);
        writer.importClass(AsciiSequenceView.class);
        writer.println();
        writer.println();
    }

    @Override
    public void close()
    {
        writer.close();
    }
}
