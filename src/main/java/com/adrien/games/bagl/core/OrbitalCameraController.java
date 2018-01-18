package com.adrien.games.bagl.core;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

/**
 * Orbital camera controller
 * <p>
 * Orbital camera focuses on a target. Use mouse wheel to
 * get further or closer to the target. Left click and move
 * the mouse to gravitate around the target
 *
 * @author adrien
 */
public class OrbitalCameraController extends CameraController {

    private Vector3f target;
    private float distance;
    private float phi;
    private float theta;

    /**
     * Construct an orbital controller
     *
     * @param camera The camera to control
     * @param target The target of the camera
     */
    public OrbitalCameraController(final Camera camera, final Vector3f target) {
        super(camera);
        this.target = target;
        final Vector3f toTarget = new Vector3f(this.target).sub(camera.getPosition());
        this.distance = toTarget.length();
        final float x = -toTarget.x();
        final float y = -toTarget.y();
        final float z = -toTarget.z();
        this.phi = (float) Math.acos(y / this.distance);
        this.theta = (float) Math.atan(x / z);
        this.computeCameraPosition();
    }

    /**
     * {@inheritDoc}
     *
     * @see CameraController#update(Time)
     */
    @Override
    public void update(final Time time) {
        this.computeCameraPosition();
        this.distance += -Input.getWheelDelta().y() * 100 * time.getElapsedTime();
        if (Input.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_1)) {
            final Vector2f mouseDelta = Input.getMouseDelta();
            this.theta += mouseDelta.x() * time.getElapsedTime();
            this.phi -= mouseDelta.y() * time.getElapsedTime();
        }
    }

    /**
     * Computes the camera position from the distance from the
     * target and from the spherical coordinates of the camera
     */
    private void computeCameraPosition() {
        final Vector3f fromTarget = new Vector3f(
                this.distance * (float) Math.sin(this.phi) * (float) Math.sin(this.theta),
                this.distance * (float) Math.cos(this.phi),
                this.distance * (float) Math.sin(this.phi) * (float) Math.cos(this.theta)
        ).normalize().mul(this.distance);

        super.camera.setDirection(new Vector3f(fromTarget).mul(-1));
        super.camera.setPosition(new Vector3f(this.target).add(fromTarget));
    }
}
