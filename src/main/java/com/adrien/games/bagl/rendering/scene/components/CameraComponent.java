package com.adrien.games.bagl.rendering.scene.components;

import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.camera.Camera;
import com.adrien.games.bagl.rendering.scene.Component;
import com.adrien.games.bagl.rendering.scene.ComponentVisitor;

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
     * @param id     The id of the component
     * @param tags   The tags of the component
     */
    public CameraComponent(final Camera camera, final String id, final String... tags) {
        super(id, tags);
        this.camera = camera;
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#onUpdate(Time)
     */
    @Override
    protected void onUpdate(final Time time) {
        // does nothing
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#onAccept(ComponentVisitor)
     */
    @Override
    protected void onAccept(final ComponentVisitor visitor) {
        visitor.visit(this);
    }

    public Camera getCamera() {
        return this.camera;
    }
}
