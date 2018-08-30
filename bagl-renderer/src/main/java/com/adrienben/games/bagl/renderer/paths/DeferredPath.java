package com.adrienben.games.bagl.renderer.paths;

import com.adrienben.games.bagl.opengl.FrameBuffer;
import com.adrienben.games.bagl.renderer.gbuffer.GBuffer;
import com.adrienben.games.bagl.renderer.gbuffer.generator.GBufferGenerator;
import com.adrienben.games.bagl.renderer.lightpass.LightPassRenderer;

/**
 * The deferred rendering path will render scene data by first generating a
 * {@link GBuffer} and then using the data from that g-buffer to perform
 * lighting calculations.
 * <p>
 * The deferred path do not handle meshes with {@link com.adrienben.games.bagl.engine.rendering.model.AlphaMode#BLEND}.
 *
 * @author adrien.
 */
public class DeferredPath extends AbstractRenderingPath {

    private final GBufferGenerator gBufferGenerator;
    private final GBuffer gBuffer;
    private final LightPassRenderer lightPassRenderer;

    public DeferredPath(final FrameBuffer targetBuffer) {
        super(targetBuffer);
        this.gBufferGenerator = new GBufferGenerator(targetBuffer.getWidth(), targetBuffer.getHeight());
        this.gBuffer = gBufferGenerator.getGBuffer();
        this.lightPassRenderer = new LightPassRenderer();
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractRenderingPath#destroy()
     */
    @Override
    public void destroy() {
        gBufferGenerator.destroy();
        lightPassRenderer.destroy();
    }

    /**
     * {@inheritDoc}
     * <p>
     * First render the scene in a {@link GBuffer} then perform a lighting pass
     * taking the shadow map into account if provided.
     *
     * @see AbstractRenderingPath#renderSceneData()
     */
    @Override
    public void renderSceneData() {
        performGeometryPass();
        performLightingPass();
    }

    private void performGeometryPass() {
        gBufferGenerator.generateGBuffer(sceneRenderData);
    }

    private void performLightingPass() {
        prepareResourcesForLightingPass();
        renderLightingPass();
        unbindResourcesPostLightingPass();
    }

    private void prepareResourcesForLightingPass() {
        targetBuffer.bind();
        gBuffer.copyDepthInto(targetBuffer);
    }

    private void renderLightingPass() {
        lightPassRenderer.setGBuffer(gBuffer);
        lightPassRenderer.setCascadedShadowMap(cascadedShadowMap);
        lightPassRenderer.setSceneRenderData(sceneRenderData);
        lightPassRenderer.renderLightPass();
    }

    private void unbindResourcesPostLightingPass() {
        targetBuffer.unbind();
    }

    public GBuffer getGBuffer() {
        return gBuffer;
    }
}
