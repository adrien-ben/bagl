package com.adrien.games.bagl.rendering.model;

import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.BufferUsage;
import com.adrien.games.bagl.rendering.DataType;
import com.adrien.games.bagl.rendering.PrimitiveType;
import com.adrien.games.bagl.rendering.vertex.*;
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

            final VertexBuffer vBuffer = new VertexBuffer(vertices, new VertexBufferParams()
                    .element(new VertexElement(Mesh.POSITION_INDEX, Mesh.ELEMENTS_PER_POSITION))
                    .element(new VertexElement(Mesh.NORMAL_INDEX, Mesh.ELEMENTS_PER_NORMAL)));
            final VertexArray vArray = new VertexArray();
            vArray.bind();
            vArray.attachVertexBuffer(vBuffer);
            vArray.unbind();
            return new Mesh(vBuffer, vArray, PrimitiveType.TRIANGLE_STRIP);
        }
    }

    public static Mesh createScreenQuad() {
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final ByteBuffer positions = stack.bytes(
                    (byte) -1, (byte) -1, (byte) 0, (byte) 0,
                    (byte) 1, (byte) -1, Byte.MAX_VALUE, (byte) 0,
                    (byte) -1, (byte) 1, (byte) 0, Byte.MAX_VALUE,
                    (byte) 1, (byte) 1, Byte.MAX_VALUE, Byte.MAX_VALUE);
            final VertexBuffer vBuffer = new VertexBuffer(positions, new VertexBufferParams()
                    .dataType(DataType.BYTE)
                    .element(new VertexElement(0, 2))
                    .element(new VertexElement(2, 2, true)));

            final VertexArray vArray = new VertexArray();
            vArray.bind();
            vArray.attachVertexBuffer(vBuffer);
            vArray.unbind();
            return new Mesh(vBuffer, vArray, PrimitiveType.TRIANGLE_STRIP);
        }
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
            vBuffer = new VertexBuffer(vertices, new VertexBufferParams()
                    .element(new VertexElement(Mesh.POSITION_INDEX, Mesh.ELEMENTS_PER_POSITION))
                    .element(new VertexElement(Mesh.NORMAL_INDEX, Mesh.ELEMENTS_PER_NORMAL)));
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

                vertices.put(bufferIt++, radius * x);
                vertices.put(bufferIt++, radius * y);
                vertices.put(bufferIt++, radius * z);
                vertices.put(bufferIt++, x);
                vertices.put(bufferIt++, y);
                vertices.put(bufferIt++, z);

            }
        }

        // top vertex
        vertices.put(bufferIt++, 0);
        vertices.put(bufferIt++, radius);
        vertices.put(bufferIt++, 0);
        vertices.put(bufferIt++, 0);
        vertices.put(bufferIt++, 1);
        vertices.put(bufferIt++, 0);

        // bottom vertex
        vertices.put(bufferIt++, 0);
        vertices.put(bufferIt++, -radius);
        vertices.put(bufferIt++, 0);
        vertices.put(bufferIt++, 0);
        vertices.put(bufferIt++, -1);
        vertices.put(bufferIt, 0);

        final VertexBuffer vBuffer = new VertexBuffer(vertices, new VertexBufferParams()
                .element(new VertexElement(Mesh.POSITION_INDEX, Mesh.ELEMENTS_PER_POSITION))
                .element(new VertexElement(Mesh.NORMAL_INDEX, Mesh.ELEMENTS_PER_NORMAL)));
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

                indices.put(bufferIt++, index0);
                indices.put(bufferIt++, index1);
                indices.put(bufferIt++, index2);
                indices.put(bufferIt++, index2);
                indices.put(bufferIt++, index3);
                indices.put(bufferIt++, index0);
            }
        }

        final int topVertexIndex = rings * segments;
        final int bottomVertexIndex = rings * segments + 1;
        for (int i = 0; i < segments; i++) {
            // top faces
            indices.put(bufferIt++, topVertexIndex);
            indices.put(bufferIt++, i);
            indices.put(bufferIt++, (i + 1) % segments);

            // bottom faces
            indices.put(bufferIt++, rings * (segments - 1) + i);
            indices.put(bufferIt++, bottomVertexIndex);
            indices.put(bufferIt++, rings * (segments - 1) + (i + 1) % segments);
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
        final float halfHeight = height * 0.5f;

        final VertexBuffer vBuffer;
        int bufferIt = 0;
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final FloatBuffer vertices = stack.mallocFloat((segments * 4 + 2) * 6);
            // base center
            vertices.put(bufferIt++, 0);
            vertices.put(bufferIt++, -halfHeight);
            vertices.put(bufferIt++, 0);
            vertices.put(bufferIt++, 0);
            vertices.put(bufferIt++, -1);
            vertices.put(bufferIt++, 0);

            // top center
            vertices.put(bufferIt++, 0);
            vertices.put(bufferIt++, halfHeight);
            vertices.put(bufferIt++, 0);
            vertices.put(bufferIt++, 0);
            vertices.put(bufferIt++, 1);
            vertices.put(bufferIt++, 0);

            int baseIt = 2 * 6;
            int topIt = (2 + segments) * 6;
            for (int i = 0; i < segments; i++) {
                final float theta = 2 * (float) Math.PI * i / segments;
                final float x = (float) Math.cos(theta);
                final float z = -(float) Math.sin(theta);

                // base vertex
                vertices.put(baseIt++, x * baseRadius);
                vertices.put(baseIt++, -halfHeight);
                vertices.put(baseIt++, z * baseRadius);
                vertices.put(baseIt++, 0);
                vertices.put(baseIt++, -1);
                vertices.put(baseIt++, 0);

                // top vertex
                vertices.put(topIt++, x * topRadius);
                vertices.put(topIt++, halfHeight);
                vertices.put(topIt++, z * topRadius);
                vertices.put(topIt++, 0);
                vertices.put(topIt++, 1);
                vertices.put(topIt++, 0);

                bufferIt += 12;
            }

            final Vector3 tangent = new Vector3();
            final Vector3 biTangent = new Vector3();
            final Vector3 normal = new Vector3();

            // side vertices
            for (int i = 0; i < segments; i++) {
                final float theta = 2 * (float) Math.PI * i / segments;
                final float x = (float) Math.cos(theta);
                final float z = -(float) Math.sin(theta);

                // normal computation
                final Vector3 top = new Vector3(x * topRadius, halfHeight, z * topRadius);
                final Vector3 bottom = new Vector3(x * baseRadius, -halfHeight, z * baseRadius);
                Vector3.sub(top, bottom, tangent);
                tangent.normalise();
                Vector3.cross(new Vector3(x, 0, z), tangent, biTangent);
                biTangent.normalise();
                Vector3.cross(tangent, biTangent, normal);
                normal.normalise();

                // top vertex
                vertices.put(bufferIt++, x * topRadius);
                vertices.put(bufferIt++, halfHeight);
                vertices.put(bufferIt++, z * topRadius);
                vertices.put(bufferIt++, normal.getX());
                vertices.put(bufferIt++, normal.getY());
                vertices.put(bufferIt++, normal.getZ());

                // base vertex
                vertices.put(bufferIt++, x * baseRadius);
                vertices.put(bufferIt++, -halfHeight);
                vertices.put(bufferIt++, z * baseRadius);
                vertices.put(bufferIt++, normal.getX());
                vertices.put(bufferIt++, normal.getY());
                vertices.put(bufferIt++, normal.getZ());
            }

            vBuffer = new VertexBuffer(vertices, new VertexBufferParams()
                    .element(new VertexElement(Mesh.POSITION_INDEX, Mesh.ELEMENTS_PER_POSITION))
                    .element(new VertexElement(Mesh.NORMAL_INDEX, Mesh.ELEMENTS_PER_NORMAL)));
        }

        final IndexBuffer iBuffer;
        bufferIt = 0;
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer indices = stack.mallocInt(segments * 2 * 3 + segments * 6);

            // base
            for (int i = 0; i < segments; i++) {
                final int offset = 2;
                indices.put(bufferIt++, 0);
                indices.put(bufferIt++, offset + (i + 1) % segments);
                indices.put(bufferIt++, offset + i);
            }

            // top
            for (int i = 0; i < segments; i++) {
                final int offset = segments + 2;
                indices.put(bufferIt++, 1);
                indices.put(bufferIt++, offset + i);
                indices.put(bufferIt++, offset + (i + 1) % segments);
            }

            // side
            for (int i = 0; i < segments; i++) {
                final int offset = 2 * (segments + 1);
                final int index0 = offset + i * 2;
                final int index1 = offset + i * 2 + 1;
                final int index2 = offset + (i * 2 + 3) % (2 * segments);
                final int index3 = offset + (i * 2 + 2) % (2 * segments);

                indices.put(bufferIt++, index0);
                indices.put(bufferIt++, index1);
                indices.put(bufferIt++, index2);
                indices.put(bufferIt++, index2);
                indices.put(bufferIt++, index3);
                indices.put(bufferIt++, index0);
            }
            iBuffer = new IndexBuffer(indices, BufferUsage.STATIC_DRAW);
        }

        final VertexArray vArray = new VertexArray();
        vArray.bind();
        vArray.attachVertexBuffer(vBuffer);
        vArray.unbind();

        return new Mesh(vBuffer, vArray, iBuffer);
    }
}
