package com.adrien.games.bagl.resource;

import com.adrien.games.bagl.core.EngineException;
import com.adrien.games.bagl.utils.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * Load the content of a shader source file
     *
     * @param filePath The path of the source file
     * @return The source code of the shader
     */
    public String loadSourceFile(final String filePath) {
        LOG.trace("Loading shader source: {}", filePath);
        FileUtils.checkFileExistence(filePath);
        try (final Stream<String> lines = Files.lines(Paths.get(filePath))) {
            return lines.collect(Collectors.joining("\n"));
        } catch (final IOException exception) {
            throw new EngineException("Failed to load shader source file", exception);
        }
    }
}
