package com.adrien.games.bagl.rendering.scene;

import com.adrien.games.bagl.rendering.scene.components.*;

/**
 * Interface that must be implemented by classes that desire to
 * go through a scene graph
 *
 * @author adrien
 */
public interface ComponentVisitor {

    /**
     * Action to perform when visiting an {@link ObjectComponent}
     *
     * @param component The component to visit
     */
    void visit(ObjectComponent component);

    /**
     * Action to perform when visiting an {@link ModelComponent}
     *
     * @param component The component to visit
     */
    void visit(ModelComponent component);

    /**
     * Action to perform when visiting an {@link CameraComponent}
     *
     * @param component The component to visit
     */
    void visit(CameraComponent component);

    /**
     * Action to perform when visiting an {@link DirectionalLightComponent}
     *
     * @param component The component to visit
     */
    void visit(DirectionalLightComponent component);

    /**
     * Action to perform when visiting an {@link PointLightComponent}
     *
     * @param component The component to visit
     */
    void visit(PointLightComponent component);


    /**
     * Action to perform when visiting an {@link SpotLightComponent}
     *
     * @param component The component to visit
     */
    void visit(SpotLightComponent component);
}
