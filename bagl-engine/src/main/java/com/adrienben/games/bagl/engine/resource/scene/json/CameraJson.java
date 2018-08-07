package com.adrienben.games.bagl.engine.resource.scene.json;

public class CameraJson {

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
