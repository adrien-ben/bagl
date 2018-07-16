package com.adrien.games.bagl.core.camera;

import com.adrien.games.bagl.core.Time;

/**
 * Abstract camera controller
 *
 * @author adrien
 */
public abstract class CameraController {

    protected final Camera camera;

    public CameraController(final Camera camera) {
        this.camera = camera;
    }

    /**
     * Update the camera
     *
     * @param time The program time
     */
    public abstract void update(final Time time);

    public Camera getCamera() {
        return this.camera;
    }
}
