package com.adrienben.games.bagl.engine.animation;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;

/**
 * Represent the state of a node at a given time.
 *
 * @author adrien
 */
public class NodeKeyFrame {

    private final float time;
    private Vector3f translation;
    private Quaternionf rotation;
    private Vector3f scale;

    public NodeKeyFrame(final float time) {
        this.time = time;
    }

    public float getTime() {
        return time;
    }

    public Optional<Vector3f> getTranslation() {
        return Optional.ofNullable(translation);
    }

    public void setTranslation(Vector3f translation) {
        this.translation = translation;
    }

    public Optional<Quaternionf> getRotation() {
        return Optional.ofNullable(rotation);
    }

    public void setRotation(Quaternionf rotation) {
        this.rotation = rotation;
    }

    public Optional<Vector3f> getScale() {
        return Optional.ofNullable(scale);
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
    }
}
