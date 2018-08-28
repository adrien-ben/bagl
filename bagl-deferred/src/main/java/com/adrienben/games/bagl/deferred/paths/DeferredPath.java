package com.adrienben.games.bagl.deferred.paths;

import com.adrienben.games.bagl.deferred.gbuffer.GBuffer;
import com.adrienben.games.bagl.deferred.gbuffer.generator.GBufferGenerator;
import com.adrienben.games.bagl.deferred.lightpass.LightPassRenderer;
import com.adrienben.games.bagl.opengl.FrameBuffer;

/**
 * The deferred rendering path will render scene data by first generating a
 * {@link GBuffer} and then using the data from that g-buffer to perform
 * lighting calculations.
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
        targetBuffer.clear();
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
