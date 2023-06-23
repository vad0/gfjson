package generator;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

/**
 * This class is the main class in a jar which generates code.
 * Here we read {@link Schema} and generate all classes according to it.
 * Run with the following (example) command:
 * java -cp build/libs/gfjson-1.0-all.jar \
 * -DschemaPath=src/test/resources/generator/schema.json \
 * -DoutputDir=build/generated/sources/gfjson \
 * generator.JsonTool
 */
public class JsonTool
{
    static final String SCHEMA_PATH = "schemaPath";
    static final String OUTPUT_DIR = "outputDir";

    public static void main(final String[] args)
    {
        final Schema schema = getSchema();
        final Path outputDir = getOutputDir();
        schema.generateEnums(outputDir);
        schema.generateStructs(outputDir);
    }

    @SneakyThrows
    static Schema getSchema()
    {
        final String path = System.getProperty(SCHEMA_PATH);
        if (path == null)
        {
            throw new FileNotFoundException("Missing schema path: -DschemaPath=...");
        }
        final var file = new File(path);
        if (!file.exists() || !file.isFile())
        {
            throw new FileNotFoundException("Schema not found at location: " + file.getAbsolutePath());
        }
        return Schema.read(file);
    }

    @SneakyThrows
    static Path getOutputDir()
    {
        final String outputPath = System.getProperty(OUTPUT_DIR);
        if (outputPath == null)
        {
            throw new FileNotFoundException("Missing output dir: -DoutputDir=...");
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
