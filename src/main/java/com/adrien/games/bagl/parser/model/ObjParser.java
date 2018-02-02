package com.adrien.games.bagl.parser.model;

import com.adrien.games.bagl.core.EngineException;
import com.adrien.games.bagl.rendering.BufferUsage;
import com.adrien.games.bagl.rendering.Material;
import com.adrien.games.bagl.rendering.model.Mesh;
import com.adrien.games.bagl.rendering.model.Model;
import com.adrien.games.bagl.rendering.vertex.IndexBuffer;
import com.adrien.games.bagl.rendering.vertex.VertexBuffer;
import com.adrien.games.bagl.rendering.vertex.VertexBufferParams;
import com.adrien.games.bagl.rendering.vertex.VertexElement;
import com.adrien.games.bagl.utils.Tuple2;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * This class parses Wavefront's .obj model files and create a {@link Mesh} from it
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
    private static final String OBJECT_LINE_FLAG = "o";
    private static final String MTL_LIB_FLAG = "mtllib";
    private static final String USE_MTL_FLAG = "usemtl";

    private static final String DEFAULT_MESH_NAME = "default";

    private String currentFile;
    private int currentLine;

    private final Map<String, MeshBuilder> builders = new HashMap<>();
    private MeshBuilder currentBuilder = new MeshBuilder(DEFAULT_MESH_NAME);

    private final MtlParser mtlParser = new MtlParser();
    private final Map<String, Material> materialLib = new HashMap<>();

    /**
     * {@inheritDoc}
     * <p>Supported features :
     * <ul>
     * <li>vertex position (x, y, z)
     * <li>texture coordinates (u, v)
     * <li>normals (x, y, z)
     * <li>polygonal faces (each vertex MUST reference a position and normal)
     * <li>Multi-mesh models</li>
     */
    @Override
    public Model parse(String filePath) {
        log.info("Parsing .obj file '{}'", filePath);
        final Model model = new Model();
        this.resetParser(filePath);
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            stream.filter(StringUtils::isNotBlank).forEach(this::parseLine);
        } catch (IOException e) {
            log.error("Failed to parse file '{}'.", filePath, e);
            throw new EngineException("Failed to parse model file", e);
        }
        this.builders.put(this.currentBuilder.meshName, this.currentBuilder);
        this.builders.values()
                .stream()
                .filter(MeshBuilder::isNotEmpty)
                .map(MeshBuilder::build)
                .forEach(tuple -> model.addMesh(tuple.getFirst(), tuple.getSecond()));
        return model;
    }

    private void resetParser(String filePath) {
        this.currentFile = filePath;
        this.currentLine = 0;
        this.builders.clear();
        MeshBuilder.clear();
        this.currentBuilder = new MeshBuilder(DEFAULT_MESH_NAME);
        this.materialLib.clear();
    }

    private void parseLine(String line) {
        this.currentLine++;
        final String[] tokens = line.split(SPACE_SEP);
        if (tokens.length > 0) {
            final String first = tokens[0];
            if (POSITION_LINE_FLAG.equals(first)) {
                this.parsePosition(tokens);
            } else if (TEXTURE_LINE_FLAG.equals(first)) {
                this.parseTextureCoords(tokens);
            } else if (NORMAL_LINE_FLAG.equals(first)) {
                this.parseNormal(tokens);
            } else if (FACE_LINE_FLAG.equals(first)) {
                this.parseFace(tokens);
            } else if (OBJECT_LINE_FLAG.equals(first)) {
                this.parseNewObject(tokens);
            } else if (MTL_LIB_FLAG.equals(first)) {
                this.parseMtlLib(tokens);
            } else if (USE_MTL_FLAG.equals(first)) {
                this.parseUseMtl(tokens);
            } else {
                log.warn("Found line flag '{}' at line {}. This line will be ignored.", first, this.currentLine);
            }
        }
    }

    private void parsePosition(String[] tokens) {
        if (tokens.length >= 4) {
            final float x = Float.parseFloat(tokens[1]);
            final float y = Float.parseFloat(tokens[2]);
            final float z = Float.parseFloat(tokens[3]);
            MeshBuilder.positions.add(new Vector3f(x, y, z));
        } else {
            this.handleParseError("Found position with less than 3 components at line " + this.currentLine + ".");
        }
    }

    private void parseTextureCoords(String[] tokens) {
        if (tokens.length >= 3) {
            final float u = Float.parseFloat(tokens[1]);
            final float v = Float.parseFloat(tokens[2]);
            MeshBuilder.coordinates.add(new Vector2f(u, v));
        } else {
            this.handleParseError("Found texture coordinates with less than 2 components at line " + this.currentLine + ".");
        }
    }

    private void parseNormal(String[] tokens) {
        if (tokens.length >= 4) {
            final float x = Float.parseFloat(tokens[1]);
            final float y = Float.parseFloat(tokens[2]);
            final float z = Float.parseFloat(tokens[3]);
            MeshBuilder.normals.add(new Vector3f(x, y, z));
        } else {
            this.handleParseError("Found normal with less than 3 components at line " + this.currentLine + ".");
        }
    }

    private void parseFace(String[] tokens) {
        if (tokens.length > 3) {
            final int vertexCount = tokens.length - 1;
            for (int i = 0; i < vertexCount - 2; i++) {
                this.parseFaceVertex(tokens[1]);
                this.parseFaceVertex(tokens[i + 2]);
                this.parseFaceVertex(tokens[i + 3]);
            }
        } else {
            this.handleParseError("Found face with less than 3 vertices at line " + this.currentLine + ".");
        }
    }

    private void parseFaceVertex(String faceVertex) {
        final String[] tokens = faceVertex.split("/");
        if (tokens.length >= 3 && StringUtils.isNotBlank(tokens[0]) && StringUtils.isNotBlank(tokens[2])) {
            final int positionIndex = Integer.parseInt(tokens[0]) - 1;
            final int textCoordIndex = StringUtils.isNotBlank(tokens[1]) ? Integer.parseInt(tokens[1]) - 1 : -1;
            final int normalIndex = Integer.parseInt(tokens[2]) - 1;
            final Face face = new Face(positionIndex, normalIndex, textCoordIndex);

            final Integer faceIndex = this.currentBuilder.faceToIndexMap.computeIfAbsent(face, key -> {
                final int nextFaceIndex = this.currentBuilder.faces.size();
                this.currentBuilder.faces.add(key);
                return nextFaceIndex;
            });
            this.currentBuilder.faceIndices.add(faceIndex);
        } else {
            this.handleParseError("A face vertex does not reference a position, coordinates and/or a normal at line " +
                    this.currentLine + ". Only triangle faces are supported.");
        }
    }

    private void parseNewObject(String[] tokens) {
        if (tokens.length > 1) {
            this.builders.put(this.currentBuilder.meshName, this.currentBuilder);
            this.currentBuilder = new MeshBuilder(tokens[1]);
        } else {
            this.handleParseError("A new object declaration misses an object name at line " + this.currentLine + ".");
        }
    }

    private void parseMtlLib(String[] tokens) {
        if (tokens.length > 1) {
            final String fileName = this.getContentInBrackets(tokens[1]);
            final String folderPath = Paths.get(this.currentFile).getParent().toString();
            final String mtlPath = folderPath + "/" + fileName;
            this.mtlParser.parse(mtlPath).forEach(this.materialLib::put);
        } else {
            this.handleParseError("A material lib reference is incorrect at line " + this.currentLine + ".");
        }
    }

    private void parseUseMtl(String[] tokens) {
        final String mtlName = getContentInBrackets(tokens[1]);
        this.currentBuilder.material = materialLib.get(mtlName);
        if (Objects.isNull(this.currentBuilder.material)) {
            this.handleParseError("Material '" + mtlName + "' at line '" + this.currentLine + " does not exists.");
        }
    }

    private String getContentInBrackets(String str) {
        return str.replaceAll("[\\[\\]]", "").trim();
    }

    private void handleParseError(String error) {
        log.error(error);
        throw new EngineException(error);
    }

    /**
     * Internal mesh builder
     */
    private static class MeshBuilder {

        private final static List<Vector3f> positions = new ArrayList<>();
        private final static List<Vector2f> coordinates = new ArrayList<>();
        private final static List<Vector3f> normals = new ArrayList<>();

        private final String meshName;
        private final List<Vector3f> tangents = new ArrayList<>();
        private final List<Face> faces = new ArrayList<>();
        private final List<Integer> faceIndices = new ArrayList<>();
        private final Map<Face, Integer> faceToIndexMap = new HashMap<>();
        private Material material;

        private MeshBuilder(String meshName) {
            this.meshName = meshName;
        }

        private boolean isNotEmpty() {
            return !this.faces.isEmpty();
        }

        private Tuple2<Mesh, Material> build() {
            if (this.material.hasNormalMap()) {
                this.computeTangents();
            }

            final VertexBuffer vBuffer = this.generateVertexBuffer();
            final IndexBuffer indexBuffer = this.generateIndexBuffer();

            return new Tuple2<>(new Mesh(vBuffer, indexBuffer), this.material);
        }

        private IndexBuffer generateIndexBuffer() {
            final int indexCount = this.faceIndices.size();
            final IntBuffer indices = MemoryUtil.memAllocInt(indexCount);
            for (int i = 0; i < indexCount; i++) {
                indices.put(i, this.faceIndices.get(i));
            }
            final IndexBuffer indexBuffer = new IndexBuffer(indices, BufferUsage.STATIC_DRAW);
            MemoryUtil.memFree(indices);
            return indexBuffer;
        }

        private VertexBuffer generateVertexBuffer() {
            final int vertexCount = this.faces.size();
            final FloatBuffer vertices = MemoryUtil.memAllocFloat(vertexCount * Mesh.ELEMENTS_PER_VERTEX);
            for (int i = 0; i < vertexCount; i++) {
                final Face face = this.faces.get(i);
                final Vector3f position = MeshBuilder.positions.get(face.getPositionIndex());
                final Vector3f normal = MeshBuilder.normals.get(face.getNormalIndex());
                final Vector2f coordinates = face.getCoordsIndex() > -1 ? MeshBuilder.coordinates.get(face.getCoordsIndex()) : new Vector2f();
                final Vector3f tangent = this.tangents.isEmpty() ? new Vector3f() : this.tangents.get(i);

                final int index = i * Mesh.ELEMENTS_PER_VERTEX;
                vertices.put(index, position.x());
                vertices.put(index + 1, position.y());
                vertices.put(index + 2, position.z());
                vertices.put(index + 3, normal.x());
                vertices.put(index + 4, normal.y());
                vertices.put(index + 5, normal.z());
                vertices.put(index + 6, coordinates.x());
                vertices.put(index + 7, coordinates.y());
                vertices.put(index + 8, tangent.x());
                vertices.put(index + 9, tangent.y());
                vertices.put(index + 10, tangent.z());
            }
            final VertexBuffer vBuffer = new VertexBuffer(vertices, VertexBufferParams.builder()
                    .element(new VertexElement(Mesh.POSITION_INDEX, Mesh.ELEMENTS_PER_POSITION))
                    .element(new VertexElement(Mesh.NORMAL_INDEX, Mesh.ELEMENTS_PER_NORMAL))
                    .element(new VertexElement(Mesh.COORDINATES_INDEX, Mesh.ELEMENTS_PER_COORDINATES))
                    .element(new VertexElement(Mesh.TANGENT_INDEX, Mesh.ELEMENTS_PER_TANGENT))
                    .build());
            MemoryUtil.memFree(vertices);
            return vBuffer;
        }

        private void computeTangents() {
            final Vector3f[] tangents = new Vector3f[this.faces.size()];
            for (int i = 0; i < this.faceIndices.size(); i += 3) {

                final int index0 = this.faceIndices.get(i);
                final int index1 = this.faceIndices.get(i + 1);
                final int index2 = this.faceIndices.get(i + 2);

                final Face face0 = this.faces.get(index0);
                final Face face1 = this.faces.get(index1);
                final Face face2 = this.faces.get(index2);

                final Vector3f pos0 = MeshBuilder.positions.get(face0.getPositionIndex());
                final Vector3f pos1 = MeshBuilder.positions.get(face1.getPositionIndex());
                final Vector3f pos2 = MeshBuilder.positions.get(face2.getPositionIndex());
                final Vector3f edge1 = new Vector3f(pos1).sub(pos0);
                final Vector3f edge2 = new Vector3f(pos2).sub(pos0);

                final Vector2f coordinates0 = MeshBuilder.coordinates.get(face0.getCoordsIndex());
                final Vector2f coordinates1 = MeshBuilder.coordinates.get(face1.getCoordsIndex());
                final Vector2f coordinates2 = MeshBuilder.coordinates.get(face2.getCoordsIndex());
                final Vector2f deltaUVx = new Vector2f(coordinates1).sub(coordinates0);
                final Vector2f deltaUVy = new Vector2f(coordinates2).sub(coordinates0);

                final float f = 1f / (deltaUVx.x() * deltaUVy.y() - deltaUVy.x() * deltaUVx.y());

                final float x = f * (deltaUVy.y() * edge1.x() - deltaUVx.y() * edge2.x());
                final float y = f * (deltaUVy.y() * edge1.y() - deltaUVx.y() * edge2.y());
                final float z = f * (deltaUVy.y() * edge1.z() - deltaUVx.y() * edge2.z());
                final Vector3f tangent = new Vector3f(x, y, z).normalize();

                this.setTangentForFace(tangents, index0, tangent);
                this.setTangentForFace(tangents, index1, tangent);
                this.setTangentForFace(tangents, index2, tangent);
            }
            Collections.addAll(this.tangents, tangents);
        }

        private void setTangentForFace(final Vector3f[] tangents, final int index, final Vector3f tangent) {
            Vector3f currentTangent = tangents[index];
            if (Objects.isNull(currentTangent)) {
                currentTangent = new Vector3f(tangent);
                tangents[index] = currentTangent;
            } else {
                currentTangent.add(tangent).mul(0.5f);
            }
        }

        private static void clear() {
            MeshBuilder.positions.clear();
            MeshBuilder.coordinates.clear();
            MeshBuilder.normals.clear();
        }
    }
}
