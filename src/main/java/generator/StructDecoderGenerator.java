package generator;

import de.JsonDecoder;
import de.KeyMap;
import lombok.SneakyThrows;
import org.agrona.AsciiSequenceView;

import java.io.File;
import java.nio.file.Path;
import java.util.StringJoiner;

public class StructDecoderGenerator
    implements AutoCloseable
{
    private final Schema schema;
    private final StructDefinition definition;
    private final Writer writer;

    public StructDecoderGenerator(final Schema schema, final Path outputDir, final String structName)
    {
        this.schema = schema;
        this.definition = schema.structByName(structName);
        final File file = JsonTool.mkdirs(outputDir, definition)
            .resolve(definition.decoderName() + ".java")
            .toFile();
        this.writer = new Writer(file);
    }

    public static void generate(final Schema schema, final Path outputDir, final String structName)
    {
        try (final var generator = new StructDecoderGenerator(schema, outputDir, structName))
        {
            generator.generateDecoder();
        }
    }

    @SneakyThrows
    public void generateDecoder()
    {
        JsonTool.writePackage(definition, writer);

        writeImports(definition, writer);

        writer.printf("public class %s", definition.decoderName());
        writer.startScope();

        writeFields(definition, writer);

        writeConstructor(definition, writer);

        writeParseSignature(definition, writer);
        writer.startScope();
        writer.printf("decoder.nextStartObject();\n\n");

        for (final var field : definition.fields())
        {
            parseField(field);
        }

        writer.printf("decoder.nextEndObject();\n");

        writer.endScope();

        writer.endScope();
    }

    private static void writeConstructor(final StructDefinition struct, final Writer writer)
    {
        final var sb = new StringJoiner(", ");
        for (final var field : struct.fields())
        {
            if (field.isMappedString())
            {
                sb.add("KeyMap<%s> %s".formatted(field.mappedClassSimpleName(), field.mapName()));
            }
        }
        writer.printf("public %sDecoder(%s)", struct.name(), sb);
        writer.startScope();
        for (final var field : struct.fields())
        {
            if (field.isMappedString())
            {
                writer.printf("this.%s = %s;\n", field.mapName(), field.mapName());
            }
        }
        writer.endScope();
        writer.println();
    }

    private  void parseField(final Field field)
    {
        writer.printf("decoder.checkKey(%s);\n", viewConstName(field));
        if (field.constant())
        {
            switch (field.type())
            {
                case STRING ->
                {
                    writer.printf("decoder.checkKey(%s);\n", expectedConstName(field));
                }
                default -> throw new RuntimeException("Not implemented constant field parsing for " + field.type());
            }
        }
        else if (field.ignored())
        {
            writer.printf("decoder.skipValue();\n");
        }
        else
        {
            switch (field.type())
            {
                case LONG ->
                {
                    writer.printf("struct.%s(decoder.nextLong());\n", field.name());
                }
                case QUOTED_DOUBLE ->
                {
                    writer.printf("struct.%s(decoder.nextDoubleFromString());\n", field.name());
                }
                case STRING ->
                {
                    writer.printf(
                        "struct.%s(%sMap.getKey(decoder.nextString()));\n",
                        field.name(),
                        field.name());
                }
                case ENUM ->
                {
                    final var enumDefinition = schema.enumByMappedClass(field.mappedClass());
                    writer.printf(
                        "struct.%s(%s.parse(decoder.nextString()));\n",
                        field.name(),
                        enumDefinition.decoderName());
                }
                default -> throw new RuntimeException("Not implemented non constant field parsing for " + field.type());
            }
        }
        writer.println();
    }

    private static void writeParseSignature(final StructDefinition struct, final Writer writer)
    {
        writer.printf("public void parse(JsonDecoder decoder, %s struct)", struct.name());
    }

    private static void writeFields(final StructDefinition struct, final Writer writer)
    {
        writeStaticFields(struct, writer);
        writeInstanceFields(struct, writer);
    }

    private static void writeInstanceFields(final StructDefinition struct, final Writer writer)
    {
        for (final var field : struct.fields())
        {
            if (field.isMappedString())
            {
                writer.printf("private final KeyMap<%s> %s;\n", field.mappedClassSimpleName(), field.mapName());
            }
        }
        writer.println();
    }

    private static void writeStaticFields(final StructDefinition struct, final Writer writer)
    {
        for (final var field : struct.fields())
        {
            writer.printf(
                "private static final %s %s = KeyMap.string2view(\"%s\");\n",
                AsciiSequenceView.class.getSimpleName(),
                viewConstName(field),
                field.name());
            if (field.constant())
            {
                switch (field.type())
                {
                    case STRING ->
                    {
                        writer.printf(
                            "private static final %s %s = KeyMap.string2view(\"%s\");\n",
                            AsciiSequenceView.class.getSimpleName(),
                            expectedConstName(field),
                            field.expected());
                    }
                    default -> throw new RuntimeException("Not implemented constant values for " + field.type());
                }
            }
        }
        writer.println();
    }

    private static void writeImports(final StructDefinition struct, final Writer writer)
    {
        writer.importClass(JsonDecoder.class);
        writer.importClass(KeyMap.class);
        writer.importClass(AsciiSequenceView.class);
        for (final var field : struct.fields())
        {
            if (field.isMappedString())
            {
                writer.printf("import %s;\n", field.mappedClass());
            }
        }
        writer.println();
        writer.println();
    }

    private static String viewConstName(final Field field)
    {
        return field.screamingSnakeName();
    }

    private static String expectedConstName(final Field field)
    {
        return "EXPECTED_" + field.screamingSnakeName();
    }

    @Override
    public void close()
    {
        writer.close();
    }
}
