package generator;

import org.junit.jupiter.api.Test;
import utils.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeneratorTest
{
    @Test
    public void testParseSchema()
    {
        final var file = TestUtils.getResourcePath("generator/schema.json").toFile();
        final var schema = Generator.parseSchema(file);

        final var expected = new Schema();
        expected.addMessage(new Message()
            .name("L1Update")
            .packageName("md")
            .strictOrder(true)
            .addField(new Field()
                .key("e")
                .name("eventType")
                .type(Field.Type.STRING)
                .constant(true)
                .description("event type, e.g. bookTicker")
                .expected("bookTicker"))
            .addField(new Field().key("u").name("updateId").type(Field.Type.LONG).description("order book updateId"))
            .addField(new Field()
                .key("b")
                .name("bestBidPrice")
                .type(Field.Type.QUOTED_DOUBLE)
                .description("best bid price"))
            .addField(new Field()
                .key("s")
                .name("symbol")
                .type(Field.Type.STRING))
        );

        assertEquals(expected, schema);
    }

    @Test
    public void generateEncoder()
    {
        final var file = TestUtils.getResourcePath("generator/schema.json").toFile();
        final var generator = new Generator(file, "build/generated/sources/gfjson");
        generator.generateDecoder("L1Update");
    }

}