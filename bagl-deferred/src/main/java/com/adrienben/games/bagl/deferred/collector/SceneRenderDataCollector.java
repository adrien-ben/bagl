package com.adrienben.games.bagl.deferred.collector;

import com.adrienben.games.bagl.core.math.Vectors;
import com.adrienben.games.bagl.engine.camera.Camera;
import com.adrienben.games.bagl.engine.rendering.light.DirectionalLight;
import com.adrienben.games.bagl.engine.rendering.light.PointLight;
import com.adrienben.games.bagl.engine.rendering.light.SpotLight;
import com.adrienben.games.bagl.engine.rendering.model.Mesh;
import com.adrienben.games.bagl.engine.rendering.model.Model;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.games.bagl.engine.rendering.particles.ParticleEmitter;
import com.adrienben.games.bagl.engine.scene.ComponentVisitor;
import com.adrienben.games.bagl.engine.scene.Scene;
import com.adrienben.games.bagl.engine.scene.components.*;
import com.adrienben.games.bagl.opengl.texture.Cubemap;
import org.joml.AABBf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class is responsible for collecting the data required for rendering a scene.
 *
 * @author adrien
 */
public class SceneRenderDataCollector implements ComponentVisitor {

    private Camera camera;
    private Cubemap environmentMap;
    private Cubemap irradianceMap;
    private Cubemap preFilteredMap;
    private final List<DirectionalLight> directionalLights;
    private final List<PointLight> pointLights;
    private final List<SpotLight> spotLights;
    private final List<Model> models;
    private final List<ParticleEmitter> particleEmitters;
    private final AABBf sceneAABB;

    private final AABBf aabBfBuffer;

    public SceneRenderDataCollector() {
        this.camera = null;
        this.directionalLights = new ArrayList<>();
        this.pointLights = new ArrayList<>();
        this.spotLights = new ArrayList<>();
        this.models = new ArrayList<>();
        this.particleEmitters = new ArrayList<>();
        this.sceneAABB = new AABBf(Vectors.VEC3_ZERO, Vectors.VEC3_ZERO);
        this.aabBfBuffer = new AABBf();
    }

    /**
     * Update the data to render by visiting the scene.
     */
    public void collectDataForRendering(final Scene scene) {
        preUpdateCleanup();
        scene.accept(this);
        computeSceneAABB();
    }

    /**
     * Clear data before rendering in case the scene change since last frame
     */
    private void preUpdateCleanup() {
        camera = null;
        environmentMap = null;
        irradianceMap = null;
        preFilteredMap = null;
        directionalLights.clear();
        pointLights.clear();
        spotLights.clear();
        models.clear();
        particleEmitters.clear();
        sceneAABB.setMin(Vectors.VEC3_ZERO);
        sceneAABB.setMax(Vectors.VEC3_ZERO);
    }

    private void computeSceneAABB() {
        models.stream().map(Model::getNodes).flatMap(Collection::stream).forEach(this::computeModelNodeAABB);
    }

    private void computeModelNodeAABB(final ModelNode modelNode) {
        final var transform = modelNode.getTransform();
        modelNode.getMeshes().keySet().stream()
                .map(Mesh::getAabb)
                .map(aabb -> transform.transformAABB(aabb, aabBfBuffer))
                .reduce(sceneAABB, AABBf::union);
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
        models.add(component.getModel());
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
        camera = component.getCamera();
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
        environmentMap = component.getEnvironmentMap().orElse(null);
        irradianceMap = component.getIrradianceMap().orElse(null);
        preFilteredMap = component.getPreFilteredMap().orElse(null);
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
        directionalLights.add(component.getLight());
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
        pointLights.add(component.getLight());
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
        spotLights.add(component.getLight());
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
        particleEmitters.add(component.getEmitter());
    }

    public Camera getCamera() {
        return camera;
    }

    public Cubemap getEnvironmentMap() {
        return environmentMap;
    }

    public Cubemap getIrradianceMap() {
        return irradianceMap;
    }

    public Cubemap getPreFilteredMap() {
        return preFilteredMap;
    }

    public List<DirectionalLight> getDirectionalLights() {
        return directionalLights;
    }

    public List<PointLight> getPointLights() {
        return pointLights;
    }

    public List<SpotLight> getSpotLights() {
        return spotLights;
    }

    public List<Model> getModels() {
        return models;
    }

    public List<ParticleEmitter> getParticleEmitters() {
        return particleEmitters;
    }

    public AABBf getSceneAABB() {
        return sceneAABB;
    }
}
