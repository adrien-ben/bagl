package com.adrien.games.bagl.resource;

import com.adrien.games.bagl.exception.EngineException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * Shader source loader
 * <p>
 * TODO: include custom directives (ex: #import, ...)
 *
 * @author adrien
 */
public class ShaderLoader {

    private static final Logger LOG = LogManager.getLogger(ShaderLoader.class);

    /**
     * Load the source code of a shader from a file
     *
     * @param filePath The path of the shader to load
     * @return The source code of the shader
     */
    public String loadSource(final String filePath) {
        LOG.trace("Loading shader source from resources file: {}", filePath);
        try (final var lines = Files.lines(Paths.get(filePath))) {
            return lines.collect(Collectors.joining("\n"));
        } catch (final IOException exception) {
            throw new EngineException("Failed to load shader source file", exception);
        }
    }
}
