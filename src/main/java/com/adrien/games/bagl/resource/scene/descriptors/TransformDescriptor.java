package com.adrien.games.bagl.resource.scene.descriptors;

import org.joml.Vector3f;

import java.util.List;

public class TransformDescriptor {

    private Vector3f translation;
    private List<Vector3f> rotations;
    private Vector3f scale;

    public Vector3f getTranslation() {
        return translation;
    }

    public List<Vector3f> getRotations() {
        return rotations;
    }

    public Vector3f getScale() {
        return scale;
    }
}
