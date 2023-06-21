package generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.JsonDecoder;
import de.KeyMap;
import lombok.SneakyThrows;
import org.agrona.AsciiSequenceView;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringJoiner;

public class Generator
{
    private final String outputDir;
    private final Schema schema;

    public Generator(final File schemaFile, final String outputDir)
    {
        this.schema = parseSchema(schemaFile);
        this.outputDir = outputDir;
    }

    @SneakyThrows
    public static Schema parseSchema(final File file)
    {
        return new ObjectMapper().readValue(file, Schema.class);
    }

    @SneakyThrows
    public void generateDecoder(final String messageName)
    {
        final var message = schema.getByName(messageName);
        final Path dir = Paths.get(outputDir).resolve(message.packageName());
        dir.toFile().mkdirs();
        final var className = messageName + "Decoder";
        try (final var writer = new Writer(dir.resolve(className + ".java").toFile()))
        {
            writePackage(message, writer);

            writeImports(message, writer);

            writer.printf("public class %s", className);
            writer.startScope();

            writeFields(message, writer);

            writeConstructor(message, writer);

            writeParseSignature(message, writer);
            writer.startScope();
            writer.printf("decoder.nextStartObject();\n\n");

            for (final var field : message.fields())
            {
                parseField(writer, field);
            }

            writer.printf("decoder.nextEndObject();\n");

            writer.endScope();

            writer.endScope();
        }
    }

    private void writeConstructor(Message message, Writer writer)
    {
        final var sb = new StringJoiner(", ");
        for (final var field : message.fields())
        {
            if (field.isMappedString())
            {
                sb.add("KeyMap<%s> %s".formatted(field.mappedClassSimpleName(), field.mapName()));
            }
        }
        writer.printf("public %sDecoder(%s)", message.name(), sb);
        writer.startScope();
        for (final var field : message.fields())
        {
            if (field.isMappedString())
            {
                writer.printf("this.%s = %s;\n", field.mapName(), field.mapName());
            }
        }
        writer.endScope();
        writer.println();
    }

    private static void parseField(Writer writer, Field field)
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
        else
        {
            switch (field.type())
            {
                case LONG ->
                {
                    writer.printf("message.%s(decoder.nextLong());\n", field.name());
                }
                case QUOTED_DOUBLE ->
                {
                    writer.printf("message.%s(decoder.nextDoubleFromString());\n", field.name());
                }
                case STRING ->
                {
                    writer.printf(
                        "message.%s(%sMap.getKey(decoder.nextString()));\n",
                        field.name(),
                        field.name());
                }
                default -> throw new RuntimeException("Not implemented non constant field parsing for " + field.type());
            }
        }
        writer.println();
    }

    private static void writeParseSignature(final Message message, final Writer writer)
    {
        writer.printf("public void parse(JsonDecoder decoder, %s message)", message.name());
    }

    private static void writeFields(final Message message, final Writer writer)
    {
        writeStaticFields(message, writer);
        writeInstanceFields(message, writer);
    }

    private static void writeInstanceFields(Message message, Writer writer)
    {
        for (final var field : message.fields())
        {
            if (field.isMappedString())
            {
                writer.printf("private final KeyMap<%s> %s;\n", field.mappedClassSimpleName(), field.mapName());
            }
        }
        writer.println();
    }

    private static void writeStaticFields(Message message, Writer writer)
    {
        for (final var field : message.fields())
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

    private static void writeImports(Message message, final Writer writer)
    {
        writer.importClass(JsonDecoder.class);
        writer.importClass(KeyMap.class);
        writer.importClass(AsciiSequenceView.class);
        for (final var field : message.fields())
        {
            if (field.isMappedString())
            {
                writer.printf("import %s;\n", field.mappedClass());
            }
        }
        writer.println();
        writer.println();
    }

    private static void writePackage(final Message message, final Writer writer)
    {
        writer.printf("package %s;", message.packageName());
        writer.println();
        writer.println();
    }

    private static String stringConstName(final Field field)
    {
        return field.screamingSnakeName() + "_STR";
    }

    private static String viewConstName(final Field field)
    {
        return field.screamingSnakeName();
    }

    private static String expectedConstName(final Field field)
    {
        return "EXPECTED_" + field.screamingSnakeName();
    }
}
