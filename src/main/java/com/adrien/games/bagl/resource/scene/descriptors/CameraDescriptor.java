package com.adrien.games.bagl.resource.scene.descriptors;

public class CameraDescriptor {

    private float fov;
    private float near;
    private float far;
    private boolean enableController;

    public float getFov() {
        return fov;
    }

    public float getNear() {
        return near;
    }

    public float getFar() {
        return far;
    }

    public boolean isEnableController() {
        return enableController;
    }
}
