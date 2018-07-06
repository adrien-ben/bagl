package com.adrien.games.bagl.resource.scene.descriptors;

import org.joml.Vector3f;

public class CameraDescriptor {

    private Vector3f position;
    private Vector3f direction;
    private Vector3f up;
    private float fov;
    private float near;
    private float far;

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public Vector3f getUp() {
        return up;
    }

    public float getFov() {
        return fov;
    }

    public float getNear() {
        return near;
    }

    public float getFar() {
        return far;
    }
}
