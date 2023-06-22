package md;

import generator.JsonTool;
import generator.StructDecoderGenerator;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utils.TestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SizePriceDecoderTest
{
    @TempDir
    public Path tempDir;

    @SneakyThrows
    @Test
    public void testDecoder()
    {
        final var file = TestUtils.getResourcePath("generator/array_schema.json").toFile();
        final var schema = JsonTool.parseSchema(file);
        final var outputDir = tempDir;
        StructDecoderGenerator.generate(schema, outputDir, "SizePrice");

        final String expected = TestUtils.readFile(Path.of("md/expected_size_price_decoder.txt").toString());
        final String actual = Files.readString(outputDir.resolve("md/SizePriceDecoder.java"));
        assertEquals(expected, actual);
    }
}
