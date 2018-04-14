package com.adrien.games.bagl.resource;

import com.adrien.games.bagl.exception.EngineException;
import com.adrien.games.bagl.utils.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
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

    private static final String BASE_SHADER_DIRECTORY = "/shaders/";

    /**
     * Load the source code of a shader from a resource file
     *
     * @param resourceName The name of the resource to load
     * @return The source code of the shader
     */
    public String loadSourceFromResource(final String resourceName) {
        LOG.trace("Loading shader source from resources file: {}", resourceName);
        final var resourcePath = BASE_SHADER_DIRECTORY + resourceName.replaceAll("^/*", "");
        try (final var reader = FileUtils.getResourceAsBufferedReader(resourcePath)) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (final IOException exception) {
            throw new EngineException("Failed to load shader source file", exception);
        }
    }
}
