package tokenizer;

interface ParseArrayElement<T>
{
    /**
     * @param structure  to fill
     * @param firstToken is parsed outside
     */
    void parseElement(Tokenizer tokenizer, T structure, Token firstToken);
}
