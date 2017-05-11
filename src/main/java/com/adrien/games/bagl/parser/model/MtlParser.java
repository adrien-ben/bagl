package com.adrien.games.bagl.parser.model;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Configuration;
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
 * Super basic Wavefronts's mtl files parser.
 * <p> Only supports :
 * <ul>
 * <li>diffuse color (Kd R G B)
 * <li>diffuse map (map_Kd RELATIV_FILEPATH)
 * <li>specular color (Ks RGB)
 * <li>specular map (map_Ks RELATIV_FILEPATH)
 * <li>specular exponent (Ns VALUE)
 * @author Adrien
 *
 */
public class MtlParser {

    private static final Logger log = LogManager.getLogger(MtlParser.class);

    private static final String SPACE_SEP = " ";
    private static final String NEW_MTL_FLAG = "newmtl";
    private static final String DIFFUSE_COLOR_FLAG = "Kd";
    private static final String SPECULAR_COLOR_FLAG = "Ks";
    private static final String SPECULAR_EXPONENT_FLAG = "Ns";
    private static final String DIFFUSE_MAP_FLAG = "map_Kd";
    private static final String SPECULAR_MAP_FLAG = "map_Ks";
    private static final String BUMP_MAP_FLAG = "bump|map_bump";

    private String currentFile;
    private final Map<String, Material> materials = new HashMap<>();
    private Material currentMaterial;

    /**
     * Parses a Wavefronts's mtl file.
     * @param filePath The file to parse.
     * @return A map with material name as key and a {@link Material} as value.
     */
    public Map<String, Material> parse(String filePath) {
        log.info("Parsing .mtl file '{}'.", filePath);
        this.reset(filePath);
        try(Stream<String> stream = Files.lines(Paths.get(filePath))) {
            stream.filter(StringUtils::isNotBlank).forEach(this::parseLine);
        } catch (IOException e) {
            log.error("Failed to parse file '{}'.", filePath, e);
            throw new RuntimeException("Failed to parse material file", e);
        }
        return materials;
    }

    private void reset(String filePath) {
        this.currentFile = filePath;
        this.materials.clear();
        this.currentMaterial = null;
    }

    private void parseLine(String line) {
        String[] tokens = line.split(SPACE_SEP);
        if(tokens.length > 0) {
            String first = tokens[0];
            if(NEW_MTL_FLAG.equals(first) && tokens.length > 1) {
                this.parseNewMaterial(tokens[1]);
            } else if(DIFFUSE_MAP_FLAG.equals(first) && tokens.length > 1) {
                this.parseDiffuseMap(tokens[1]);
            } else if(SPECULAR_EXPONENT_FLAG.equals(first) && tokens.length > 1) {
                this.parseSpecularExponent(tokens[1]);
            } else if(SPECULAR_COLOR_FLAG.equals(first) && tokens.length > 3) {
                this.parseSpecularColor(tokens);
            } else if(DIFFUSE_COLOR_FLAG.equals(first) && tokens.length > 3) {
                this.parseDiffuseColor(tokens);
            } else if(SPECULAR_MAP_FLAG.equals(first) && tokens.length > 1) {
                this.parseSpecularMap(tokens[1]);
            } else if(first.matches(BUMP_MAP_FLAG) && tokens.length > 1) {
                this.parseBumpMap(tokens[1]);
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

    private void parseSpecularExponent(String value) {
        this.checkCurrentMaterial();
        this.currentMaterial.setSpecularExponent(Float.parseFloat(value));
    }

    private void parseSpecularColor(String[] tokens) {
        this.checkCurrentMaterial();
        this.currentMaterial.setSpecularIntensity(Float.parseFloat(tokens[1]));
    }

    private void parseDiffuseColor(String[] tokens) {
        this.checkCurrentMaterial();
        float r = Float.parseFloat(tokens[1]);
        float g = Float.parseFloat(tokens[2]);
        float b = Float.parseFloat(tokens[3]);
        this.currentMaterial.setDiffuseColor(new Color(r,g ,b));
    }

    private void parseSpecularMap(String fileName) {
        this.checkCurrentMaterial();
        this.currentMaterial.setSpecularMap(this.loadTexture(fileName));
    }

    private void parseBumpMap(String fileName) {
        this.checkCurrentMaterial();
        this.currentMaterial.setBumpMap(this.loadTexture(fileName));
    }

    private Texture loadTexture(String name) {
        String folderPath = Paths.get(this.currentFile).getParent().toString();
        String texturePath = folderPath + "/" + name;
        TextureParameters parameters = new TextureParameters().mipmaps(true).minFilter(Filter.MIPMAP_LINEAR_LINEAR)
                .anisotropic(Configuration.getInstance().getAnisotropicLevel());
        return new Texture(texturePath, parameters);
    }

    private void checkCurrentMaterial() {
        if(Objects.isNull(this.currentMaterial)) {
            handleParseError("Missing 'newmtl' declaration in '" + this.currentFile + "'.");
        }
    }

    private void handleParseError(String error) {
        log.error(error);
        throw new RuntimeException(error);
    }

}
