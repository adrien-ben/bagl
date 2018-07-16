package com.adrien.games.bagl.utils;

import com.adrien.games.bagl.exception.EngineException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * File utility methods
 *
 * @author adrien
 */
public final class FileUtils {

    private static final String CLASSPATH_PREFIX = "classpath:";

    /**
     * Private constructor to prevent instantiation
     */
    private FileUtils() {
    }

    /**
     * Get the absolute path of a resource
     *
     * @param resource The name of the resource
     * @return The absolute path of the resource as a {@link String}
     */
    public static InputStream getResourceAsStream(final String resource) {
        final var correctResourcePath = resource.startsWith("/") ? resource.replaceFirst("/", "") : resource;
        final var classLoader = Thread.currentThread().getContextClassLoader();
        final var resourceUrl = classLoader.getResourceAsStream(correctResourcePath);
        if (Objects.isNull(resourceUrl)) {
            throw new EngineException(String.format("Resource %s does not exist.", correctResourcePath));
        }
        return resourceUrl;
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
     * Open a stream to the file at {@code filePath}.
     * <p>
     * If the path is prefixed with {@value CLASSPATH_PREFIX} it will open a stream to a
     * resource file.
     */
    public static InputStream openStream(final String filePath) {
        if (filePath.startsWith(CLASSPATH_PREFIX)) {
            return FileUtils.getResourceAsStream(filePath.replace(CLASSPATH_PREFIX, ""));
        }
        try {
            return Files.newInputStream(Paths.get(filePath));
        } catch (final IOException exception) {
            throw new EngineException(String.format("Failed to open stream from %s.", filePath), exception);
        }
    }

    /**
     * Resolve the actual value of {@code filePath}.
     * <p>
     * If {@code filePath} is prefixed with {@value CLASSPATH_PREFIX} it will return
     * the absolute path of the resource whose name is specified after the prefix.
     * <p>
     * Otherwise it will simply return {@code filePath}.
     */
    public static String resolvePath(final String filePath) {
        if (filePath.startsWith(CLASSPATH_PREFIX)) {
            return FileUtils.getResourceAbsolutePath(filePath.replace(CLASSPATH_PREFIX, ""));
        }
        return filePath;
    }
}
