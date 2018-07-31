package com.adrienben.games.bagl.engine.scene;

import com.adrienben.games.bagl.engine.scene.components.*;

/**
 * Interface that must be implemented by classes that desire to
 * go through a scene graph
 *
 * @author adrien
 */
public interface ComponentVisitor {

    /**
     * Action to perform when visiting a {@link ModelComponent}
     *
     * @param component The component to visit
     */
    void visit(ModelComponent component);

    /**
     * Action to perform when visiting a {@link CameraComponent}
     *
     * @param component The component to visit
     */
    void visit(CameraComponent component);

    /**
     * Actio to perform when visiting an {@link EnvironmentComponent}
     *
     * @param component The component to visit
     */
    void visit(EnvironmentComponent component);

    /**
     * Action to perform when visiting a {@link DirectionalLightComponent}
     *
     * @param component The component to visit
     */
    void visit(DirectionalLightComponent component);

    /**
     * Action to perform when visiting a {@link PointLightComponent}
     *
     * @param component The component to visit
     */
    void visit(PointLightComponent component);


    /**
     * Action to perform when visiting a {@link SpotLightComponent}
     *
     * @param component The component to visit
     */
    void visit(SpotLightComponent component);

    /**
     * Action to perform when visiting a {@link ParticleComponent}
     *
     * @param component The component to visit
     */
    void visit(ParticleComponent component);
}
