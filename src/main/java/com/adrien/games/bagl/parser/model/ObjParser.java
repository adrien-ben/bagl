package com.adrien.games.bagl.parser.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.adrien.games.bagl.core.Vector2;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Material;
import com.adrien.games.bagl.rendering.Mesh;
import com.adrien.games.bagl.rendering.Vertex;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.VertexPositionNormalTexture;

/**
 * This class parses Wavefront's .obj model files and create a {@link Mesh} from it.
 * 
 * @author Adrien
 */
public class ObjParser implements ModelParser {	
	
	private static final Logger log = LogManager.getLogger(ObjParser.class);
	
	private static final String SPACE_SEP = " ";
	private static final String COMMENT_LINE_FLAG = "#";
	private static final String POSITION_LINE_FLAG = "v";
	private static final String TEXTURE_LINE_FLAG = "vt";
	private static final String NORMAL_LINE_FLAG = "vn";
	private static final String FACE_LINE_FLAG = "f";
	private static final String MTL_LIB_FLAG = "mtllib";
	private static final String USE_MTL_FLAG = "usemtl";
	
	private String currentFile;
	private int currentLine;
	private final List<Vector3> positions = new ArrayList<>();
	private final List<Vector2> coords = new ArrayList<>();
	private final List<Vector3> normals = new ArrayList<>();
	private final List<Vertex> vertices = new ArrayList<>();
	private final MtlParser mtlParser = new MtlParser();
	private final Map<String, Material> materialLib = new HashMap<>();
	private Material usedMaterial;
	
	/**
	 * {@inheritDoc}
	 * <p>Supported features : 
	 * <ul>
	 * <li>vertex position (x, y, z)
	 * <li>texture coordinates (u, v)
	 * <li>normals (x, y, z)
	 * <li>triangle faces (each vertex MUST reference a position, coordinates and normal)
	 */
	@Override
	public Mesh parse(String filePath) {
		log.info("Parsing .obj file '{}'", filePath);
		this.resetParser(filePath);
		try(Stream<String> stream = Files.lines(Paths.get(filePath))) {
			stream.forEach(this::parseLine);
		} catch (IOException e) {
			log.error("Failed to parse file '{}'.", filePath, e);
			throw new RuntimeException("Failed to parse model file", e);
		}	
		return this.build();
	}
	
	private Mesh build() {
		int vertexCount = this.vertices.size();
		Vertex[] vertexArray = new VertexPositionNormalTexture[vertexCount];
		int[] indexArray = new int[vertexCount];
		for(int i = 0; i < vertexCount; i++) {
			vertexArray[i] = this.vertices.get(i);
			indexArray[i] = i;
		}
		VertexBuffer vertexBuffer = new VertexBuffer(VertexPositionNormalTexture.DESCRIPTION, vertexArray);
		IndexBuffer indexBuffer = new IndexBuffer(indexArray);
		return new Mesh(vertexBuffer, indexBuffer, this.usedMaterial);
	}
	
	private void resetParser(String filePath) {
		this.currentFile = filePath;
		this.currentLine = 0;
		this.positions.clear();
		this.coords.clear();
		this.normals.clear();
		this.vertices.clear();
		this.materialLib.clear();
		this.usedMaterial = null;
	}
	
	private void parseLine(String line) {
		this.currentLine++;
		String[] tokens = line.split(SPACE_SEP);
		if(tokens.length > 0) {
			String first = tokens[0];
			if(COMMENT_LINE_FLAG.equals(first)) {
				return;
			} else if(POSITION_LINE_FLAG.equals(first)) {
				this.parsePosition(tokens);
			} else if(TEXTURE_LINE_FLAG.equals(first)) {
				this.parseTextureCoords(tokens);
			} else if(NORMAL_LINE_FLAG.equals(first)) {
				this.parseNormal(tokens);
			} else if(FACE_LINE_FLAG.equals(first)) {
				this.parseFace(tokens);
			} else if(MTL_LIB_FLAG.equals(first)) {
				this.parseMtlLib(tokens);
			} else if(USE_MTL_FLAG.equals(first)) {
				this.parseUseMtl(tokens);
			} else {
				log.warn("Found line flag '{}' at line {}. This line will be ignored.",  first, this.currentLine);
			}
		}
	}
	
	private void parsePosition(String[] tokens) {
		if(tokens.length >= 4) {
			float x = Float.parseFloat(tokens[1]);
			float y = Float.parseFloat(tokens[2]);
			float z = Float.parseFloat(tokens[3]);
			this.positions.add(new Vector3(x, y, z));
		} else {
			this.handleParseError(StringUtils.join("Found position with less than 3 components at line ", 
					Integer.toString(this.currentLine), "."));
		}
	}
	
	private void parseTextureCoords(String[] tokens) {
		if(tokens.length >= 3) {
			float u = Float.parseFloat(tokens[1]);
			float v = Float.parseFloat(tokens[2]);
			this.coords.add(new Vector2(u, v));
		} else {
			this.handleParseError(StringUtils.join("Found texture coordinates with less than 2 components at line ", 
					Integer.toString(this.currentLine), "."));
		}
	}

	private void parseNormal(String[] tokens) {
		if(tokens.length >= 4) {
			float x = Float.parseFloat(tokens[1]);
			float y = Float.parseFloat(tokens[2]);
			float z = Float.parseFloat(tokens[3]);
			this.normals.add(new Vector3(x, y, z));
		} else {
			this.handleParseError(StringUtils.join("Found normal with less than 3 components at line ", 
					Integer.toString(this.currentLine), "."));
		}
	}
	
	private void parseFace(String[] tokens) {
		if(tokens.length == 4) {
			this.parseFaceVertex(tokens[1]);
			this.parseFaceVertex(tokens[2]);
			this.parseFaceVertex(tokens[3]);
		} else {
			this.handleParseError(StringUtils.join("Found vertex with more or less than 3 components at line ", 
					Integer.toString(this.currentLine), ". Only triangle faces are supported."));
		}
	}
	
	private void parseFaceVertex(String faceVertex) {
		String[] tokens = faceVertex.split("/");
		if(tokens.length >= 3 && StringUtils.isNotBlank(tokens[0]) && 
				StringUtils.isNotBlank(tokens[1]) && StringUtils.isNotBlank(tokens[2])) {
			int positionIndex = Integer.parseInt(tokens[0]) - 1;
			int textCoordIndex = Integer.parseInt(tokens[1]) - 1;
			int normalIndex = Integer.parseInt(tokens[2]) - 1;
			this.reconstructVertex(positionIndex, textCoordIndex, normalIndex);
		} else {
			this.handleParseError(StringUtils.join("A face vertex does not reference a position, coords and/or a normal at line ", 
					Integer.toString(this.currentLine), ". Only triangle faces are supported."));
		}
	}
	
	private void reconstructVertex(int positionIndex, int textCoordIndex, int normalIndex) {
		this.vertices.add(new VertexPositionNormalTexture(
				this.positions.get(positionIndex),
				this.normals.get(normalIndex),
				this.coords.get(textCoordIndex)));
	}
	
	private void parseMtlLib(String[] tokens) {
		if(tokens.length > 1) {
			String fileName = this.getContentInBrackets(tokens[1]);
			String folderPath = Paths.get(this.currentFile).getParent().toString();
			String mtlPath = StringUtils.join(folderPath, "/", fileName);
			this.mtlParser.parse(mtlPath).entrySet().stream()
				.forEach(entry -> this.materialLib.put(entry.getKey(), entry.getValue()));
		} else {
			this.handleParseError(StringUtils.join("A material lib reference is incorrect at line ", 
					Integer.toString(this.currentLine), "."));
		}
	}
	
	private void parseUseMtl(String[] tokens) {
		String mtlName = getContentInBrackets(tokens[1]);
		this.usedMaterial = materialLib.get(mtlName);
		if(Objects.isNull(this.usedMaterial)) {
			handleParseError(StringUtils.join("Material '", mtlName, "' at line '", 
					Integer.toString(this.currentLine), " does not exists."));
		}
	}
	
	private String getContentInBrackets(String str) {
		return str.replaceAll("[\\[\\]]", "");
	}

	private void handleParseError(String error) {
		log.error(error);
		throw new RuntimeException(error);
	}
	
}
