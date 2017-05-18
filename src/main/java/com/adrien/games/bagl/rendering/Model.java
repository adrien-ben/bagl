package com.adrien.games.bagl.rendering;

import java.util.ArrayList;
import java.util.List;

/**
 * A model is a set of {@link Mesh}.
 */
public class Model {

    private final List<Mesh> meshes = new ArrayList<>();

    /**
     * Adds a mesh to the model.
     * @param mesh The mesh to add.
     * @return This for chaining.
     */
    public Model addMesh(Mesh mesh) {
        this.meshes.add(mesh);
        return this;
    }

    /**
     * Returns all the meshes of the model.
     * @return A list of {@link Mesh}.
     */
    public List<Mesh> getMeshes() {
        return this.meshes;
    }

    /**
     * Release resources.
     */
    public void destroy() {
        this.meshes.forEach(Mesh::destroy);
    }

}
