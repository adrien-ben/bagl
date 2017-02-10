package com.adrien.games.bagl.parser.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.adrien.games.bagl.rendering.Material;
import com.adrien.games.bagl.rendering.Texture;

/**
 * Super basic Wavefronts's mtl files parser.
 * <p> Only supports diffuse texture and specular exponent.
 * @author Adrien
 *
 */
public class MtlParser {

	private static final Logger log = LogManager.getLogger(MtlParser.class);
	
	private static final String SPACE_SEP = " ";
	private static final String COMMENT_LINE_FLAG = "#";
	private static final String NEW_MTL_FLAG = "newmtl";
	private static final String SPECULAR_EXPONENT_FLAG = "Ns";
	private static final String DIFFUSE_MAP_FLAG = "map_Kd";
	
	private String currentFile;
	private Map<String, Material> materials = new HashMap<>();
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
			stream.forEach(this::parseLine);
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
			if(COMMENT_LINE_FLAG.equals(first)) {
				return;
			} else if(NEW_MTL_FLAG.equals(first) && tokens.length > 1) {
				this.parseNewMaterial(tokens[1]);
			} else if(DIFFUSE_MAP_FLAG.equals(first) && tokens.length > 1) {
				this.parseDiffuseMap(tokens[1]);
			} else if(SPECULAR_EXPONENT_FLAG.equals(first) && tokens.length > 1) {
				this.parseSpecularExponent(tokens[1]);
			}
		}
	}
	
	private void parseNewMaterial(String name) {
		currentMaterial = new Material(null, 0, 2);
		materials.put(name, currentMaterial);
	}
	
	private void parseDiffuseMap(String fileName) {
		this.checkCurrentMaterial();
		String folderPath = Paths.get(this.currentFile).getParent().toString();
		String texturePath = StringUtils.join(folderPath, "/", fileName);
		this.currentMaterial.setDiffuse(new Texture(texturePath));
	}
	
	private void parseSpecularExponent(String value) {
		this.currentMaterial.setSpecularExponent(Float.parseFloat(value));
	}
	
	private void checkCurrentMaterial() {
		if(Objects.isNull(this.currentMaterial)) {
			handleParseError(StringUtils.join("Missing 'newmtl' declaration in '", this.currentFile, "'."));
		}
	}
	
	private void handleParseError(String error) {
		log.error(error);
		throw new RuntimeException(error);
	}
	
}
