package com.adrien.games.bagl.rendering.model;

import com.adrien.games.bagl.rendering.Material;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A model is a set of {@link Mesh}
 *
 * @author adrien
 */
public class Model {

    private final Map<Mesh, Material> meshes = new LinkedHashMap<>();

    /**
     * Add a mesh to the model
     *
     * @param mesh     The mesh to add
     * @param material The material of the model
     * @return This for chaining
     */
    public Model addMesh(final Mesh mesh, final Material material) {
        this.meshes.put(mesh, material);
        return this;
    }

    /**
     * Return all the meshes of the model
     *
     * @return A map of {@link Mesh} coupled with their {@link Material}
     */
    public Map<Mesh, Material> getMeshes() {
        return this.meshes;
    }

    /**
     * Release resources
     */
    public void destroy() {
        this.meshes.keySet().forEach(Mesh::destroy);
    }
}
