package generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class JsonToolTest
{
    public static final Path DEBUG_DIR = Path.of(System.getProperty("user.dir"), "build/generated/sources/gfjson");
    public static final String SCHEMA_PATH = "src/test/resources/generator/schema.json";

    @TempDir
    public Path tempDir;

    @Test
    public void wrongPath()
    {
        assertThrows(
            FileNotFoundException.class,
            () -> Schema.read(new File("non existent path")));
    }

    @Test
    public void getSchema1()
    {
        System.clearProperty(JsonTool.SCHEMA_PATH);
        assertThrows(FileNotFoundException.class, JsonTool::getSchema);
    }

    @Test
    public void getSchema2()
    {
        System.setProperty(JsonTool.SCHEMA_PATH, "non existent path");
        assertThrows(FileNotFoundException.class, JsonTool::getSchema);
    }

    @Test
    public void getSchema3()
    {
        System.setProperty(JsonTool.SCHEMA_PATH, SCHEMA_PATH);
        final var schema = JsonTool.getSchema();
        final var expected = Schema.read(new File(SCHEMA_PATH));
        assertEquals(expected, schema);
    }

    @Test
    public void getOutputDir1()
    {
        System.clearProperty(JsonTool.OUTPUT_DIR);
        assertThrows(FileNotFoundException.class, JsonTool::getOutputDir);
    }

    @Test
    public void getOutputDir2()
    {
        System.setProperty(JsonTool.OUTPUT_DIR, DEBUG_DIR.toString());
        assertEquals(DEBUG_DIR, JsonTool.getOutputDir());
    }

    @Test
    public void testMain()
    {
        System.setProperty(JsonTool.SCHEMA_PATH, SCHEMA_PATH);
        System.setProperty(JsonTool.OUTPUT_DIR, tempDir.toString());
        JsonTool.main(new String[]{});

        assertTrue(tempDir.resolve("md").resolve("OrderType.java").toFile().exists());
    }
}