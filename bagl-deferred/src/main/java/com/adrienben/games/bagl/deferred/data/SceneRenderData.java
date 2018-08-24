package com.adrienben.games.bagl.deferred.data;

import com.adrienben.games.bagl.core.math.Vectors;
import com.adrienben.games.bagl.engine.camera.Camera;
import com.adrienben.games.bagl.engine.rendering.light.DirectionalLight;
import com.adrienben.games.bagl.engine.rendering.light.PointLight;
import com.adrienben.games.bagl.engine.rendering.light.SpotLight;
import com.adrienben.games.bagl.engine.rendering.model.Model;
import com.adrienben.games.bagl.engine.rendering.particles.ParticleEmitter;
import com.adrienben.games.bagl.opengl.texture.Cubemap;
import org.joml.AABBf;

import java.util.ArrayList;
import java.util.List;

/**
 * Data used to render a scene.
 *
 * @author adrien
 */
public class SceneRenderData {

    private Camera camera;
    private Cubemap environmentMap;
    private Cubemap irradianceMap;
    private Cubemap preFilteredMap;
    private final List<DirectionalLight> directionalLights = new ArrayList<>();
    private final List<PointLight> pointLights = new ArrayList<>();
    private final List<SpotLight> spotLights = new ArrayList<>();
    private final List<Model> models = new ArrayList<>();
    private final List<ParticleEmitter> particleEmitters = new ArrayList<>();
    private final AABBf sceneAABB = new AABBf(Vectors.VEC3_ZERO, Vectors.VEC3_ZERO);

    /**
     * Reset the data to its initial state.
     */
    public void reset() {
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

    public void addDirectionalLight(final DirectionalLight directionalLight) {
        directionalLights.add(directionalLight);
    }

    public void addPointLight(final PointLight pointLight) {
        pointLights.add(pointLight);
    }

    public void addSpotLight(final SpotLight spotLight) {
        spotLights.add(spotLight);
    }

    public void addModel(final Model model) {
        models.add(model);
    }

    public void addParticleEmitter(final ParticleEmitter particleEmitter) {
        particleEmitters.add(particleEmitter);
    }

    public void setCamera(final Camera camera) {
        this.camera = camera;
    }

    public void setEnvironmentMap(final Cubemap environmentMap) {
        this.environmentMap = environmentMap;
    }

    public void setIrradianceMap(final Cubemap irradianceMap) {
        this.irradianceMap = irradianceMap;
    }

    public void setPreFilteredMap(final Cubemap preFilteredMap) {
        this.preFilteredMap = preFilteredMap;
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
