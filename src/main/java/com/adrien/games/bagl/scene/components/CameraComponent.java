package com.adrien.games.bagl.scene.components;

import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.camera.Camera;
import com.adrien.games.bagl.scene.Component;
import com.adrien.games.bagl.scene.ComponentVisitor;

/**
 * Scene component containing a {@link Camera}
 *
 * @author adrien
 */
public class CameraComponent extends Component {

    private final Camera camera;

    /**
     * Construct a camera component
     *
     * @param camera The camera to link to this component
     */
    public CameraComponent(final Camera camera) {
        this.camera = camera;
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#update(Time)
     */
    @Override
    public void update(final Time time) {
        // does nothing
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
