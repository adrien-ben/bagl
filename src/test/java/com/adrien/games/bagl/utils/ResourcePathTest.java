package com.adrien.games.bagl.utils;

import com.adrien.games.bagl.exception.EngineException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ResourcePath} test class.
 *
 * @author adrien
 */
class ResourcePathTest {

    @Test
    void itShouldNotGetAResource() {
        final var path = ResourcePath.get("/root/test.txt");
        assertFalse(path.isResource());
    }

    @Test
    void itShouldGetAResource() {
        final var path = ResourcePath.get("classpath:/root/test.txt");
        assertTrue(path.isResource());
    }

    @Test
    void itShouldFailToGetFromInvalidPath() {
        assertThrows(InvalidPathException.class, () -> ResourcePath.get("<Invalid"));
    }

    @Test
    void itShouldOpenInputStreamToResource() throws IOException {
        final var resourcePath = ResourcePath.get("classpath:/test.txt");
        try (final var inputStream = resourcePath.openInputStream()) {
            final var content = new String(inputStream.readAllBytes());
            assertEquals("Test file", content);
        }
    }

    @Test
    void itShouldOpenInputStreamToFile() throws IOException {
        final var resourcePath = ResourcePath.get(getTestFilePath());
        try (final var inputStream = resourcePath.openInputStream()) {
            final var content = new String(inputStream.readAllBytes());
            assertEquals("Test file", content);
        }
    }

    @Test
    void itShouldFailToOpenInputStreamToNonExistingResource() {
        final var path = ResourcePath.get("classpath:/not_here.txt");
        assertThrows(EngineException.class, path::openInputStream);
    }

    @Test
    void itShouldFailToOpenInputStreamToNonExistingFile() {
        final var path = ResourcePath.get("P:/not_here.txt");
        assertThrows(EngineException.class, path::openInputStream);
    }

    @Test
    void itShouldGetResourceAbsolutePath() {
        final var absolutePath = ResourcePath.get("classpath:/test.txt").getAbsolutePath();
        final var expectedPath = Paths.get(getTestFilePath()).toAbsolutePath().toString();
        assertEquals(expectedPath, absolutePath);
    }

    @Test
    void itShouldGetFileAbsolutePath() {
        final var absolutePath = ResourcePath.get(getTestFilePath()).getAbsolutePath();
        final var expectedPath = Paths.get(getTestFilePath()).toAbsolutePath().toString();
        assertEquals(expectedPath, absolutePath);
    }

    @Test
    void itShouldGetResourceParent() {
        final var parent = ResourcePath.get("classpath:/root/test.txt").getParent();
        assertNotNull(parent);
        assertTrue(parent.isResource());
    }

    @Test
    void itShouldGetNonResourceParent() {
        final var parent = ResourcePath.get(getTestFilePath()).getParent();
        assertNotNull(parent);
        assertFalse(parent.isResource());
    }

    @Test
    void itShouldNotGetRootResourceParent() {
        final var path = ResourcePath.get("classpath:");
        assertThrows(EngineException.class, path::getParent);
    }

    @Test
    void itShouldNotGetRootFileParent() {
        final var path = ResourcePath.get("");
        assertThrows(EngineException.class, path::getParent);
    }

    private String getTestFilePath() {
        return getExecutionPath() + File.separator + "target/test-classes/test.txt";
    }

    private String getExecutionPath() {
        return System.getProperty("user.dir");
    }
}