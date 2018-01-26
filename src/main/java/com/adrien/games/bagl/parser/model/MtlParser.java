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
import java.util.stream.Stream;

/**
 * Custom Wavefront's mtl files parser
 * <p>
 * Supports :
 * <ul>
 * <li>diffuse color (Kd R G B)
 * <li>diffuse map (map_Kd RELATIVE_FILE_PATH)
 * <li>roughness factor (Kr FLOAT)
 * <li>roughness map (map_Kr RELATIVE_FILE_PATH)
 * <li>metalness factor (Km FLOAT)
 * <li>metalness map (map_Km RELATIVE_FILE_PATH)
 * <li>normal map (bump/map_bump RELATIVE_FILE_PATH)
 * <li>emissive color (Ke R G B)
 * </ul>
 *
 * @author adrien
 */
public class MtlParser {

    private static final Logger log = LogManager.getLogger(MtlParser.class);

    private String currentFile;
    private final Map<String, Material> materials = new HashMap<>();
    private Material currentMaterial;

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
        return this.materials;
    }

    private void reset(final String filePath) {
        this.currentFile = filePath;
        this.materials.clear();
        this.currentMaterial = null;
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
            } else if ("map_Kr".equals(first) && tokens.length > 1) {
                this.parseRoughnessMap(tokens[1]);
            } else if ("Km".equals(first) && tokens.length > 1) {
                this.parseMetallic(tokens[1]);
            } else if ("map_Km".equals(first) && tokens.length > 1) {
                this.parseMetallicMap(tokens[1]);
            } else if ("Ke".equals(first) && tokens.length > 3) {
                this.parseEmissiveColor(tokens);
            } else if ("map_Ke".equals(first) && tokens.length > 1) {
                this.parseEmissiveMap(tokens[1]);
            }
        }
    }

    private void parseNewMaterial(String name) {
        currentMaterial = new Material();
        materials.put(name, currentMaterial);
    }

    private void parseDiffuseMap(String fileName) {
        this.checkCurrentMaterial();
        this.currentMaterial.setDiffuseMap(this.loadTexture(fileName));
    }

    private void parseDiffuseColor(String[] tokens) {
        this.checkCurrentMaterial();
        float r = Float.parseFloat(tokens[1]);
        float g = Float.parseFloat(tokens[2]);
        float b = Float.parseFloat(tokens[3]);
        this.currentMaterial.setDiffuseColor(new Color(r, g, b));
    }

    private void parseBumpMap(String fileName) {
        this.checkCurrentMaterial();
        this.currentMaterial.setNormalMap(this.loadTexture(fileName));
    }

    private void parseRoughness(String value) {
        this.checkCurrentMaterial();
        this.currentMaterial.setRoughness(Float.parseFloat(value));
    }

    private void parseRoughnessMap(String fileName) {
        this.checkCurrentMaterial();
        this.currentMaterial.setRoughnessMap(this.loadTexture(fileName));
    }

    private void parseMetallic(String value) {
        this.checkCurrentMaterial();
        this.currentMaterial.setMetallic(Float.parseFloat(value));
    }

    private void parseMetallicMap(String fileName) {
        this.checkCurrentMaterial();
        this.currentMaterial.setMetallicMap(this.loadTexture(fileName));
    }

    private void parseEmissiveColor(final String[] tokens) {
        this.checkCurrentMaterial();
        float r = Float.parseFloat(tokens[1]);
        float g = Float.parseFloat(tokens[2]);
        float b = Float.parseFloat(tokens[3]);
        this.currentMaterial.setEmissiveColor(new Color(r, g, b));
        this.currentMaterial.setEmissiveIntensity(1.0f);
    }

    private void parseEmissiveMap(final String filePath) {
        this.checkCurrentMaterial();
        this.currentMaterial.setEmissiveMap(this.loadTexture(filePath));
        this.currentMaterial.setEmissiveIntensity(1.0f);
    }

    private Texture loadTexture(String name) {
        final String folderPath = Paths.get(this.currentFile).getParent().toString();
        final String texturePath = folderPath + "/" + name;
        final TextureParameters.Builder params = TextureParameters.builder()
                .mipmaps()
                .minFilter(Filter.MIPMAP_LINEAR_LINEAR)
                .anisotropic(Configuration.getInstance().getAnisotropicLevel());
        return Texture.fromFile(texturePath, params);
    }

    private void checkCurrentMaterial() {
        if (Objects.isNull(this.currentMaterial)) {
            throw new EngineException("Missing 'newmtl' declaration in '" + this.currentFile + "'.");
        }
    }
}
