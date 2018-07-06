package com.adrien.games.bagl.resource.scene.descriptors;

import org.joml.Vector3f;

public class TransformDescriptor {

    private Vector3f translation;
    private Vector3f rotation;
    private Vector3f scale;

    public Vector3f getTranslation() {
        return translation;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public Vector3f getScale() {
        return scale;
    }
}
