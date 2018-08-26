package com.adrienben.games.bagl.engine.rendering.model;

import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.engine.rendering.material.Material;
import com.adrienben.games.bagl.engine.resource.gltf.GltfLoader;

/**
 * {@link Model} factory
 *
 * @author adrien
 */
public final class ModelFactory {

    private static final GltfLoader LOADER = new GltfLoader();

    /**
     * Private constructor to prevent instantiation
     */
    private ModelFactory() {
    }

    /**
     * Load a model from a file
     *
     * @param path The path of the file to load
     * @return A {@link Model}
     */
    public static Model fromFile(final ResourcePath path) {
        return LOADER.load(path);
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
        final var model = new Model();
        model.addNode().addMesh(MeshFactory.createCube(size), material);
        return model;
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
        final var model = new Model();
        model.addNode().addMesh(MeshFactory.createSphere(radius, rings, segments), material);
        return model;
    }

    /**
     * Create a truncated cone model
     * <p>
     * The cone's center is (0, 0, 0), it has normals, but no tangents
     * nor texture coordinates
     *
     * @param baseRadius The radius of the base
     * @param topRadius  The radius of the top
     * @param height     The height of the cone
     * @param segments   The number of horizontal subdivisions
     * @param material   The material of the model
     * @return A {@link Model}
     */
    public static Model createCone(final float baseRadius, final float topRadius, final float height, final int segments, final Material material) {
        final var model = new Model();
        model.addNode().addMesh(MeshFactory.createCylinder(baseRadius, topRadius, height, segments), material);
        return model;
    }
}
