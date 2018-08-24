package com.adrienben.games.bagl.deferred.data;

import com.adrienben.games.bagl.engine.rendering.model.Mesh;
import com.adrienben.games.bagl.engine.rendering.model.Model;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.games.bagl.engine.scene.ComponentVisitor;
import com.adrienben.games.bagl.engine.scene.Scene;
import com.adrienben.games.bagl.engine.scene.components.*;
import org.joml.AABBf;

import java.util.Collection;

/**
 * This class is responsible for collecting the data required for rendering a scene.
 *
 * @author adrien
 */
public class SceneRenderDataCollector implements ComponentVisitor {

    private final SceneRenderData sceneRenderData;
    private final AABBf aabBfBuffer;

    public SceneRenderDataCollector() {
        this.sceneRenderData = new SceneRenderData();
        this.aabBfBuffer = new AABBf();
    }

    /**
     * Update the data to render by visiting the scene.
     */
    public SceneRenderData collectDataForRendering(final Scene scene) {
        sceneRenderData.reset();
        scene.accept(this);
        computeSceneAABB();
        return sceneRenderData;
    }

    private void computeSceneAABB() {
        sceneRenderData.getModels().stream()
                .map(Model::getNodes).flatMap(Collection::stream)
                .forEach(this::computeModelNodeAABB);
    }

    private void computeModelNodeAABB(final ModelNode modelNode) {
        final var transform = modelNode.getTransform();
        modelNode.getMeshes().keySet().stream()
                .map(Mesh::getAabb)
                .map(aabb -> transform.transformAABB(aabb, aabBfBuffer))
                .reduce(sceneRenderData.getSceneAABB(), AABBf::union);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Add the model contained in component to the list of models to render
     *
     * @see ComponentVisitor#visit(ModelComponent)
     */
    @Override
    public void visit(final ModelComponent component) {
        sceneRenderData.addModel(component.getModel());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Set the camera contained in component as the camera to use when rendering.
     * Take care if your scene contains several camera then the last visited camera will
     * be the one used
     *
     * @see ComponentVisitor#visit(CameraComponent)
     */
    @Override
    public void visit(final CameraComponent component) {
        sceneRenderData.setCamera(component.getCamera());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Set the environment maps to render. Take care, for now only one set of
     * maps can be used. If several {@link EnvironmentComponent} are present
     * in the scene then only the last will be used
     *
     * @param component The component to visit
     */
    @Override
    public void visit(final EnvironmentComponent component) {
        sceneRenderData.setEnvironmentMap(component.getEnvironmentMap().orElse(null));
        sceneRenderData.setIrradianceMap(component.getIrradianceMap().orElse(null));
        sceneRenderData.setPreFilteredMap(component.getPreFilteredMap().orElse(null));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Add the light contained in component to the list of light to take
     * into account when rendering
     *
     * @see ComponentVisitor#visit(DirectionalLightComponent)
     */
    @Override
    public void visit(final DirectionalLightComponent component) {
        sceneRenderData.addDirectionalLight(component.getLight());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Add the light contained in component to the list of light to take
     * into account when rendering
     *
     * @see ComponentVisitor#visit(PointLightComponent)
     */
    @Override
    public void visit(final PointLightComponent component) {
        sceneRenderData.addPointLight(component.getLight());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Add the light contained in component to the list of light to take
     * into account when rendering
     *
     * @see ComponentVisitor#visit(SpotLightComponent)
     */
    @Override
    public void visit(final SpotLightComponent component) {
        sceneRenderData.addSpotLight(component.getLight());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Add the particle emitter contained in component to the list of emitter
     * to render.
     *
     * @see ComponentVisitor#visit(SpotLightComponent)
     */
    @Override
    public void visit(final ParticleComponent component) {
        sceneRenderData.addParticleEmitter(component.getEmitter());
    }
}
