package generator;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utils.TestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StructDecoderGeneratorTest
{
    @TempDir
    public Path tempDir;

    @SneakyThrows
    @Test
    public void generateDecoder()
    {
        final var file = TestUtils.getResourcePath("generator/schema.json").toFile();
        final var schema = JsonTool.parseSchema(file);
        final var debugDir = Path.of(System.getProperty("user.dir"), "build/generated/sources/gfjson");
        final var outputDir = tempDir;
        StructDecoderGenerator.generate(schema, outputDir, "L1Update");

        final String expected = TestUtils.readFile(Path.of("generator/expected_l1_update_decoder.txt").toString());
        final String actual = Files.readString(outputDir.resolve("md/L1UpdateDecoder.java"));
        assertEquals(expected, actual);
    }
}