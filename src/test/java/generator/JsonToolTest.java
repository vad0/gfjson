package generator;

import org.junit.jupiter.api.Test;
import utils.TestUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonToolTest
{
    public static final Path DEBUG_DIR = Path.of(System.getProperty("user.dir"), "build/generated/sources/gfjson");

    @Test
    public void testParseSchema()
    {
        final var file = TestUtils.getResourcePath("generator/schema.json").toFile();
        final var schema = Schema.read(file);

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
                .asEnum())
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
                .addField(new Field()
                    .key("t")
                    .name("timeInForce")
                    .type(Field.Type.ENUM)
                    .mappedClass("md.TimeInForce"))
                .addField(new Field()
                    .key("bo")
                    .name("isFast")
                    .type(Field.Type.BOOLEAN))
            );

        assertEquals(expected, schema);
    }

    @Test
    public void wrongPath()
    {
        assertThrows(
            FileNotFoundException.class,
            () -> Schema.read(new File("non existent path")));
    }
}