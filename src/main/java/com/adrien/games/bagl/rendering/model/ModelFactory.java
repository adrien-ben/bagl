package com.adrien.games.bagl.rendering.model;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.parser.model.ModelParser;
import com.adrien.games.bagl.parser.model.ObjParser;
import com.adrien.games.bagl.rendering.BufferUsage;
import com.adrien.games.bagl.rendering.Material;
import com.adrien.games.bagl.rendering.vertex.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class ModelFactory {

    private static final ModelParser parser = new ObjParser();

    /**
     * Load a model from a file
     *
     * @param filePath The path of the file to load
     * @return A {@link Model}
     */
    public static Model fromFile(final String filePath) {
        return ModelFactory.parser.parse(filePath);
    }

    /**
     * Create a cube model
     * <p>
     * The cube's center is (0, 0, 0), it has normals, but no tangents
     * nor texture coordinates
     *
     * @param size       The size of the cube
     * @param color      The color of the cube
     * @param isMetallic Is the cube metallic
     * @param roughness  The roughness of the cube
     * @return A {@link Model}
     */
    public static Model createCube(final float size, final Color color, final boolean isMetallic, final float roughness) {
        final float halfSize = size * 0.5f;

        final IndexBuffer iBuffer;
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer indices = stack.ints(
                    0, 1, 2, 2, 3, 0, // back face
                    4, 5, 6, 6, 7, 4, //right face
                    8, 9, 10, 10, 11, 8, // front face
                    12, 13, 14, 14, 15, 12, //left face
                    16, 17, 18, 18, 19, 16, // bottom face
                    20, 21, 22, 22, 23, 20 // top face
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

        final Material material = new Material();
        material.setDiffuseColor(color);
        material.setMetallic(isMetallic ? 1 : 0);
        material.setRoughness(roughness);

        return new Model().addMesh(new Mesh(vBuffer, vArray, iBuffer, material));
    }

    /**
     * Create a sphere model
     * <p>
     * The sphere's center is (0, 0, 0), it has normals, but no tangents
     * nor texture coordinates
     *
     * @param radius     The radius of the sphere
     * @param rings      The number of horizontal subdivisions
     * @param segments   The number of horizontal subdivisions
     * @param color      The color of the sphere
     * @param isMetallic Is the sphere metallic ?
     * @param roughness  The roughness of the sphere
     * @return A {@link Model}
     */
    public static Model createSphere(final float radius, final int rings, final int segments, final Color color, final boolean isMetallic,
                                     final float roughness) {

        final IntBuffer indices = MemoryUtil.memAllocInt(segments * (rings - 1) * 6 + 6 * segments);
        int bufferIt = 0;
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
            indices.put(bufferIt++, topVertexIndex);
            indices.put(bufferIt++, i);
            indices.put(bufferIt++, (i + 1) % segments);

            indices.put(bufferIt++, rings * (segments - 1) + i);
            indices.put(bufferIt++, bottomVertexIndex);
            indices.put(bufferIt++, rings * (segments - 1) + (i + 1) % segments);
        }

        final IndexBuffer iBuffer = new IndexBuffer(indices, BufferUsage.STATIC_DRAW);
        MemoryUtil.memFree(indices);

        final FloatBuffer vertices = MemoryUtil.memAllocFloat((rings * segments + 2) * 6);
        bufferIt = 0;
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

        vertices.put(bufferIt++, 0);
        vertices.put(bufferIt++, radius);
        vertices.put(bufferIt++, 0);
        vertices.put(bufferIt++, 0);
        vertices.put(bufferIt++, 1);
        vertices.put(bufferIt++, 0);
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

        final Material material = new Material();
        material.setDiffuseColor(color);
        material.setMetallic(isMetallic ? 1 : 0);
        material.setRoughness(roughness);

        return new Model().addMesh(new Mesh(vBuffer, vArray, iBuffer, material));
    }

}
