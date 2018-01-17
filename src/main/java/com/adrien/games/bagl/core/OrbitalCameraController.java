package com.adrien.games.bagl.core;

import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;
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

    private Vector3 target;
    private float distance;
    private float phi;
    private float theta;

    /**
     * Construct an orbital controller
     *
     * @param camera The camera to control
     * @param target The target of the camera
     */
    public OrbitalCameraController(final Camera camera, final Vector3 target) {
        super(camera);
        this.target = target;
        final Vector3 toTarget = Vector3.sub(this.target, camera.getPosition());
        this.distance = toTarget.length();
        final float x = -toTarget.getX();
        final float y = -toTarget.getY();
        final float z = -toTarget.getZ();
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
        this.distance += -Input.getWheelDelta().getY() * 100 * time.getElapsedTime();
        if (Input.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_1)) {
            final Vector2 mouseDelta = Input.getMouseDelta();
            this.theta += mouseDelta.getX() * time.getElapsedTime();
            this.phi -= mouseDelta.getY() * time.getElapsedTime();
        }
    }

    /**
     * Computes the camera position from the distance from the
     * target and from the spherical coordinates of the camera
     */
    private void computeCameraPosition() {
        final Vector3 fromTarget = new Vector3(
                this.distance * (float) Math.sin(this.phi) * (float) Math.sin(this.theta),
                this.distance * (float) Math.cos(this.phi),
                this.distance * (float) Math.sin(this.phi) * (float) Math.cos(this.theta)
        ).normalise().scale(this.distance);

        super.camera.setDirection(new Vector3(fromTarget).scale(-1));
        super.camera.setPosition(Vector3.add(this.target, fromTarget));
    }
}
