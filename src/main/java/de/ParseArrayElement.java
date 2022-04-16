package de;

public interface ParseArrayElement<T>
{
    /**
     * @param structure  to fill
     * @param firstToken is parsed outside
     */
    void parseElement(JsonDecoder jsonDecoder, T structure, Token firstToken);
}
