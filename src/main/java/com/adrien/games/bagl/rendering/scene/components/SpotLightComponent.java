package com.adrien.games.bagl.rendering.scene.components;

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
     */
    public SpotLightComponent(final SpotLight light) {
        this.light = light;
    }

    /**
     * Add the light contained in this component to the renderer
     *
     * @param renderer The visiting renderer
     */
    @Override
    public void visit(final Renderer renderer) {
        this.light.setPosition(super.transform.getTranslation());
        renderer.addSpotLight(this.light);
    }
}
