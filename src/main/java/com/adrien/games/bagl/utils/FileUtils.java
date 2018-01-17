package com.adrien.games.bagl.utils;

import com.adrien.games.bagl.core.EngineException;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

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
        try (final FileChannel channel = FileChannel.open(Paths.get(filePath))) {
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
        final File file = new File(filePath);
        if (!file.exists()) {
            throw new EngineException("File not found: " + filePath);
        }
        return file;
    }

}
