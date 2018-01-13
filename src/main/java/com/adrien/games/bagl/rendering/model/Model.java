package com.adrien.games.bagl.rendering.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A model is a set of {@link Mesh}
 *
 * @author adrien
 */
public class Model {

    private final List<Mesh> meshes = new ArrayList<>();

    /**
     * Add a mesh to the model
     *
     * @param mesh The mesh to add
     * @return This for chaining
     */
    public Model addMesh(final Mesh mesh) {
        this.meshes.add(mesh);
        return this;
    }

    /**
     * Return all the meshes of the model
     *
     * @return A list of {@link Mesh}
     */
    public List<Mesh> getMeshes() {
        return this.meshes;
    }

    /**
     * Release resources
     */
    public void destroy() {
        this.meshes.forEach(Mesh::destroy);
    }

}
