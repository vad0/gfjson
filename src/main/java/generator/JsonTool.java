package generator;

import java.io.File;
import java.nio.file.Path;

/**
 * This class is the main class in a jar which generates code.
 * Here we read {@link Schema} and generate all classes according to it.
 */
public class JsonTool
{
    public static void main(final String[] args)
    {
        final Schema schema = getSchema();
        final Path outputDir = getOutputDir();
        schema.generateEnums(outputDir);
        schema.generateStructs(outputDir);
    }

    private static Schema getSchema()
    {
        final String path = System.getProperty("schemaPath");
        if (path == null)
        {
            throw new RuntimeException("Missing schema path: -DschemaPath=...");
        }
        final var file = new File(path);
        if (!file.exists() || !file.isFile())
        {
            throw new RuntimeException("Schema not found at location: " + file.getAbsolutePath());
        }
        return Schema.read(file);
    }

    private static Path getOutputDir()
    {
        final String outputPath = System.getProperty("outputDir");
        if (outputPath == null)
        {
            throw new RuntimeException("Missing output dir: -DoutputDir=...");
        }
        return Path.of(outputPath);
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
