package generator;

import de.*;
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

    private StructDecoderGenerator(final Schema schema, final Path outputDir, final StructDefinition definition)
    {
        this.schema = schema;
        this.definition = definition;
        final File file = JsonTool.mkdirs(outputDir, definition)
            .resolve(definition.decoderName() + ".java")
            .toFile();
        this.writer = new Writer(file);
    }

    public static void generate(final Schema schema, final Path outputDir, final String structName)
    {
        generate(schema, outputDir, schema.structByName(structName));
    }

    public static void generate(final Schema schema, final Path outputDir, final StructDefinition struct)
    {
        try (var generator = new StructDecoderGenerator(schema, outputDir, struct))
        {
            generator.generateDecoder();
        }
    }

    @SneakyThrows
    public void generateDecoder()
    {
        JsonTool.writePackage(definition, writer);

        writeImports(definition, writer);

        writer.printf("public class %s\n", definition.decoderName());
        writer.printf("    implements ParseArrayElement<Array<%s>>", definition.name());
        writer.startScope();

        writeFields();

        writeConstructor();

        writeParseMethod();
        writeFinishParsingMethod();
        writeParseElementMethod();

        writer.endScope();
    }

    private void writeParseMethod()
    {
        writer.printf("public void parse(final JsonDecoder decoder, final %s struct)", definition.name());
        writer.startScope();
        writer.printf("decoder.nextStartObject();\n");
        writer.printf("finishParsing(decoder, struct);\n");
        writer.endScope();
        writer.println();
    }

    private void writeFinishParsingMethod()
    {
        writer.printf("private void finishParsing(final JsonDecoder decoder, final %s struct)", definition.name());
        writer.startScope();
        for (final var field : definition.fields())
        {
            parseField(field);
        }
        writer.printf("decoder.nextEndObject();\n");
        writer.endScope();
        writer.println();
    }

    private void writeParseElementMethod()
    {
        writer.printf("@Override\n");
        writer.printf(
            "public void parseElement(final JsonDecoder jsonDecoder, final Array<%s> array, final Token firstToken)",
            definition.name());
        writer.startScope();
        writer.printf("Token.START_OBJECT.checkToken(firstToken);\n");
        writer.printf("final var struct = array.claimNext();\n");
        writer.printf("finishParsing(jsonDecoder, struct);\n");
        writer.endScope();
    }

    private void writeConstructor()
    {
        final var sb = new StringJoiner(", ");
        for (final var field : definition.fields())
        {
            if (field.isMappedString())
            {
                sb.add("final KeyMap<%s> %s".formatted(field.mappedClassSimpleName(), field.mapName()));
            }
        }
        final var args = sb.toString();
        if (args.isEmpty())
        {
            // We don't need to generate an empty constructor
            return;
        }
        writer.printf("public %sDecoder(%s)", definition.name(), args);
        writer.startScope();
        for (final var field : definition.fields())
        {
            if (field.isMappedString())
            {
                writer.printf("this.%s = %s;\n", field.mapName(), field.mapName());
            }
        }
        writer.endScope();
        writer.println();
    }

    private void parseField(final Field field)
    {
        writer.printf("decoder.checkKey(%s);\n", viewConstName(field));
        if (field.ignored())
        {
            writer.printf("decoder.skipValue();\n");
        }
        else if (field.constant())
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
        else
        {
            switch (field.type())
            {
                case LONG -> writer.printf("struct.%s(decoder.nextLong());\n", field.name());
                case QUOTED_DOUBLE -> writer.printf("struct.%s(decoder.nextDoubleFromString());\n", field.name());
                case STRING -> writer.printf(
                    "struct.%s(%sMap.getKey(decoder.nextString()));\n",
                    field.name(),
                    field.name());
                case ENUM ->
                {
                    final var enumDefinition = schema.enumByMappedClass(field.mappedClass());
                    writer.printf(
                        "struct.%s(%s.parse(decoder.nextString()));\n",
                        field.name(),
                        enumDefinition.decoderName());
                }
                case BOOLEAN -> writer.printf("struct.%s(decoder.nextBoolean());\n", field.name());
                default -> throw new RuntimeException("Not implemented non constant field parsing for " + field.type());
            }
        }
        writer.println();
    }

    private void writeFields()
    {
        writeStaticFields();
        writeInstanceFields();
    }

    private void writeInstanceFields()
    {
        for (final var field : definition.fields())
        {
            if (field.isMappedString())
            {
                writer.printf("private final KeyMap<%s> %s;\n", field.mappedClassSimpleName(), field.mapName());
            }
        }
        writer.println();
    }

    private void writeStaticFields()
    {
        for (final var field : definition.fields())
        {
            writer.printf(
                "private static final %s %s = KeyMap.string2view(\"%s\");\n",
                AsciiSequenceView.class.getSimpleName(),
                viewConstName(field),
                field.key());
            if (field.constant())
            {
                switch (field.type())
                {
                    case STRING -> writer.printf(
                        "private static final %s %s = KeyMap.string2view(\"%s\");\n",
                        AsciiSequenceView.class.getSimpleName(),
                        expectedConstName(field),
                        field.expected());
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
        writer.importClass(ParseArrayElement.class);
        writer.importClass(Token.class);
        writer.importClass(Array.class);
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
