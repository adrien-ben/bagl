package com.adrien.games.bagl.parser.model;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.EngineException;
import com.adrien.games.bagl.rendering.Material;
import com.adrien.games.bagl.rendering.texture.Filter;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureParameters;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Custom Wavefront's mtl files parser
 * <p>
 * Supports :
 * <ul>
 * <li>diffuse color (Kd R G B)
 * <li>diffuse map (map_Kd RELATIVE_FILE_PATH)
 * <li>emissive color (Ke R G B)
 * <li>emissive map (map_Ke RELATIVE_FILE_PATH)
 * <li>roughness factor (Kr FLOAT)
 * <li>metalness factor (Km FLOAT)
 * <li>orm (occlusion/roughness/metalness) map (map_Korm RELATIVE_FILE_PATH)
 * <li>normal map (bump/map_bump RELATIVE_FILE_PATH)
 * </ul>
 *
 * @author adrien
 */
public class MtlParser {

    private static final Logger log = LogManager.getLogger(MtlParser.class);

    private String currentFile;
    private final Map<String, Material.Builder> builders = new HashMap<>();
    private Material.Builder currentBuilder;

    /**
     * Parse a Wavefront's mtl file
     *
     * @param filePath The file to parse
     * @return A map with material name as key and a {@link Material} as value
     */
    public Map<String, Material> parse(final String filePath) {
        log.trace("Parsing .mtl file '{}'.", filePath);
        this.reset(filePath);
        try (final Stream<String> stream = Files.lines(Paths.get(filePath))) {
            stream.filter(StringUtils::isNotBlank).forEach(this::parseLine);
        } catch (final IOException e) {
            throw new EngineException("Failed to parse material file", e);
        }
        return this.builders.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().build()));
    }

    private void reset(final String filePath) {
        this.currentFile = filePath;
        this.builders.clear();
        this.currentBuilder = null;
    }

    private void parseLine(final String line) {
        final String[] tokens = line.split(" ");
        if (tokens.length > 0) {
            final String first = tokens[0];
            if ("newmtl".equals(first) && tokens.length > 1) {
                this.parseNewMaterial(tokens[1]);
            } else if ("map_Kd".equals(first) && tokens.length > 1) {
                this.parseDiffuseMap(tokens[1]);
            } else if ("Kd".equals(first) && tokens.length > 3) {
                this.parseDiffuseColor(tokens);
            } else if (first.matches("bump|map_bump") && tokens.length > 1) {
                this.parseBumpMap(tokens[1]);
            } else if ("Kr".equals(first) && tokens.length > 1) {
                this.parseRoughness(tokens[1]);
            } else if ("Km".equals(first) && tokens.length > 1) {
                this.parseMetallic(tokens[1]);
            } else if ("Ke".equals(first) && tokens.length > 3) {
                this.parseEmissiveColor(tokens);
            } else if ("map_Ke".equals(first) && tokens.length > 1) {
                this.parseEmissiveMap(tokens[1]);
            } else if ("map_Korm".equals(first) && tokens.length > 1) {
                this.parseOrmMap(tokens[1]);
            }
        }
    }

    private void parseNewMaterial(final String name) {
        this.currentBuilder = Material.builder();
        this.builders.put(name, this.currentBuilder);
    }

    private void parseDiffuseMap(final String fileName) {
        this.checkCurrentMaterial();
        this.currentBuilder.diffuse(this.loadTexture(fileName));
    }

    private void parseDiffuseColor(final String[] tokens) {
        this.checkCurrentMaterial();
        final float r = Float.parseFloat(tokens[1]);
        final float g = Float.parseFloat(tokens[2]);
        final float b = Float.parseFloat(tokens[3]);
        this.currentBuilder.diffuse(new Color(r, g, b));
    }

    private void parseBumpMap(final String fileName) {
        this.checkCurrentMaterial();
        this.currentBuilder.normals(this.loadTexture(fileName));
    }

    private void parseRoughness(final String value) {
        this.checkCurrentMaterial();
        this.currentBuilder.roughness(Float.parseFloat(value));
    }

    private void parseMetallic(final String value) {
        this.checkCurrentMaterial();
        this.currentBuilder.roughness(Float.parseFloat(value));
    }

    private void parseEmissiveColor(final String[] tokens) {
        this.checkCurrentMaterial();
        final float r = Float.parseFloat(tokens[1]);
        final float g = Float.parseFloat(tokens[2]);
        final float b = Float.parseFloat(tokens[3]);
        this.currentBuilder.emissive(new Color(r, g, b));
        this.currentBuilder.emissiveIntensity(1.0f);
    }

    private void parseEmissiveMap(final String filePath) {
        this.checkCurrentMaterial();
        this.currentBuilder.emissive(this.loadTexture(filePath));
        this.currentBuilder.emissiveIntensity(1.0f);
    }

    private void parseOrmMap(final String filePath) {
        this.checkCurrentMaterial();
        this.currentBuilder.orm(this.loadTexture(filePath));
    }

    private Texture loadTexture(final String name) {
        final String folderPath = Paths.get(this.currentFile).getParent().toString();
        final String texturePath = folderPath + "/" + name;
        final TextureParameters.Builder params = TextureParameters.builder()
                .mipmaps(true)
                .minFilter(Filter.MIPMAP_LINEAR_LINEAR)
                .anisotropic(Configuration.getInstance().getAnisotropicLevel());
        return Texture.fromFile(texturePath, params);
    }

    private void checkCurrentMaterial() {
        if (Objects.isNull(this.currentBuilder)) {
            throw new EngineException("Missing 'newmtl' declaration in '" + this.currentFile + "'.");
        }
    }
}
