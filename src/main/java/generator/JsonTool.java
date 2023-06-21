package generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Path;

public class JsonTool
{
    private final Path outputDir;
    private final Schema schema;

    public JsonTool(final File schemaFile, final Path outputDir)
    {
        this.schema = parseSchema(schemaFile);
        this.outputDir = outputDir;
    }

    @SneakyThrows
    public static Schema parseSchema(final File file)
    {
        return new ObjectMapper().readValue(file, Schema.class);
    }

    static void writePackage(final Definition definition, final Writer writer)
    {
        writer.printf("package %s;", definition.packageName());
        writer.println();
        writer.println();
    }

    public static Path mkdirs(Path outputDir, Definition definition)
    {
        final Path path = outputDir.resolve(definition.packageName());
        boolean success = path
            .toFile()
            .mkdirs();
        assert success;
        return path;
    }
}
