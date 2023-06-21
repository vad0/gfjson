package generator;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Writer
    implements AutoCloseable
{
    public static final String TAB = " ".repeat(4);
    private final PrintWriter writer;
    private int indent = 0;

    @SneakyThrows
    public Writer(final File file)
    {
        this.writer = new PrintWriter(new FileWriter(file));
    }

    void endScope()
    {
        indent--;
        assert indent >= 0 : indent;
        printf("}\n");
    }

    void startScope()
    {
        println();
        printf("{");
        println();
        indent++;
    }

    void importClass(final Class<?> k)
    {
        printf("import %s;\n", k.getCanonicalName());
    }

    @Override
    public void close()
    {
        writer.close();
    }

    void println()
    {
        writer.println();
    }

    void printf(final String format, final Object... args)
    {
        writer.printf(TAB.repeat(indent) + format, args);
    }
}
