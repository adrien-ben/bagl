package com.adrien.games.bagl.rendering.scene.components;

import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.math.Quaternions;
import com.adrien.games.bagl.rendering.Renderer;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.rendering.scene.Component;

/**
 * Scene component containing a spot light
 *
 * @author adrien
 */
public class SpotLightComponent extends Component {

    private final SpotLight light;

    /**
     * Construct a spot light component
     *
     * @param light The light to link to this component
     * @param id    The id of the component
     * @param tags  The tags of the component
     */
    public SpotLightComponent(final SpotLight light, final String id, final String... tags) {
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
        this.light.setPosition(super.transform.getTranslation());
        this.light.setDirection(Quaternions.getForwardVector(super.transform.getRotation()));
    }

    /**
     * Add the light contained in this component to the renderer
     *
     * @param renderer The visiting renderer
     */
    @Override
    public void visit(final Renderer renderer) {
        renderer.addSpotLight(this.light);
    }

    public SpotLight getLight() {
        return this.light;
    }
}
