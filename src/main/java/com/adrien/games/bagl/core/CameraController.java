package com.adrien.games.bagl.core;

import com.adrien.games.bagl.core.math.Quaternion;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;
import org.lwjgl.glfw.GLFW;

/**
 * Default camera controller.
 */
public class CameraController {

    private static final float DEGREES_PER_PIXEL = 1f;
    private static final float MOVEMENT_SPEED = 3f;

    private Camera camera;
    private Vector3 forward;
    private Vector3 side;
    private Vector3 up;

    public CameraController(Camera camera) {
        this.camera = camera;
        this.forward = new Vector3();
        this.side = new Vector3();
        this.up = new Vector3();
    }

    public void update(Time time) {
        float elapsed = time.getElapsedTime();
        final Vector2 mouseDelta = Input.getMouseDelta();
        if(!mouseDelta.isZero()) {
            if(mouseDelta.getX() != 0) {
                float hAngle = -(float)Math.toRadians(elapsed*mouseDelta.getX()*DEGREES_PER_PIXEL*1.7777777777777f);
                this.camera.rotate(Quaternion.fromAngleAndVector(hAngle, Vector3.UP));
            }
            if(mouseDelta.getY() != 0) {
                float vAngle = (float)Math.toRadians(elapsed*mouseDelta.getY()*DEGREES_PER_PIXEL);
                this.camera.rotate(Quaternion.fromAngleAndVector(vAngle, this.camera.getSide()));
            }
        }

        if(Input.isKeyPressed(GLFW.GLFW_KEY_W) || Input.isKeyPressed(GLFW.GLFW_KEY_S)) {
            this.forward.set(this.camera.getDirection()).normalise().scale(elapsed*MOVEMENT_SPEED);
            if(Input.isKeyPressed(GLFW.GLFW_KEY_S)) {
                this.forward.scale(-1);
            }
            this.camera.move(this.forward);
        }

        if(Input.isKeyPressed(GLFW.GLFW_KEY_D) || Input.isKeyPressed(GLFW.GLFW_KEY_A)) {
            this.side.set(this.camera.getSide()).normalise().scale(elapsed*MOVEMENT_SPEED);
            if(Input.isKeyPressed(GLFW.GLFW_KEY_A)) {
                this.side.scale(-1);
            }
            this.camera.move(this.side);
        }

        if(Input.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL) || Input.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
            this.up.set(Vector3.UP).scale(elapsed*MOVEMENT_SPEED);
            if(Input.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL)) {
                this.up.scale(-1);
            }
            this.camera.move(this.up);
        }

    }

    public Camera getCamera() {
        return this.camera;
    }

}
