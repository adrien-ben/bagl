package com.adrienben.games.bagl.core.io;

import com.adrienben.games.bagl.core.exception.EngineException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Assertions.assertThrows(InvalidPathException.class, () -> ResourcePath.get("<Invalid"));
    }

    @Test
    void itShouldOpenInputStreamToResource() throws IOException {
        final var resourcePath = ResourcePath.get("classpath:/test.txt");
        try (final var inputStream = resourcePath.openInputStream()) {
            final var content = new String(inputStream.readAllBytes());
            Assertions.assertEquals("Test file", content);
        }
    }

    @Test
    void itShouldOpenInputStreamToFile() throws IOException {
        final var resourcePath = ResourcePath.get(getTestFilePath());
        try (final var inputStream = resourcePath.openInputStream()) {
            final var content = new String(inputStream.readAllBytes());
            Assertions.assertEquals("Test file", content);
        }
    }

    @Test
    void itShouldFailToOpenInputStreamToNonExistingResource() {
        final var path = ResourcePath.get("classpath:/not_here.txt");
        Assertions.assertThrows(EngineException.class, path::openInputStream);
    }

    @Test
    void itShouldFailToOpenInputStreamToNonExistingFile() {
        final var path = ResourcePath.get("P:/not_here.txt");
        Assertions.assertThrows(EngineException.class, path::openInputStream);
    }

    @Test
    void itShouldGetResourceAbsolutePath() {
        final var absolutePath = ResourcePath.get("classpath:/test.txt").getAbsolutePath();
        final var expectedPath = Paths.get(getTestFilePath()).toAbsolutePath().toString();
        Assertions.assertEquals(expectedPath, absolutePath);
    }

    @Test
    void itShouldGetFileAbsolutePath() {
        final var absolutePath = ResourcePath.get(getTestFilePath()).getAbsolutePath();
        final var expectedPath = Paths.get(getTestFilePath()).toAbsolutePath().toString();
        Assertions.assertEquals(expectedPath, absolutePath);
    }

    @Test
    void itShouldGetResourceParent() {
        final var parent = ResourcePath.get("classpath:/root/test.txt").getParent();
        Assertions.assertNotNull(parent);
        assertTrue(parent.isResource());
    }

    @Test
    void itShouldGetNonResourceParent() {
        final var parent = ResourcePath.get(getTestFilePath()).getParent();
        Assertions.assertNotNull(parent);
        assertFalse(parent.isResource());
    }

    @Test
    void itShouldNotGetRootResourceParent() {
        final var path = ResourcePath.get("classpath:");
        Assertions.assertThrows(EngineException.class, path::getParent);
    }

    @Test
    void itShouldNotGetRootFileParent() {
        final var path = ResourcePath.get("");
        Assertions.assertThrows(EngineException.class, path::getParent);
    }

    @Test
    void itShouldReturnTrueWhenCallingExistsOnExistingResource() {
        final var resourcePath = ResourcePath.get("classpath:/test.txt");
        assertTrue(resourcePath.exists());
    }

    @Test
    void itShouldReturnFalseWhenCallingExistsOnNonExistingResource() {
        final var resourcePath = ResourcePath.get("classpath:/not_here.txt");
        assertFalse(resourcePath.exists());
    }

    @Test
    void itShouldReturnTrueWhenCallingExistsOnExistingFile() {
        final var resourcePath = ResourcePath.get(getTestFilePath());
        assertTrue(resourcePath.exists());
    }

    @Test
    void itShouldReturnFalseWhenCallingExistsOnNonExistingFile() {
        final var resourcePath = ResourcePath.get("P:/not_here.txt");
        assertFalse(resourcePath.exists());
    }

    private String getTestFilePath() {
        return getExecutionPath() + File.separator + "target/test-classes/test.txt";
    }

    private String getExecutionPath() {
        return System.getProperty("user.dir");
    }
}