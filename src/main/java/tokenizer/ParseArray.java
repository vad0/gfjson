package tokenizer;

interface ParseArray<T> {
    /**
     * @param structure  to fill
     * @param firstToken is parsed outside
     */
    void parseElement(Tokenizer tokenizer, T structure, Token firstToken);

    /**
     * Parse array of uniform values, e.g. quotes. This method is not suitable if e.g. first element of array is price
     * and second is size.
     */
    default void parseArray(Tokenizer tokenizer, T structure) {
        Token token = tokenizer.next();
        Token.START_ARRAY.checkToken(token);
        token = tokenizer.next();
        while (token != Token.END_ARRAY) {
            parseElement(tokenizer, structure, token);
            token = tokenizer.next();
        }
    }
}
