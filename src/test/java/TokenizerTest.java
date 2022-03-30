import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenizerTest {
    @SneakyThrows
    @Test
    public void parseIncrement() {
        String str = Files.readString(Paths.get("src", "test", "resources", "increment.json"));
        var tokenizer = new Tokenizer();
        tokenizer.wrap(str);

        assertEquals(Token.START_OBJECT, tokenizer.next());
        checkString(tokenizer, "e");
        checkString(tokenizer, "depthUpdate");
        checkString(tokenizer, "E");
        checkLong(tokenizer, 123456789);
        checkString(tokenizer, "s");
        checkString(tokenizer, "BNBBTC");
        checkString(tokenizer, "U");
        checkLong(tokenizer, 157);
        checkString(tokenizer, "u");
        checkLong(tokenizer, 160);
        checkString(tokenizer, "b");
        assertEquals(Token.START_ARRAY, tokenizer.next());
        assertEquals(Token.START_ARRAY, tokenizer.next());
        checkString(tokenizer, "0.0024");
        checkString(tokenizer, "10");
        assertEquals(Token.END_ARRAY, tokenizer.next());
        assertEquals(Token.END_ARRAY, tokenizer.next());
        checkString(tokenizer, "a");
        assertEquals(Token.START_ARRAY, tokenizer.next());
        assertEquals(Token.START_ARRAY, tokenizer.next());
        checkString(tokenizer, "0.0026");
        checkString(tokenizer, "100");
        assertEquals(Token.END_ARRAY, tokenizer.next());
        assertEquals(Token.END_ARRAY, tokenizer.next());
        assertEquals(Token.END_OBJECT, tokenizer.next());
        assertEquals(Token.END, tokenizer.next());
    }

    private static void checkLong(Tokenizer tokenizer, final int expected) {
        assertEquals(Token.LONG, tokenizer.next());
        assertEquals(expected, tokenizer.getLong());
    }

    private static void checkString(Tokenizer tokenizer, final String expected) {
        assertEquals(Token.STRING, tokenizer.next());
        assertEquals(expected, tokenizer.getString().toString());
    }
}