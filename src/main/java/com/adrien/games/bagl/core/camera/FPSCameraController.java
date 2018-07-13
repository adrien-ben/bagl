package com.adrien.games.bagl.core.camera;

import com.adrien.games.bagl.core.Input;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.math.Vectors;
import com.adrien.games.bagl.utils.MathUtils;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

/**
 * FPS camera controller
 * <p>
 * Use WASD (or local equivalent) to move the camera and
 * the mouse to rotate it
 *
 * @author adrien
 */
public class FPSCameraController extends CameraController {

    private static final float DEFAULT_DEGREES_PER_PIXEL = 0.15f;
    private static final float DEFAULT_MOVEMENT_SPEED = 8f;

    private final Vector3f forward;
    private final Vector3f side;
    private final Vector3f up;
    private final Vector3f direction;

    /**
     * Construct a FPS camera controller
     *
     * @param camera The camera to control
     */
    public FPSCameraController(final Camera camera) {
        super(camera);
        this.forward = new Vector3f();
        this.side = new Vector3f();
        this.up = new Vector3f();
        this.direction = new Vector3f();
    }

    /**
     * {@inheritDoc}
     *
     * @see CameraController#update(Time)
     */
    public void update(final Time time) {
        final var elapsed = time.getElapsedTime();
        final var mouseDelta = Input.getMouseDelta();

        this.forward.set(this.camera.getDirection()).normalize();
        this.side.set(this.camera.getSide()).normalize();
        this.up.set(0, 1, 0);

        if (!Vectors.isZero(mouseDelta)) {
            if (mouseDelta.y() != 0) {
                float vAngle = MathUtils.toRadians(mouseDelta.y() * DEFAULT_DEGREES_PER_PIXEL);
                this.camera.rotate(new Quaternionf().setAngleAxis(vAngle, this.side.x(), this.side.y(), this.side.z()));
            }
            if (mouseDelta.x() != 0) {
                float hAngle = -MathUtils.toRadians(mouseDelta.x() * DEFAULT_DEGREES_PER_PIXEL);
                this.camera.rotate(new Quaternionf().setAngleAxis(hAngle, this.up.x(), this.up.y(), this.up.z()));
            }
        }

        this.computeCameraMove(elapsed);
    }

    /**
     * Compute camera movement from keyboard events
     *
     * @param elapsed The time elapsed since last frame
     */
    private void computeCameraMove(final float elapsed) {
        this.direction.set(0, 0, 0);

        if (Input.isKeyPressed(GLFW.GLFW_KEY_W) || Input.isKeyPressed(GLFW.GLFW_KEY_S)) {
            if (Input.isKeyPressed(GLFW.GLFW_KEY_S)) {
                this.forward.mul(-1);
            }
            this.direction.add(this.forward);
        }

        if (Input.isKeyPressed(GLFW.GLFW_KEY_D) || Input.isKeyPressed(GLFW.GLFW_KEY_A)) {
            if (Input.isKeyPressed(GLFW.GLFW_KEY_A)) {
                this.side.mul(-1);
            }
            this.direction.add(this.side);
        }

        if (Input.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL) || Input.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
            if (Input.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL)) {
                this.up.mul(-1);
            }
            this.direction.add(this.up);
        }

        if (!Vectors.isZero(this.direction)) {
            super.camera.move(this.direction.normalize().mul(elapsed * DEFAULT_MOVEMENT_SPEED));
        }
    }
}
