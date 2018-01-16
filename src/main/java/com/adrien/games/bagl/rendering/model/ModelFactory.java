package com.adrien.games.bagl.rendering.model;

import com.adrien.games.bagl.parser.model.ModelParser;
import com.adrien.games.bagl.parser.model.ObjParser;
import com.adrien.games.bagl.rendering.Material;

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
     * @param size     The size of the cube
     * @param material The material of the model
     * @return A {@link Model}
     */
    public static Model createCube(final float size, final Material material) {
        return new Model().addMesh(MeshFactory.createCube(size), material);
    }

    /**
     * Create a sphere model
     * <p>
     * The sphere's center is (0, 0, 0), it has normals, but no tangents
     * nor texture coordinates
     *
     * @param radius   The radius of the sphere
     * @param rings    The number of horizontal subdivisions
     * @param segments The number of horizontal subdivisions
     * @param material The material of the model
     * @return A {@link Model}
     */
    public static Model createSphere(final float radius, final int rings, final int segments, final Material material) {
        return new Model().addMesh(MeshFactory.createSphere(radius, rings, segments), material);
    }
}
