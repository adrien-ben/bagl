package com.adrien.games.bagl.resource;

import com.adrien.games.bagl.exception.EngineException;
import com.adrien.games.bagl.utils.ResourcePath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        final var path = ResourcePath.get(filePath);
        try (final var reader = new BufferedReader(new InputStreamReader(path.openInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (final IOException exception) {
            throw new EngineException("Failed to load shader source file", exception);
        }
    }

}
