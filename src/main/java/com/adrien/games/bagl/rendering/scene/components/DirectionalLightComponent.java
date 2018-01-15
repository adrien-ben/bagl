package com.adrien.games.bagl.rendering.scene.components;

import com.adrien.games.bagl.rendering.Renderer;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.scene.Component;

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
     * Add the light contained in this component to the renderer
     *
     * @param renderer The visiting renderer
     */
    @Override
    public void visit(final Renderer renderer) {
        this.light.setDirection(super.transform.getRotation().getDirection());
        renderer.addDirectionalLight(this.light);
    }
}
