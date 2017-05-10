package com.adrien.games.bagl.parser.model;

import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.*;
import com.adrien.games.bagl.rendering.vertex.MeshVertex;
import com.adrien.games.bagl.rendering.vertex.Vertex;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

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
    private final List<Vector3> tangents = new ArrayList<>();
    private final List<Face> faces = new ArrayList<>();
    private final List<Integer> faceIndices = new ArrayList<>();
    private final Map<Face, Integer> faceToIndexMap = new HashMap<>();
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
            stream.filter(StringUtils::isNotBlank).forEach(this::parseLine);
        } catch (IOException e) {
            log.error("Failed to parse file '{}'.", filePath, e);
            throw new RuntimeException("Failed to parse model file", e);
        }
        this.computeTangents();
        return this.build();
    }

    private void computeTangents() {

        Vector3[] tangents = new Vector3[this.faces.size()];

        for(int i = 0; i < this.faceIndices.size(); i+=3) {

            int index0 = this.faceIndices.get(i);
            int index1 = this.faceIndices.get(i + 1);
            int index2 = this.faceIndices.get(i + 2);

            Face face0 = this.faces.get(index0);
            Face face1 = this.faces.get(index1);
            Face face2 = this.faces.get(index2);

            Vector3 pos0 = this.positions.get(face0.getPositionIndex());
            Vector3 pos1 = this.positions.get(face1.getPositionIndex());
            Vector3 pos2 = this.positions.get(face2.getPositionIndex());
            Vector3 edge1 = Vector3.sub(pos1, pos0);
            Vector3 edge2 = Vector3.sub(pos2, pos0);

            Vector2 coords0 = this.coords.get(face0.getCoordsIndex());
            Vector2 coords1 = this.coords.get(face1.getCoordsIndex());
            Vector2 coords2 = this.coords.get(face2.getCoordsIndex());
            Vector2 deltaUVx = Vector2.sub(coords1, coords0);
            Vector2 deltaUVy = Vector2.sub(coords2, coords0);

            float f = 1/(deltaUVx.getX()*deltaUVy.getY() - deltaUVy.getX()*deltaUVx.getY());

            float x = f*(deltaUVy.getY()*edge1.getX() - deltaUVx.getY()*edge2.getX());
            float y = f*(deltaUVy.getY()*edge1.getY() - deltaUVx.getY()*edge2.getY());
            float z = f*(deltaUVy.getY()*edge1.getZ() - deltaUVx.getY()*edge2.getZ());
            Vector3 tangent = new Vector3(x, y, z);
            tangent.normalise();

            this.setTangentForFace(tangents, index0, tangent);
            this.setTangentForFace(tangents, index1, tangent);
            this.setTangentForFace(tangents, index2, tangent);
        }

        for(Vector3 tangent : tangents) {
            this.tangents.add(tangent);
        }

    }

    private void setTangentForFace(Vector3[] tangents, int index, Vector3 tangent) {
        Vector3 currentTangent = tangents[index];
        if(Objects.isNull(currentTangent)) {
            currentTangent = new Vector3(tangent);
            tangents[index] = currentTangent;
        } else {
            currentTangent.average(tangent);
        }
    }

    private Mesh build() {
        int vertexCount = this.faces.size();
        Vertex[] vertexArray = new MeshVertex[vertexCount];
        for(int i = 0; i < vertexCount; i++) {
            Face face = this.faces.get(i);
            Vector3 position = this.positions.get(face.getPositionIndex());
            Vector3 normal = this.normals.get(face.getNormalIndex());
            Vector2 coord = this.coords.get(face.getCoordsIndex());
            Vector3 tangent = this.tangents.get(i);
            vertexArray[i] = new MeshVertex(position, normal, coord, tangent);
        }

        int indexCount = this.faceIndices.size();
        int[] indexArray = new int[indexCount];
        for(int i = 0; i < indexCount; i++) {
            indexArray[i] = this.faceIndices.get(i);
        }

        VertexBuffer vertexBuffer = new VertexBuffer(MeshVertex.DESCRIPTION, BufferUsage.STATIC_DRAW, vertexArray);
        IndexBuffer indexBuffer = new IndexBuffer(BufferUsage.STATIC_DRAW, indexArray);
        return new Mesh(vertexBuffer, indexBuffer, this.usedMaterial);
    }

    private void resetParser(String filePath) {
        this.currentFile = filePath;
        this.currentLine = 0;
        this.positions.clear();
        this.coords.clear();
        this.normals.clear();
        this.tangents.clear();
        this.faces.clear();
        this.faceIndices.clear();
        this.faceToIndexMap.clear();
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
            this.handleParseError("Found position with less than 3 components at line " +  this.currentLine + ".");
        }
    }

    private void parseTextureCoords(String[] tokens) {
        if(tokens.length >= 3) {
            float u = Float.parseFloat(tokens[1]);
            float v = Float.parseFloat(tokens[2]);
            this.coords.add(new Vector2(u, v));
        } else {
            this.handleParseError("Found texture coordinates with less than 2 components at line " + this.currentLine + ".");
        }
    }

    private void parseNormal(String[] tokens) {
        if(tokens.length >= 4) {
            float x = Float.parseFloat(tokens[1]);
            float y = Float.parseFloat(tokens[2]);
            float z = Float.parseFloat(tokens[3]);
            this.normals.add(new Vector3(x, y, z));
        } else {
            this.handleParseError("Found normal with less than 3 components at line " + this.currentLine + ".");
        }
    }

    private void parseFace(String[] tokens) {
        if(tokens.length == 4) {
            this.parseFaceVertex(tokens[1]);
            this.parseFaceVertex(tokens[2]);
            this.parseFaceVertex(tokens[3]);
        } else {
            this.handleParseError("Found vertex with more or less than 3 components at line " + this.currentLine +
                    ". Only triangle faces are supported.");
        }
    }

    private void parseFaceVertex(String faceVertex) {
        String[] tokens = faceVertex.split("/");
        if(tokens.length >= 3 && StringUtils.isNotBlank(tokens[0]) &&
                StringUtils.isNotBlank(tokens[1]) && StringUtils.isNotBlank(tokens[2])) {
            int positionIndex = Integer.parseInt(tokens[0]) - 1;
            int textCoordIndex = Integer.parseInt(tokens[1]) - 1;
            int normalIndex = Integer.parseInt(tokens[2]) - 1;
            Face face = new Face(positionIndex, normalIndex, textCoordIndex);
            Integer faceIndex = this.faceToIndexMap.get(face);
            if(Objects.isNull(faceIndex)) {
                faceIndex = this.faces.size();
                this.faces.add(face);
                this.faceToIndexMap.put(face, faceIndex);
            }
            this.faceIndices.add(faceIndex);
        } else {
            this.handleParseError("A face vertex does not reference a position, coords and/or a normal at line " +
                    this.currentLine + ". Only triangle faces are supported.");
        }
    }

    private void parseMtlLib(String[] tokens) {
        if(tokens.length > 1) {
            String fileName = this.getContentInBrackets(tokens[1]);
            String folderPath = Paths.get(this.currentFile).getParent().toString();
            String mtlPath = folderPath + "/" + fileName;
            this.mtlParser.parse(mtlPath).entrySet().stream()
                    .forEach(entry -> this.materialLib.put(entry.getKey(), entry.getValue()));
        } else {
            this.handleParseError("A material lib reference is incorrect at line " + this.currentLine + ".");
        }
    }

    private void parseUseMtl(String[] tokens) {
        String mtlName = getContentInBrackets(tokens[1]);
        this.usedMaterial = materialLib.get(mtlName);
        if(Objects.isNull(this.usedMaterial)) {
            handleParseError("Material '" + mtlName + "' at line '" + this.currentLine + " does not exists.");
        }
    }

    private String getContentInBrackets(String str) {
        return str.replaceAll("[\\[\\]]", "").trim();
    }

    private void handleParseError(String error) {
        log.error(error);
        throw new RuntimeException(error);
    }

}
