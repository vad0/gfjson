package de;

public class TokenException extends RuntimeException
{
    public TokenException(final int offset, final char unexpected)
    {
        this("Unexpected char " + unexpected + " at offset " + offset);
    }

    public TokenException(final String message)
    {
        super(message);
    }
}
