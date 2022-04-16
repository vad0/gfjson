package de;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenTest
{
    @Test
    public void testIsNumber()
    {
        for (final var token : Token.values())
        {
            if (token == Token.LONG || token == Token.FLOAT)
            {
                assertTrue(token.isNumber());
            }
            else
            {
                assertFalse(token.isNumber());
            }
        }
    }
}