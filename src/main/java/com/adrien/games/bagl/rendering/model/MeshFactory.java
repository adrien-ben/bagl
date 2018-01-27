package com.adrien.games.bagl.rendering.model;

import com.adrien.games.bagl.rendering.BufferUsage;
import com.adrien.games.bagl.rendering.DataType;
import com.adrien.games.bagl.rendering.PrimitiveType;
import com.adrien.games.bagl.rendering.vertex.*;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Mesh factory
 *
 * @author adrien
 */
public class MeshFactory {

    private final static byte UNIT_CUBE_POS_HALF_SIZE = (byte) 1;
    private final static byte UNIT_CUBE_NEG_HALF_SIZE = (byte) -1;

    /**
     * Create a plane mesh
     * <p>
     * The plane's center is (0, 0, 0), it has normals, but no tangents
     * nor texture coordinates. The plane is oriented upward
     *
     * @param width The width of the plane
     * @param depth The height of the plane
     * @return A {@link Mesh}
     */
    public static Mesh createPlane(final float width, final float depth) {
        final float halfWidth = width * 0.5f;
        final float halfDepth = depth * 0.5f;

        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final FloatBuffer vertices = stack.floats(-halfWidth, 0, halfDepth, 0, 1, 0,
                    halfWidth, 0, halfDepth, 0, 1, 0,
                    -halfWidth, 0, -halfDepth, 0, 1, 0,
                    halfWidth, 0, -halfDepth, 0, 1, 0);

            final VertexBuffer vBuffer = new VertexBuffer(vertices, VertexBufferParams.builder()
                    .element(new VertexElement(Mesh.POSITION_INDEX, Mesh.ELEMENTS_PER_POSITION))
                    .element(new VertexElement(Mesh.NORMAL_INDEX, Mesh.ELEMENTS_PER_NORMAL))
                    .build());
            final VertexArray vArray = new VertexArray();
            vArray.bind();
            vArray.attachVertexBuffer(vBuffer);
            vArray.unbind();
            return new Mesh(vBuffer, vArray, PrimitiveType.TRIANGLE_STRIP);
        }
    }

    /**
     * Create a quad for full screen rendering
     * <p>
     * This method generates an un-indexed mesh with position
     * and texture coordinates
     *
     * @return A new {@link Mesh}
     */
    public static Mesh createScreenQuad() {
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final ByteBuffer positions = stack.bytes(
                    (byte) -1, (byte) -1, (byte) 0, (byte) 0,
                    (byte) 1, (byte) -1, Byte.MAX_VALUE, (byte) 0,
                    (byte) -1, (byte) 1, (byte) 0, Byte.MAX_VALUE,
                    (byte) 1, (byte) 1, Byte.MAX_VALUE, Byte.MAX_VALUE);
            final VertexBuffer vBuffer = new VertexBuffer(positions, VertexBufferParams.builder()
                    .dataType(DataType.BYTE)
                    .element(new VertexElement(0, 2))
                    .element(new VertexElement(2, 2, true))
                    .build());

            final VertexArray vArray = new VertexArray();
            vArray.bind();
            vArray.attachVertexBuffer(vBuffer);
            vArray.unbind();
            return new Mesh(vBuffer, vArray, PrimitiveType.TRIANGLE_STRIP);
        }
    }

    /**
     * Create a mesh to render cube maps
     * <p>
     * The method generates an indexed mesh with only position data
     *
     * @return A new {@link Mesh}
     */
    public static Mesh createCubeMapMesh() {
        final IndexBuffer iBuffer;
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final ByteBuffer indices = stack.bytes(
                    (byte) 1, (byte) 0, (byte) 3, (byte) 3, (byte) 0, (byte) 2,
                    (byte) 5, (byte) 1, (byte) 7, (byte) 7, (byte) 1, (byte) 3,
                    (byte) 4, (byte) 5, (byte) 6, (byte) 6, (byte) 5, (byte) 7,
                    (byte) 0, (byte) 4, (byte) 2, (byte) 2, (byte) 4, (byte) 6,
                    (byte) 6, (byte) 7, (byte) 2, (byte) 2, (byte) 7, (byte) 3,
                    (byte) 0, (byte) 1, (byte) 4, (byte) 4, (byte) 1, (byte) 5
            );
            iBuffer = new IndexBuffer(indices, BufferUsage.STATIC_DRAW);
        }

        final VertexBuffer vBuffer;
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final ByteBuffer vertices = stack.bytes(
                    UNIT_CUBE_NEG_HALF_SIZE, UNIT_CUBE_NEG_HALF_SIZE, UNIT_CUBE_POS_HALF_SIZE,
                    UNIT_CUBE_POS_HALF_SIZE, UNIT_CUBE_NEG_HALF_SIZE, UNIT_CUBE_POS_HALF_SIZE,
                    UNIT_CUBE_NEG_HALF_SIZE, UNIT_CUBE_POS_HALF_SIZE, UNIT_CUBE_POS_HALF_SIZE,
                    UNIT_CUBE_POS_HALF_SIZE, UNIT_CUBE_POS_HALF_SIZE, UNIT_CUBE_POS_HALF_SIZE,
                    UNIT_CUBE_NEG_HALF_SIZE, UNIT_CUBE_NEG_HALF_SIZE, UNIT_CUBE_NEG_HALF_SIZE,
                    UNIT_CUBE_POS_HALF_SIZE, UNIT_CUBE_NEG_HALF_SIZE, UNIT_CUBE_NEG_HALF_SIZE,
                    UNIT_CUBE_NEG_HALF_SIZE, UNIT_CUBE_POS_HALF_SIZE, UNIT_CUBE_NEG_HALF_SIZE,
                    UNIT_CUBE_POS_HALF_SIZE, UNIT_CUBE_POS_HALF_SIZE, UNIT_CUBE_NEG_HALF_SIZE);
            vBuffer = new VertexBuffer(vertices, VertexBufferParams.builder()
                    .dataType(DataType.BYTE)
                    .element(new VertexElement(0, 3))
                    .build());
        }

        final VertexArray vArray = new VertexArray();
        vArray.bind();
        vArray.attachVertexBuffer(vBuffer);
        vArray.unbind();
        return new Mesh(vBuffer, vArray, iBuffer);
    }

    /**
     * Create a cube mesh
     * <p>
     * The cube's center is (0, 0, 0), it has normals, but no tangents
     * nor texture coordinates
     *
     * @param size The size of the cube
     * @return A {@link Mesh}
     */
    public static Mesh createCube(final float size) {
        final float halfSize = size * 0.5f;

        final IndexBuffer iBuffer;
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final ByteBuffer indices = stack.bytes(
                    (byte) 0, (byte) 1, (byte) 2, (byte) 2, (byte) 3, (byte) 0, // back face
                    (byte) 4, (byte) 5, (byte) 6, (byte) 6, (byte) 7, (byte) 4, //right face
                    (byte) 8, (byte) 9, (byte) 10, (byte) 10, (byte) 11, (byte) 8, // front face
                    (byte) 12, (byte) 13, (byte) 14, (byte) 14, (byte) 15, (byte) 12, //left face
                    (byte) 16, (byte) 17, (byte) 18, (byte) 18, (byte) 19, (byte) 16, // bottom face
                    (byte) 20, (byte) 21, (byte) 22, (byte) 22, (byte) 23, (byte) 20 // top face
            );
            iBuffer = new IndexBuffer(indices, BufferUsage.STATIC_DRAW);
        }

        final VertexBuffer vBuffer;
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final FloatBuffer vertices = stack.floats(
                    // back face
                    -halfSize, -halfSize, halfSize, 0, 0, 1,
                    halfSize, -halfSize, halfSize, 0, 0, 1,
                    halfSize, halfSize, halfSize, 0, 0, 1,
                    -halfSize, halfSize, halfSize, 0, 0, 1,

                    // right face
                    halfSize, -halfSize, halfSize, 1, 0, 0,
                    halfSize, -halfSize, -halfSize, 1, 0, 0,
                    halfSize, halfSize, -halfSize, 1, 0, 0,
                    halfSize, halfSize, halfSize, 1, 0, 0,

                    // front face
                    halfSize, -halfSize, -halfSize, 0, 0, -1,
                    -halfSize, -halfSize, -halfSize, 0, 0, -1,
                    -halfSize, halfSize, -halfSize, 0, 0, -1,
                    halfSize, halfSize, -halfSize, 0, 0, -1,

                    // left face
                    -halfSize, -halfSize, -halfSize, -1, 0, 0,
                    -halfSize, -halfSize, halfSize, -1, 0, 0,
                    -halfSize, halfSize, halfSize, -1, 0, 0,
                    -halfSize, halfSize, -halfSize, -1, 0, 0,

                    // bottom face
                    -halfSize, -halfSize, -halfSize, 0, -1, 0,
                    halfSize, -halfSize, -halfSize, 0, -1, 0,
                    halfSize, -halfSize, halfSize, 0, -1, 0,
                    -halfSize, -halfSize, halfSize, 0, -1, 0,

                    // top face
                    -halfSize, halfSize, halfSize, 0, 1, 0,
                    halfSize, halfSize, halfSize, 0, 1, 0,
                    halfSize, halfSize, -halfSize, 0, 1, 0,
                    -halfSize, halfSize, -halfSize, 0, 1, 0
            );
            vBuffer = new VertexBuffer(vertices, VertexBufferParams.builder()
                    .element(new VertexElement(Mesh.POSITION_INDEX, Mesh.ELEMENTS_PER_POSITION))
                    .element(new VertexElement(Mesh.NORMAL_INDEX, Mesh.ELEMENTS_PER_NORMAL))
                    .build());
        }

        final VertexArray vArray = new VertexArray();
        vArray.bind();
        vArray.attachVertexBuffer(vBuffer);
        vArray.unbind();

        return new Mesh(vBuffer, vArray, iBuffer);
    }

    /**
     * Create a sphere mesh
     * <p>
     * The sphere's center is (0, 0, 0), it has normals, but no tangents
     * nor texture coordinates
     *
     * @param radius   The radius of the sphere
     * @param rings    The number of horizontal subdivisions
     * @param segments The number of horizontal subdivisions
     * @return A {@link Mesh}
     */
    public static Mesh createSphere(final float radius, final int rings, final int segments) {

        final FloatBuffer vertices = MemoryUtil.memAllocFloat((rings * segments + 2) * 6);
        int bufferIt = 0;
        for (int i = 1; i <= rings; i++) {
            for (int j = 0; j < segments; j++) {
                final float theta = (float) Math.PI * i / (rings + 1);
                final float phi = (float) Math.PI * 2 * j / segments;

                final float x = (float) Math.sin(theta) * (float) Math.sin(phi);
                final float y = (float) Math.cos(theta);
                final float z = (float) Math.sin(theta) * (float) Math.cos(phi);

                bufferIt += MeshFactory.insertElement3f(vertices, bufferIt, radius * x, radius * y, radius * z);
                bufferIt += MeshFactory.insertElement3f(vertices, bufferIt, x, y, z);
            }
        }

        // top vertex
        bufferIt += MeshFactory.insertElement3f(vertices, bufferIt, 0, radius, 0);
        bufferIt += MeshFactory.insertElement3f(vertices, bufferIt, 0, 1, 0);

        // bottom vertex
        bufferIt += MeshFactory.insertElement3f(vertices, bufferIt, 0, -radius, 0);
        MeshFactory.insertElement3f(vertices, bufferIt, 0, -1, 0);

        final VertexBuffer vBuffer = new VertexBuffer(vertices, VertexBufferParams.builder()
                .element(new VertexElement(Mesh.POSITION_INDEX, Mesh.ELEMENTS_PER_POSITION))
                .element(new VertexElement(Mesh.NORMAL_INDEX, Mesh.ELEMENTS_PER_NORMAL))
                .build());
        MemoryUtil.memFree(vertices);

        final VertexArray vArray = new VertexArray();
        vArray.bind();
        vArray.attachVertexBuffer(vBuffer);
        vArray.unbind();

        final IntBuffer indices = MemoryUtil.memAllocInt(segments * (rings - 1) * 6 + 6 * segments);
        bufferIt = 0;
        for (int i = 0; i < rings - 1; i++) {
            for (int j = 0; j < segments; j++) {
                final int index0 = rings * i + j;
                final int index1 = rings * (i + 1) + j;
                final int index2 = rings * (i + 1) + (j + 1) % segments;
                final int index3 = rings * i + (j + 1) % segments;

                bufferIt += MeshFactory.insertElement3i(indices, bufferIt, index0, index1, index2);
                bufferIt += MeshFactory.insertElement3i(indices, bufferIt, index2, index3, index0);
            }
        }

        final int topVertexIndex = rings * segments;
        final int bottomVertexIndex = rings * segments + 1;
        for (int i = 0; i < segments; i++) {
            // top faces
            bufferIt += MeshFactory.insertElement3i(indices, bufferIt, topVertexIndex, i, (i + 1) % segments);

            // bottom faces
            bufferIt += MeshFactory.insertElement3i(indices, bufferIt, rings * (segments - 1) + i,
                    bottomVertexIndex, rings * (segments - 1) + (i + 1) % segments);
        }

        final IndexBuffer iBuffer = new IndexBuffer(indices, BufferUsage.STATIC_DRAW);
        MemoryUtil.memFree(indices);

        return new Mesh(vBuffer, vArray, iBuffer);
    }

    /**
     * Create a truncated cone mesh
     * <p>
     * The cone's center is (0, 0, 0), it has normals, but no tangents
     * nor texture coordinates
     *
     * @param baseRadius The radius of the base
     * @param topRadius  The radius of the top
     * @param height     The height of the cone
     * @param segments   The number of horizontal subdivisions
     * @return A {@link Mesh}
     */
    public static Mesh createCylinder(final float baseRadius, final float topRadius, final float height, final int segments) {
        final VertexBuffer vBuffer = MeshFactory.createConeVertices(baseRadius, topRadius, height, segments);

        final IndexBuffer iBuffer;
        int bufferIt = 0;
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer indices = stack.mallocInt(segments * 2 * 3 + segments * 6);

            // base
            for (int i = 0; i < segments; i++) {
                final int offset = 2;
                bufferIt += MeshFactory.insertElement3i(indices, bufferIt, 0, offset + (i + 1) % segments, offset + i);
            }

            // top
            for (int i = 0; i < segments; i++) {
                final int offset = segments + 2;
                bufferIt += MeshFactory.insertElement3i(indices, bufferIt, 1, offset + i, offset + (i + 1) % segments);
            }

            // side
            for (int i = 0; i < segments; i++) {
                final int offset = 2 * (segments + 1);
                final int index0 = offset + i * 2;
                final int index1 = offset + i * 2 + 1;
                final int index2 = offset + (i * 2 + 3) % (2 * segments);
                final int index3 = offset + (i * 2 + 2) % (2 * segments);

                bufferIt += MeshFactory.insertElement3i(indices, bufferIt, index0, index1, index2);
                bufferIt += MeshFactory.insertElement3i(indices, bufferIt, index2, index3, index0);
            }
            iBuffer = new IndexBuffer(indices, BufferUsage.STATIC_DRAW);
        }

        final VertexArray vArray = new VertexArray();
        vArray.bind();
        vArray.attachVertexBuffer(vBuffer);
        vArray.unbind();

        return new Mesh(vBuffer, vArray, iBuffer);
    }

    /**
     * Create a vertex buffer containing the vertices of a cone
     *
     * @param baseRadius The radius of the base
     * @param topRadius  The radius of the top
     * @param height     The height of the cone
     * @param segments   The number of horizontal subdivisions
     * @return A {@link VertexBuffer}
     */
    private static VertexBuffer createConeVertices(final float baseRadius, final float topRadius, final float height, final int segments) {
        final float halfHeight = height * 0.5f;
        int bufferIt = 0;
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final FloatBuffer vertices = stack.mallocFloat((segments * 4 + 2) * 6);
            // base center
            bufferIt += MeshFactory.insertElement3f(vertices, bufferIt, 0, -halfHeight, 0);
            bufferIt += MeshFactory.insertElement3f(vertices, bufferIt, 0, -1, 0);

            // top center
            bufferIt += MeshFactory.insertElement3f(vertices, bufferIt, 0, halfHeight, 0);
            bufferIt += MeshFactory.insertElement3f(vertices, bufferIt, 0, 1, 0);

            int baseIt = 2 * 6;
            int topIt = (2 + segments) * 6;
            for (int i = 0; i < segments; i++) {
                final float theta = 2 * (float) Math.PI * i / segments;
                final float x = (float) Math.cos(theta);
                final float z = -(float) Math.sin(theta);

                // base vertex
                baseIt += MeshFactory.insertElement3f(vertices, baseIt, x * baseRadius, -halfHeight, z * baseRadius);
                baseIt += MeshFactory.insertElement3f(vertices, baseIt, 0, -1, 0);

                // top vertex
                topIt += MeshFactory.insertElement3f(vertices, topIt, x * topRadius, halfHeight, z * topRadius);
                topIt += MeshFactory.insertElement3f(vertices, topIt, 0, 1, 0);

                bufferIt += 12;
            }

            final Vector3f tangent = new Vector3f();
            final Vector3f biTangent = new Vector3f();
            final Vector3f normal = new Vector3f();

            // side vertices
            for (int i = 0; i < segments; i++) {
                final float theta = 2 * (float) Math.PI * i / segments;
                final float x = (float) Math.cos(theta);
                final float z = -(float) Math.sin(theta);

                // normal computation
                final Vector3f top = new Vector3f(x * topRadius, halfHeight, z * topRadius);
                final Vector3f bottom = new Vector3f(x * baseRadius, -halfHeight, z * baseRadius);
                top.sub(bottom, tangent);
                new Vector3f(x, 0, z).cross(tangent, biTangent);
                tangent.cross(biTangent, normal);
                normal.normalize();

                // top vertex
                bufferIt += MeshFactory.insertElement3f(vertices, bufferIt, x * topRadius, halfHeight, z * topRadius);
                bufferIt += MeshFactory.insertElement3f(vertices, bufferIt, normal.x(), normal.y(), normal.z());

                // base vertex
                bufferIt += MeshFactory.insertElement3f(vertices, bufferIt, x * baseRadius, -halfHeight, z * baseRadius);
                bufferIt += MeshFactory.insertElement3f(vertices, bufferIt, normal.x(), normal.y(), normal.z());
            }

            return new VertexBuffer(vertices, VertexBufferParams.builder()
                    .element(new VertexElement(Mesh.POSITION_INDEX, Mesh.ELEMENTS_PER_POSITION))
                    .element(new VertexElement(Mesh.NORMAL_INDEX, Mesh.ELEMENTS_PER_NORMAL))
                    .build());
        }
    }

    /**
     * Inserts three floats in a float buffer
     *
     * @param buffer The buffer to insert data into
     * @param index  The index where to insert the data
     * @param f1     The first float to insert
     * @param f2     The second float to insert
     * @param f3     The third float to insert
     * @return The number of inserted elements
     */
    private static int insertElement3f(final FloatBuffer buffer, final int index, final float f1, final float f2, final float f3) {
        buffer.put(index, f1);
        buffer.put(index + 1, f2);
        buffer.put(index + 2, f3);
        return 3;
    }

    /**
     * Inserts three integers in a float buffer
     *
     * @param buffer The buffer to insert data into
     * @param index  The index where to insert the data
     * @param i1     The first int to insert
     * @param i2     The second int to insert
     * @param i3     The third int to insert
     * @return The number of inserted elements
     */
    private static int insertElement3i(final IntBuffer buffer, final int index, final int i1, final int i2, final int i3) {
        buffer.put(index, i1);
        buffer.put(index + 1, i2);
        buffer.put(index + 2, i3);
        return 3;
    }
}
