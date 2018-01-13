package com.adrien.games.bagl.rendering.model;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.parser.model.ModelParser;
import com.adrien.games.bagl.parser.model.ObjParser;
import com.adrien.games.bagl.rendering.BufferUsage;
import com.adrien.games.bagl.rendering.Material;
import com.adrien.games.bagl.rendering.vertex.*;
import org.lwjgl.system.MemoryStack;

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
     * The cube center is (0, 0, 0), it has normals, but no tangents
     * nor texture coordinates
     *
     * @param size       The size of the cube
     * @param color      The color of the cube
     * @param isMetallic Is the cube metallic
     * @param roughness  The roughness of the cube
     * @return
     */
    public static Model createCube(final float size, final Color color, final boolean isMetallic, final float roughness) {
        final float halfSize = size * 0.5f;

        IndexBuffer iBuffer;
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

        VertexBuffer vBuffer;
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

        final Mesh mesh = new Mesh(vBuffer, vArray, iBuffer, material);
        return new Model().addMesh(mesh);
    }

}
