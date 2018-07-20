package com.adrien.games.bagl.resource.shader;

import com.adrien.games.bagl.exception.EngineException;
import com.adrien.games.bagl.utils.ResourcePath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Shader source parser is responsible for parsing shader source.
 * <p>
 * The parser handles a custom #import directive which allows to reference other shader files.
 * It will just 'paste' the content of the referenced shader without checking for code correctness.
 * Trying to compile the parsed shader would then fail if a constant appears multiple times for example.
 * The parser will prevent a same file to be included several times though. It means that if a shader source
 * file A references B and C. And that B references C. C will only be included once.
 * <p>
 * You must not share instances of this class between different threads.
 *
 * @author adrien
 */
public class ShaderSourceParser {

    private static final String IMPORT_DIRECTIVE = "#import";
    private static final Pattern IMPORT_LINE_PATTERN = Pattern.compile(IMPORT_DIRECTIVE + "(?:\\s*)\"(.*)\"");
    private static final int IMPORT_LINE_MATCHER_PATH_GROUP = 1;

    private ResourcePath currentPath;
    private Set<String> importedPaths;
    private StringBuilder source;

    /**
     * Parse a shader source file and returned the parsed content.
     */
    public String parse(final ResourcePath path) {
        initParser(path);
        loadSource(path);
        return source.toString();
    }

    private void initParser(final ResourcePath path) {
        currentPath = path;
        importedPaths = new LinkedHashSet<>();
        importedPaths.add(path.getAbsolutePath());
        source = new StringBuilder();
    }

    private void loadSource(final ResourcePath path) {
        try (final var reader = new BufferedReader(new InputStreamReader(path.openInputStream()))) {
            reader.lines().forEach(this::processLine);
        } catch (final IOException exception) {
            throw new EngineException(String.format("Failed to parse shader file %s", currentPath), exception);
        }
    }

    private void processLine(final String line) {
        if (isImportLine(line)) {
            processImportLine(line);
        } else {
            appendNewLineToSource(line);
        }
    }

    private boolean isImportLine(final String line) {
        return line.startsWith(IMPORT_DIRECTIVE);
    }

    private void processImportLine(final String line) {
        final var dependencyPath = getPathFromImportLine(line);
        loadDependency(dependencyPath);
    }

    private String getPathFromImportLine(final String line) {
        final var importLineMatcher = IMPORT_LINE_PATTERN.matcher(line);
        if (!importLineMatcher.matches()) {
            throw new EngineException("Found invalid import line");
        }
        return importLineMatcher.group(IMPORT_LINE_MATCHER_PATH_GROUP);
    }

    private void loadDependency(final String dependencyPath) {
        final ResourcePath path = ResourcePath.get(dependencyPath);
        final var isNotAlreadyLoaded = importedPaths.add(path.getAbsolutePath());
        if (isNotAlreadyLoaded) {
            loadSource(path);
        }
    }

    private void appendNewLineToSource(final String line) {
        source.append(line).append(System.lineSeparator());
    }
}
