package de;

public class Quote
{
    public double price;
    public double size;

    @Override
    public String toString()
    {
        return size + " @ " + price;
    }
}
