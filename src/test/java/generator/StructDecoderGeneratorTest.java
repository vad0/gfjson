package generator;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utils.TestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StructDecoderGeneratorTest
{
    @TempDir
    public Path tempDir;

    @Test
    public void testParseSchema()
    {
        final var file = TestUtils.getResourcePath("generator/schema.json").toFile();
        final var schema = JsonTool.parseSchema(file);

        final var expected = new Schema()
            .addEnum(new EnumDefinition()
                .name("OrderType")
                .packageName("md")
                .generate(true)
                .asEnum()
                .values(List.of("LIMIT", "MARKET", "MARKET_LIMIT")))
            .addEnum(new EnumDefinition()
                .name("TimeInForce")
                .packageName("md")
                .asEnum()
                .values(List.of("IOC", "GTC", "FOK")))
            .addMessage(new StructDefinition()
                .name("L1Update")
                .packageName("md")
                .asMessage()
                .strictOrder(true)
                .addField(new Field()
                    .key("e")
                    .name("eventType")
                    .type(Field.Type.STRING)
                    .constant(true)
                    .description("event type, e.g. bookTicker")
                    .expected("bookTicker"))
                .addField(new Field().key("u")
                    .name("updateId")
                    .type(Field.Type.LONG)
                    .description("order book updateId"))
                .addField(new Field()
                    .key("b")
                    .name("bestBidPrice")
                    .type(Field.Type.QUOTED_DOUBLE)
                    .description("best bid price"))
                .addField(new Field()
                    .key("s")
                    .name("symbol")
                    .type(Field.Type.STRING)
                    .mappedClass("org.agrona.collections.MutableInteger"))
                .addField(new Field()
                    .key("x")
                    .name("error")
                    .type(Field.Type.STRING)
                    .ignored(true))
            );

        assertEquals(expected, schema);
    }

    @SneakyThrows
    @Test
    public void generateDecoder()
    {
        final var file = TestUtils.getResourcePath("generator/schema.json").toFile();
        final var schema = JsonTool.parseSchema(file);
        final var debugDir = Path.of(System.getProperty("user.dir"), "build/generated/sources/gfjson");
        final var outputDir = tempDir;
        StructDecoderGenerator.generate(schema, outputDir, "L1Update");

        final String expected = TestUtils.readFile(Path.of("generator/expected_l1_decoder.txt").toString());
        final String actual = Files.readString(outputDir.resolve("md/L1UpdateDecoder.java"));
        assertEquals(expected, actual);
    }
}