package com.adrien.games.bagl.core;

import com.adrien.games.bagl.core.math.Quaternion;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;
import org.lwjgl.glfw.GLFW;

/**
 * Default camera controller.
 */
public class CameraController {

    private static final float DEFAULT_DEGREES_PER_PIXEL = 0.15f;
    private static final float DEFAULT_MOVEMENT_SPEED = 8f;

    private Camera camera;
    private Vector3 forward;
    private Vector3 side;
    private Vector3 up;
    private Vector3 direction;

    public CameraController(Camera camera) {
        this.camera = camera;
        this.forward = new Vector3();
        this.side = new Vector3();
        this.up = new Vector3();
        this.direction = new Vector3();
    }

    public void update(Time time) {
        float elapsed = time.getElapsedTime();
        final Vector2 mouseDelta = Input.getMouseDelta();

        this.forward.set(this.camera.getDirection()).normalise();
        this.side.set(this.camera.getSide()).normalise();
        this.up.set(Vector3.UP);

        if (!mouseDelta.isZero()) {
            if (mouseDelta.getY() != 0) {
                float vAngle = (float) Math.toRadians(mouseDelta.getY() * DEFAULT_DEGREES_PER_PIXEL);
                this.camera.rotate(Quaternion.fromAngleAndVector(vAngle, this.side));
            }
            if (mouseDelta.getX() != 0) {
                float hAngle = -(float) Math.toRadians(mouseDelta.getX() * DEFAULT_DEGREES_PER_PIXEL);
                this.camera.rotate(Quaternion.fromAngleAndVector(hAngle, this.up));
            }
        }

        this.computeCameraMove(elapsed);

    }

    private void computeCameraMove(float elapsed) {
        this.direction.setXYZ(0, 0, 0);

        if (Input.isKeyPressed(GLFW.GLFW_KEY_W) || Input.isKeyPressed(GLFW.GLFW_KEY_S)) {
            if (Input.isKeyPressed(GLFW.GLFW_KEY_S)) {
                this.forward.scale(-1);
            }
            this.direction.add(this.forward);
        }

        if (Input.isKeyPressed(GLFW.GLFW_KEY_D) || Input.isKeyPressed(GLFW.GLFW_KEY_A)) {
            if (Input.isKeyPressed(GLFW.GLFW_KEY_A)) {
                this.side.scale(-1);
            }
            this.direction.add(this.side);
        }

        if (Input.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL) || Input.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
            if (Input.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL)) {
                this.up.scale(-1);
            }
            this.direction.add(this.up);
        }

        if (!this.direction.isZero()) {
            this.camera.move(this.direction.normalise().scale(elapsed * DEFAULT_MOVEMENT_SPEED));
        }
    }

    public Camera getCamera() {
        return this.camera;
    }

}
