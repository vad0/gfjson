package generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Path;

/**
 * This class is the main class in a jar which generates code.
 * Here we read {@link Schema} and generate all classes according to it.
 */
public class JsonTool
{
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

    public static Path mkdirs(final Path outputDir, final Definition definition)
    {
        final Path path = outputDir.resolve(definition.packageName());
        final File file = path.toFile();
        if (!file.exists())
        {
            final boolean success = file.mkdirs();
            assert success;
        }
        return path;
    }
}
