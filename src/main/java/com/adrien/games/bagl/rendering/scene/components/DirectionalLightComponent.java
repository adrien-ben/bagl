package com.adrien.games.bagl.rendering.scene.components;

import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.math.Quaternions;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.scene.Component;
import com.adrien.games.bagl.rendering.scene.ComponentVisitor;

/**
 * Scene component containing a directional light
 *
 * @author adrien
 */
public class DirectionalLightComponent extends Component {

    private final DirectionalLight light;

    /**
     * Construct a directional light component
     *
     * @param light The light to link to this component
     * @param id    The id of the component
     * @param tags  The tags of the component
     */
    public DirectionalLightComponent(final DirectionalLight light, final String id, final String... tags) {
        super(id, tags);
        this.light = light;
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#onUpdate(Time)
     */
    @Override
    protected void onUpdate(final Time time) {
        this.light.setDirection(Quaternions.getForwardVector(super.transform.getRotation()));
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

    public DirectionalLight getLight() {
        return this.light;
    }
}
