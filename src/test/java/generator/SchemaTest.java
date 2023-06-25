package generator;

import org.junit.jupiter.api.Test;
import utils.TestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchemaTest
{
    @Test
    public void testParseSchema()
    {
        final var file = TestUtils.getResourcePath("generator/schema.json").toFile();
        final var schema = Schema.read(file);

        final var expected = new Schema()
            .addEnum(new EnumDefinition()
                .name("OrderType")
                .packageName("md")
                .generate(List.of(Generate.POJO, Generate.DECODER))
                .javadoc("obvious")
                .asEnum()
                .values(List.of(
                    new EnumValue("LIMIT", "Order has price."),
                    new EnumValue("MARKET"),
                    new EnumValue("MARKET_LIMIT"))))
            .addEnum(new EnumDefinition()
                .name("TimeInForce")
                .packageName("md")
                .generate(List.of(Generate.DECODER))
                .asEnum())
            .addMessage(new StructDefinition()
                .name("L1Update")
                .packageName("md")
                .generate(List.of(Generate.DECODER))
                .asStruct()
                .strictOrder(true)
                .addField(new Field()
                    .key("e")
                    .name("eventType")
                    .type(Field.Type.STRING)
                    .constant(true)
                    .javadoc("event type, e.g. bookTicker")
                    .expected("bookTicker"))
                .addField(new Field().key("u")
                    .name("updateId")
                    .type(Field.Type.LONG)
                    .javadoc("order book updateId"))
                .addField(new Field()
                    .key("b")
                    .name("bestBidPrice")
                    .type(Field.Type.QUOTED_DOUBLE)
                    .javadoc("best bid price"))
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
}