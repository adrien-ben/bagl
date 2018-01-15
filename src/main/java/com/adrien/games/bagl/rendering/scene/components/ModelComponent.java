package com.adrien.games.bagl.rendering.scene.components;

import com.adrien.games.bagl.rendering.Renderer;
import com.adrien.games.bagl.rendering.model.Model;
import com.adrien.games.bagl.rendering.scene.Component;

/**
 * Scene component containing a model
 *
 * @author adrien
 */
public class ModelComponent extends Component {

    private final Model model;

    /**
     * Construct a direction light component
     *
     * @param model The model to link to this component
     * @param id    The id of the component
     * @param tags  The tags of the component
     */
    public ModelComponent(final Model model, final String id, final String... tags) {
        super(id, tags);
        this.model = model;
    }

    /**
     * Add the model and its transform matrix to the renderer
     *
     * @param renderer The visiting renderer
     */
    @Override
    public void visit(final Renderer renderer) {
        renderer.addModel(this.transform.getTransformMatrix(), this.model);
    }

    public Model getModel() {
        return this.model;
    }
}
