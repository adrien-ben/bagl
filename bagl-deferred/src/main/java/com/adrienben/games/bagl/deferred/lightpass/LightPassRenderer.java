package com.adrienben.games.bagl.deferred.lightpass;

import com.adrienben.games.bagl.deferred.data.SceneRenderData;
import com.adrienben.games.bagl.deferred.gbuffer.GBuffer;
import com.adrienben.games.bagl.deferred.pbr.BrdfLookup;
import com.adrienben.games.bagl.deferred.shaders.DeferredShader;
import com.adrienben.games.bagl.deferred.shadow.CascadedShadowMap;
import com.adrienben.games.bagl.engine.rendering.model.Mesh;
import com.adrienben.games.bagl.engine.rendering.model.MeshFactory;
import com.adrienben.games.bagl.engine.rendering.renderer.MeshRenderer;
import com.adrienben.games.bagl.opengl.OpenGL;
import com.adrienben.games.bagl.opengl.shader.Shader;

import java.util.Objects;
import java.util.Optional;

import static com.adrienben.games.bagl.deferred.shaders.DeferredShader.*;
import static com.adrienben.games.bagl.deferred.shaders.uniforms.ShadowUniformSetter.SHADOW_MAP_0_CHANNEL;
import static org.lwjgl.opengl.GL11.*;

/**
 * Light pass renderer.
 *
 * @author adrien.
 */
public class LightPassRenderer {

    private final DeferredShader deferredShader;
    private final BrdfLookup brdfLookup;
    private final MeshRenderer meshRenderer;
    private final Mesh screenQuad;

    private GBuffer gBuffer;
    private SceneRenderData sceneRenderData;
    private CascadedShadowMap cascadedShadowMap;

    public LightPassRenderer() {
        this.deferredShader = new DeferredShader();
        this.brdfLookup = new BrdfLookup();
        this.meshRenderer = new MeshRenderer();
        this.screenQuad = MeshFactory.createScreenQuad();
    }

    /**
     * Release resources
     */
    public void destroy() {
        deferredShader.destroy();
        brdfLookup.destroy();
        screenQuad.destroy();
    }

    /**
     * Perform the lighting pass using data from the GBuffer, environment maps if any and
     * analytical light found in the scene.
     * <p>
     * Prior to perform the light pass, {@link GBuffer}, {@link SceneRenderData} and
     * {@link CascadedShadowMap} must have been set up using the respective setters.
     */
    public void renderLightPass() {
        prepareResourcesForLightingPass();
        renderLightingPass();
        unbindResourcesPostLightingPass();
    }

    private void prepareResourcesForLightingPass() {
        gBuffer.getColorTexture().bind(COLORS_TEXTURE_CHANNEL);
        gBuffer.getNormalTexture().bind(NORMALS_TEXTURE_CHANNEL);
        gBuffer.getEmissiveTexture().bind(EMISSIVE_TEXTURE_CHANNEL);
        gBuffer.getOcclusionTexture().bind(OCCLUSION_TEXTURE_CHANNEL);
        gBuffer.getDepthTexture().bind(DEPTH_TEXTURE_CHANNEL);
        brdfLookup.getTexture().bind(BRDF_LOOKUP_CHANNEL);

        deferredShader.bind()
                .setCameraUniforms(sceneRenderData.getCamera())
                .setCSMUniforms(cascadedShadowMap)
                .setDirectionalLightsUniforms(sceneRenderData.getDirectionalLights())
                .setPointLightsUniforms(sceneRenderData.getPointLights())
                .setSpotLightsUniforms(sceneRenderData.getSpotLights());

        if (Objects.nonNull(sceneRenderData.getIrradianceMap())) {
            sceneRenderData.getIrradianceMap().bind(IRRADIANCE_MAP_CHANNEL);
        }
        if (Objects.nonNull(sceneRenderData.getPreFilteredMap())) {
            sceneRenderData.getPreFilteredMap().bind(PRE_FILTERED_MAP_CHANNEL);
        }
    }

    private void renderLightingPass() {
        glDepthFunc(GL_NOTEQUAL);
        OpenGL.disableDepthWrite();
        meshRenderer.render(screenQuad);
        OpenGL.enableDepthWrite();
        glDepthFunc(GL_LESS);
    }

    private void unbindResourcesPostLightingPass() {
        Shader.unbind();
        gBuffer.getColorTexture().unbind(COLORS_TEXTURE_CHANNEL);
        gBuffer.getNormalTexture().unbind(NORMALS_TEXTURE_CHANNEL);
        gBuffer.getEmissiveTexture().unbind(EMISSIVE_TEXTURE_CHANNEL);
        gBuffer.getOcclusionTexture().unbind(OCCLUSION_TEXTURE_CHANNEL);
        gBuffer.getDepthTexture().unbind(DEPTH_TEXTURE_CHANNEL);
        Optional.ofNullable(sceneRenderData.getIrradianceMap()).ifPresent(map -> map.unbind(IRRADIANCE_MAP_CHANNEL));
        Optional.ofNullable(sceneRenderData.getPreFilteredMap()).ifPresent(map -> map.unbind(PRE_FILTERED_MAP_CHANNEL));
        brdfLookup.getTexture().unbind(BRDF_LOOKUP_CHANNEL);
        if (Objects.nonNull(cascadedShadowMap)) {
            for (int i = 0; i < CascadedShadowMap.CASCADE_COUNT; i++) {
                cascadedShadowMap.getShadowCascade(i).getShadowMap().unbind(SHADOW_MAP_0_CHANNEL + i);
            }
        }
    }

    public void setGBuffer(final GBuffer gBuffer) {
        this.gBuffer = gBuffer;
    }

    public void setSceneRenderData(final SceneRenderData sceneRenderData) {
        this.sceneRenderData = sceneRenderData;
    }

    public void setCascadedShadowMap(final CascadedShadowMap cascadedShadowMap) {
        this.cascadedShadowMap = cascadedShadowMap;
    }
}
