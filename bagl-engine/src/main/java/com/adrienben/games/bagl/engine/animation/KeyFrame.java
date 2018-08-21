package com.adrienben.games.bagl.engine.animation;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class KeyFrame {

    private final float time;
    private Vector3f translation;
    private Quaternionf rotation;
    private Vector3f scale;

    public KeyFrame(final float time) {
        this.time = time;
    }

    public float getTime() {
        return time;
    }

    public Vector3f getTranslation() {
        return translation;
    }

    public void setTranslation(Vector3f translation) {
        this.translation = translation;
    }

    public Quaternionf getRotation() {
        return rotation;
    }

    public void setRotation(Quaternionf rotation) {
        this.rotation = rotation;
    }

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
    }
}
