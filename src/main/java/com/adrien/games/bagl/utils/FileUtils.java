package com.adrien.games.bagl.utils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

public final class FileUtils {

    private FileUtils() {
    }

    /**
     * Load the content of a file into a byte buffer.
     * @param filePath The path of the file to load.
     * @return A {@link ByteBuffer} filled with the file content.
     */
    public static ByteBuffer loadAsByteBuffer(String filePath) {
        try (final FileChannel channel = FileChannel.open(Paths.get(filePath))) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file into the byte buffer", e);
        }
    }

    /**
     * Gets the absolute path of a resource.
     * @param resource The name of the resource.
     * @return The absolute path of the resource as a {@link String}.
     */
    public static String getResourceAbsolutePath(String resource) {
        return new File(FileUtils.class.getResource(resource).getFile()).getAbsolutePath();
    }

}
