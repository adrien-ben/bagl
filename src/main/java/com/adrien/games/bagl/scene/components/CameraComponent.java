package com.adrien.games.bagl.scene.components;

import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.camera.Camera;
import com.adrien.games.bagl.core.camera.CameraController;
import com.adrien.games.bagl.core.camera.FPSCameraController;
import com.adrien.games.bagl.core.math.Quaternions;
import com.adrien.games.bagl.scene.Component;
import com.adrien.games.bagl.scene.ComponentVisitor;

import java.util.Objects;

/**
 * Scene component containing a {@link Camera}
 *
 * @author adrien
 */
public class CameraComponent extends Component {

    private final Camera camera;
    private final CameraController controller;
    private boolean isInit = false;

    /**
     * Construct a camera component
     *
     * @param camera The camera to link to this component
     */
    public CameraComponent(final Camera camera, final boolean enabledController) {
        this.camera = camera;
        this.controller = enabledController ? new FPSCameraController(camera) : null;
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#update(Time)
     */
    @Override
    public void update(final Time time) {
        if (!isInit) {
            camera.setPosition(super.parentObject.getTransform().getTranslation());
            camera.setDirection(Quaternions.getForwardVector(super.parentObject.getTransform().getRotation()));
            camera.setUp(Quaternions.getUpVector(super.parentObject.getTransform().getRotation()));
            isInit = true;
        }
        if (Objects.nonNull(controller)) {
            controller.update(time);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#accept(ComponentVisitor)
     */
    @Override
    public void accept(final ComponentVisitor visitor) {
        visitor.visit(this);
    }

    public Camera getCamera() {
        return this.camera;
    }
}
