package utils;

import lombok.SneakyThrows;

import java.net.URI;
import java.nio.file.*;
import java.util.Collections;
import java.util.function.BiConsumer;

public class TestUtils
{
    private static final BiConsumer DO_NOTHING = (a, b) ->
    {
    };

    public static <A, B> BiConsumer<A, B> doNothing()
    {
        return DO_NOTHING;
    }

    @SneakyThrows
    public static Path getResourcePath(final String dir)
    {
        final URI uri = Thread.currentThread().getContextClassLoader().getResource(dir).toURI();
        if (uri.getScheme().equals("jar"))
        {
            final FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
            return fileSystem.getPath(dir);
        }
        else
        {
            return Paths.get(uri);
        }
    }

    @SneakyThrows
    public static String readFile(final String fileName)
    {
        final var path = getResourcePath(fileName);
        return Files.readString(path);
    }
}
