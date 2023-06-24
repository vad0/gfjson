package generator;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utils.TestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StructGeneratorTest
{
    @TempDir
    public Path tempDir;

    @SneakyThrows
    @Test
    public void generatePojo()
    {
        final var file = TestUtils.getResourcePath("generator/schema.json").toFile();
        final var schema = Schema.read(file);
        final var outputDir = tempDir;
        StructGenerator.generate(schema, outputDir, schema.structByName("L1Update"));

        final String expected = TestUtils.readFile(Path.of("generator/expected_l1_update.txt").toString());
        final String actual = Files.readString(outputDir.resolve("md/L1Update.java"));
        assertEquals(expected, actual);
    }
}