package de;

public enum Token
{
    START_OBJECT,
    END_OBJECT,
    START_ARRAY,
    END_ARRAY,
    STRING,
    BOOLEAN,
    LONG,
    FLOAT,
    NULL,
    END;

    public void checkToken(final Token token)
    {
        if (JsonDecoder.APPLY_CHECKS)
        {
            assert token == this : token;
        }
    }

    public boolean isNumber()
    {
        return this == LONG || this == FLOAT;
    }
}
