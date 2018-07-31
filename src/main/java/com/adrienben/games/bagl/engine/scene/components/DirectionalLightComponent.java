package com.adrienben.games.bagl.engine.scene.components;

import com.adrienben.games.bagl.core.math.Quaternions;
import com.adrienben.games.bagl.engine.Time;
import com.adrienben.games.bagl.engine.rendering.light.DirectionalLight;
import com.adrienben.games.bagl.engine.scene.Component;
import com.adrienben.games.bagl.engine.scene.ComponentVisitor;

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
     */
    public DirectionalLightComponent(final DirectionalLight light) {
        this.light = light;
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#update(Time)
     */
    @Override
    public void update(final Time time) {
        final var rotation = super.parentObject.getTransform().getRotation();
        this.light.setDirection(Quaternions.getForwardVector(rotation));
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

    public DirectionalLight getLight() {
        return this.light;
    }
}
