package com.adrien.games.bagl.utils;

import com.adrien.games.bagl.exception.EngineException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * File utility methods
 *
 * @author adrien
 */
public final class FileUtils {

    /**
     * Private constructor to prevent instantiation
     */
    private FileUtils() {
    }

    /**
     * Load the content of a file into a byte buffer
     *
     * @param filePath The path of the file to load
     * @return A {@link ByteBuffer} filled with the file content
     */
    public static ByteBuffer loadAsByteBuffer(final String filePath) {
        try (final var channel = FileChannel.open(Paths.get(filePath))) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        } catch (IOException e) {
            throw new EngineException("Failed to load file into the byte buffer", e);
        }
    }

    /**
     * Get the absolute path of a resource
     *
     * @param resource The name of the resource
     * @return The absolute path of the resource as a {@link String}
     */
    public static String getResourceAbsolutePath(final String resource) {
        return new File(FileUtils.class.getResource(resource).getFile()).getAbsolutePath();
    }

    /**
     * Checks if a file exists
     *
     * @param filePath The path of the file
     * @return The {@link File} if found
     * @throws EngineException if the file does not exists
     */
    public static File checkFileExistence(final String filePath) {
        final var file = new File(filePath);
        if (!file.exists()) {
            throw new EngineException("File not found: " + filePath);
        }
        return file;
    }

    /**
     * Opens a {@link BufferedReader} to a resource file
     *
     * @param resourcePath The path of the resource
     * @return The opened buffered reader
     */
    public static BufferedReader getResourceAsBufferedReader(final String resourcePath) {
        final var inputStream = FileUtils.class.getResourceAsStream(resourcePath);
        if (Objects.isNull(inputStream)) {
            throw new EngineException("Could not find resource file " + resourcePath);
        }
        return new BufferedReader(new InputStreamReader(inputStream));
    }

}
