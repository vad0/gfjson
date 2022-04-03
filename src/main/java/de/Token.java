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
    END;

    public void checkToken(final Token token)
    {
        if (Tokenizer.APPLY_CHECKS)
        {
            assert token == this : token;
        }
    }
}
