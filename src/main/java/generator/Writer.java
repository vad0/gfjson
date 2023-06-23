package generator;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * This class helps to write strings to file. The main value of it is that it tracks indentation.
 */
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

    void writeJavadoc(final HasJavadoc value)
    {
        final String javadoc = value.javadoc();
        if (javadoc == null)
        {
            return;
        }
        println("/**");
        println("* %s", javadoc);
        println("*/");
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

    void println(final String format, final String... args)
    {
        printf(format + '\n', args);
    }

    void printf(final String format, final String... args)
    {
        writer.printf(TAB.repeat(indent) + format, Arrays.stream(args).toArray());
    }
}
