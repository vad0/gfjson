package generator;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utils.TestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnumDecoderGeneratorTest
{
    @TempDir
    public Path tempDir;

    @SneakyThrows
    @Test
    public void generateOrderTypeDecoder()
    {
        final var file = TestUtils.getResourcePath("generator/schema.json").toFile();
        final var schema = JsonTool.parseSchema(file);
        final var outputDir = tempDir;
        EnumDecoderGenerator.generate(schema, outputDir, "OrderType");

        final String expected = TestUtils.readFile(Path.of("generator/expected_order_type_decoder.txt").toString());
        final String actual = Files.readString(outputDir.resolve("md/OrderTypeDecoder.java"));
        assertEquals(expected, actual);
    }
}