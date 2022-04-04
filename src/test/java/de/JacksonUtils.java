package de;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.SneakyThrows;

import java.util.function.BiConsumer;

public class JacksonUtils
{
    public static void checkToken(final JsonToken expected, final JsonToken actual)
    {
        if (JsonDecoder.APPLY_CHECKS)
        {
            assert expected == actual : actual;
        }
    }

    /**
     * @param actions   map of actions which should be taken when certain keys are encountered
     * @param structure to fill
     */
    @SneakyThrows
    public static <T> void parseStruct(
        final JsonParser parser,
        final JacksonKeyMap<BiConsumer<JsonParser, T>> actions,
        final T structure)
    {
        var token = parser.nextToken();
        checkToken(JsonToken.START_OBJECT, token);
        while (true)
        {
            token = parser.nextToken();
            if (token == JsonToken.END_OBJECT)
            {
                break;
            }
            checkToken(JsonToken.FIELD_NAME, token);
            final var key = actions.getKey(parser.getValueAsString());
            key.accept(parser, structure);
        }
    }

    /**
     * This method skips structure of any complexity
     */
    @SneakyThrows
    public static void skipValue(final JsonParser parser)
    {
        int startArrayCount = 0;
        int endArrayCount = 0;
        int startObjectCount = 0;
        int endObjectCount = 0;
        do
        {
            final var token = parser.nextToken();
            switch (token)
            {
                case START_ARRAY:
                    startArrayCount++;
                    break;
                case END_ARRAY:
                    endArrayCount++;
                    break;
                case START_OBJECT:
                    startObjectCount++;
                    break;
                case END_OBJECT:
                    endObjectCount++;
                    break;
            }
        }
        while (startArrayCount > endArrayCount || startObjectCount > endObjectCount);
    }


    @SneakyThrows
    public static <T> void parseArray(
        final JsonParser parser,
        final T structure,
        final JacksonParseArrayElement<T> parseArrayElement)
    {
        var token = parser.nextToken();
        checkToken(JsonToken.START_ARRAY, token);
        token = parser.nextToken();
        while (token != JsonToken.END_ARRAY)
        {
            parseArrayElement.parseElement(parser, structure, token);
            token = parser.nextToken();
        }
    }

    interface JacksonParseArrayElement<T>
    {
        /**
         * @param structure  to fill
         * @param firstToken is parsed outside
         */
        void parseElement(JsonParser parser, T structure, JsonToken firstToken);
    }
}
