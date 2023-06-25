package generator;

import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public final class StructGenerator
    implements AutoCloseable
{
    private final Schema schema;
    private final StructDefinition definition;
    private final Writer writer;

    private StructGenerator(final Schema schema, final Path outputDir, final StructDefinition definition)
    {
        this.schema = schema;
        this.definition = definition;
        final File file = JsonTool.mkdirs(outputDir, definition)
            .resolve(definition.name() + ".java")
            .toFile();
        this.writer = new Writer(file);
    }

    public static void generate(final Schema schema, final Path outputDir, final StructDefinition definition)
    {
        try (var generator = new StructGenerator(schema, outputDir, definition))
        {
            generator.generateStruct();
        }
    }

    @SneakyThrows
    public void generateStruct()
    {
        JsonTool.writePackage(definition, writer);

        writeImports(definition, writer);

        writer.writeJavadoc(definition);
        writer.signature("public class %s", definition.name());

        writeFields();
        // constructor is not needed
        writeGetters();
        writeSetters();
        writeEquals();

        writer.println();
        writeHashCode();

        writer.println();
        writeToString();

        writer.endScope();
    }

    private void writeFields()
    {
        for (final var field : definition.fields())
        {
            if (!field.isGenerated())
            {
                continue;
            }
            writer.writeJavadoc(field);
            writer.println("private %s %s;", field.getFieldType(), field.name());
        }
        writer.println();
    }

    private void writeGetters()
    {
        for (final var field : definition.fields())
        {
            if (!field.isGenerated())
            {
                continue;
            }
            writer.signature("public %s %s()", field.getFieldType(), field.name());
            writer.println("return %s;", field.name());
            writer.endScope();
            writer.println();
        }
    }

    private void writeSetters()
    {
        for (final var field : definition.fields())
        {
            if (!field.isGenerated())
            {
                continue;
            }
            writer.signature(
                "public %s %s(final %s %s)",
                definition.name(),
                field.name(),
                field.getFieldType(),
                field.name());
            writer.println("this.%s = %s;", field.name(), field.name());
            writer.println("return this;");
            writer.endScope();
            writer.println();
        }
    }

    private void writeEquals()
    {
        writer.println("@Override");
        writer.signature("public boolean equals(final Object o)");

        writer.signature("if (o == this)");
        writer.println("return true;");
        writer.endScope();

        writer.signature("if (!(o instanceof final %s other))", definition.name());
        writer.println("return false;");
        writer.endScope();

        for (final var field : definition.fields())
        {
            if (!field.isGenerated())
            {
                continue;
            }
            switch (field.type())
            {
                case QUOTED_DOUBLE -> writer.signature(
                    "if (Double.compare(%s(), other.%s()) != 0)",
                    field.name(),
                    field.name());
                case BOOLEAN, LONG, ENUM -> writer.signature(
                    "if (%s() != other.%s())",
                    field.name(),
                    field.name());
                default ->
                {
                    assert field.isMappedString() : field;
                    writer.signature(
                        "if (!Objects.equals(%s(), other.%s()))",
                        field.name(),
                        field.name());
                }
            }
            writer.println("return false;");
            writer.endScope();
        }
        writer.println("return true;");
        writer.endScope();
    }

    private void writeHashCode()
    {
        writer.println("@Override");
        writer.signature("public int hashCode()");
        writer.println("final int prime = 59;");
        writer.println("int result = 1;");
        for (final var field : definition.fields())
        {
            if (!field.isGenerated())
            {
                continue;
            }
            final String str = switch (field.type())
                //CHECKSTYLE:OFF
            {
                //CHECKSTYLE:ON
                case BOOLEAN -> "Boolean.hashCode(%s)".formatted(field.name());
                case ENUM -> "Objects.hashCode(%s)".formatted(field.name());
                case LONG -> "Long.hashCode(%s)".formatted(field.name());
                case QUOTED_DOUBLE -> "Double.hashCode(%s)".formatted(field.name());
                default ->
                {
                    assert field.isMappedString();
                    yield "Objects.hashCode(%s)".formatted(field.name());
                }
            };
            writer.println("result = result * prime + %s;", str);
        }
        writer.println("return result;");
        writer.endScope();
    }

    private void writeToString()
    {
        writer.println("@Override");
        writer.signature("public String toString()");
        boolean isFirst = true;
        for (final var field : definition.fields())
        {
            if (!field.isGenerated())
            {
                continue;
            }
            final String prefix;
            if (isFirst)
            {
                prefix = "return \"%s(".formatted(definition.name());
                isFirst = false;
            }
            else
            {
                prefix = "    \", ";
            }
            writer.println("%s%s=\" + %s() +", prefix, field.name(), field.name());
        }
        writer.println("    \")\";");
        writer.endScope();
    }

    private static void writeImports(final StructDefinition struct, final Writer writer)
    {
        writer.importMappedClasses(struct);
        writer.println();
        writer.importClass(Objects.class);
        writer.println();
        writer.println();
    }

    @Override
    public void close()
    {
        writer.close();
    }
}
