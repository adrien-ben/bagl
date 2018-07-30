package com.adrien.games.bagl.core.io;

import com.adrien.games.bagl.core.exception.EngineException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Represents an accessor to a file.
 * <p>
 * A file can be any file on the file system or a resource accessible
 * in the classpath.
 *
 * @author adrien
 */
public class ResourcePath {

    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String EMPTY_STR = "";
    private static final String RESOURCE_SEPARATOR = "/";
    private static final int RESOURCE_SEPARATOR_LENGTH = RESOURCE_SEPARATOR.length();

    private boolean isResource;
    private Path path;

    private ResourcePath(final String pathAsString, final String... more) {
        this.isResource = pathAsString.startsWith(CLASSPATH_PREFIX);
        this.path = Paths.get(this.isResource ? pathAsString.replaceFirst(CLASSPATH_PREFIX, EMPTY_STR) : pathAsString, more);
    }

    /**
     * Get a resource path.
     * <p>
     * {@code pathAsString} can be the absolute path of a file on the file system or
     * the path of a resource prefixed by {@value CLASSPATH_PREFIX}. {@code more} is
     * additional elements that will be joined to create the path.
     */
    public static ResourcePath get(final String pathAsString, final String... more) {
        return new ResourcePath(pathAsString, more);
    }

    /**
     * Open a stream to the file pointed by this resource path.
     */
    public InputStream openInputStream() {
        if (isResource()) {
            return openResourceAsStream();
        }
        return openFileAsStream();
    }

    private InputStream openResourceAsStream() {
        final var resourceAsStream = getClassLoader().getResourceAsStream(getFormattedResourcePath());
        if (Objects.isNull(resourceAsStream)) {
            throw new EngineException(String.format("Resource %s does not exist.", path));
        }
        return resourceAsStream;
    }

    private ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private String getFormattedResourcePath() {
        final var pathWithResourceSeparator = this.path.toString().replace(File.separator, RESOURCE_SEPARATOR);
        if (startsWithResourceSeparator(pathWithResourceSeparator)) {
            return pathWithResourceSeparator.substring(RESOURCE_SEPARATOR_LENGTH);
        }
        return pathWithResourceSeparator;
    }

    private boolean startsWithResourceSeparator(final String path) {
        return path.startsWith(RESOURCE_SEPARATOR);
    }

    private InputStream openFileAsStream() {
        try {
            return Files.newInputStream(path);
        } catch (final IOException exception) {
            throw new EngineException(String.format("Failed to open stream from %s.", path), exception);
        }
    }

    /**
     * Get the absolute path of the resource pointed by this path.
     */
    public String getAbsolutePath() {
        return isResource() ? getResourceAbsolutePath() : path.toAbsolutePath().toString();
    }

    private String getResourceAbsolutePath() {
        return new File(getResourceAsUrl().getFile()).getAbsolutePath();
    }

    private URL getResourceAsUrl() {
        final URL url = getResource();
        if (Objects.isNull(url)) {
            throw new EngineException(String.format("Resource %s does not exist.", path));
        }
        return url;
    }

    private URL getResource() {
        return getClassLoader().getResource(getFormattedResourcePath());
    }

    public ResourcePath getParent() {
        final var parentPath = path.getParent();
        if (Objects.isNull(parentPath)) {
            throw new EngineException(String.format("The path %s has no parent", path));
        }
        return get(getPathString(parentPath));
    }

    /**
     * Check if the pointed resource exists.
     */
    public boolean exists() {
        return isResource() ? resourceExists() : fileExists();
    }

    private boolean resourceExists() {
        return Objects.nonNull(getResource());
    }

    private boolean fileExists() {
        return Files.exists(path);
    }

    @Override
    public String toString() {
        return getPathString(path);
    }

    private String getPathString(final Path path) {
        return isResource() ? CLASSPATH_PREFIX + path.toString() : path.toString();
    }

    public boolean isResource() {
        return isResource;
    }
}
