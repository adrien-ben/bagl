package com.adrien.games.bagl.parser.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.adrien.games.bagl.core.Vector2;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.IndexBuffer;
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
	private static final String POSITION_LINE_FLAG = "v";
	private static final String TEXTURE_LINE_FLAG = "vt";
	private static final String NORMAL_LINE_FLAG = "vn";
	private static final String FACE_LINE_FLAG = "f";
	
	private int currentLine;
	private List<Vector3> positions = new ArrayList<>();
	private List<Vector2> coords = new ArrayList<>();
	private List<Vector3> normals = new ArrayList<>();
	private List<Vertex> vertices = new ArrayList<>();
	
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
		this.resetParser();
		try(Stream<String> stream = Files.lines(Paths.get(filePath))) {
			stream.forEach(this::parseLine);
		} catch (IOException e) {
			log.error("Failed to parse file '{}'.", filePath, e);
			throw new RuntimeException("Failed to parse model file", e);
		}	
		return this.constructMesh();
	}
	
	private Mesh constructMesh() {
		int vertexCount = this.vertices.size();
		Vertex[] vertexArray = new VertexPositionNormalTexture[vertexCount];
		int[] indexArray = new int[vertexCount];
		for(int i = 0; i < vertexCount; i++) {
			vertexArray[i] = this.vertices.get(i);
			indexArray[i] = i;
		}
		VertexBuffer vertexBuffer = new VertexBuffer(VertexPositionNormalTexture.DESCRIPTION, vertexArray);
		IndexBuffer indexBuffer = new IndexBuffer(indexArray);
		return new Mesh(vertexBuffer, indexBuffer, null);
	}
	
	private void resetParser() {
		this.currentLine = 0;
		this.positions.clear();
		this.coords.clear();
		this.normals.clear();
		this.vertices.clear();
	}
	
	private void parseLine(String line) {
		this.currentLine++;
		String[] tokens = line.split(SPACE_SEP);
		if(tokens.length > 0) {
			String first = tokens[0];
			switch (first) {
			case POSITION_LINE_FLAG:
				this.parsePosition(tokens);
				break;
			case TEXTURE_LINE_FLAG:
				this.parseTextureCoords(tokens);
				break;
			case NORMAL_LINE_FLAG:
				this.parseNormal(tokens);
				break;
			case FACE_LINE_FLAG:
				this.parseFace(tokens);
				break;
			default:
				log.warn("Found line flag '{}' at line {}. This line will be ignored.", 
						first, this.currentLine);
				break;
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

	private void handleParseError(String error) {
		log.error(error);
		throw new RuntimeException(error);
	}
	
}
