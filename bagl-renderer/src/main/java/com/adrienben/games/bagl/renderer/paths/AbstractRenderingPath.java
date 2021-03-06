package com.adrienben.games.bagl.renderer.paths;

import com.adrienben.games.bagl.opengl.FrameBuffer;
import com.adrienben.games.bagl.renderer.data.SceneRenderData;
import com.adrienben.games.bagl.renderer.shadow.CascadedShadowMap;

/**
 * Abstract rendering path.
 * <p>
 * A rendering path is responsible for rendering data contained is a {@link SceneRenderData}.
 * If provided it can also make use of the {@link CascadedShadowMap}. The rendering will take
 * place in the provided {@link FrameBuffer}.
 *
 * @author adrien
 */
public abstract class AbstractRenderingPath {

    protected final FrameBuffer targetBuffer;
    protected SceneRenderData sceneRenderData;
    protected CascadedShadowMap cascadedShadowMap;

    protected AbstractRenderingPath(final FrameBuffer targetBuffer) {
        this.targetBuffer = targetBuffer;
    }

    /**
     * Release resources.
     */
    public abstract void destroy();

    /**
     * Render the content of the {@link SceneRenderData} provided to the path.
     *
     * @apiNote Implementations are free to render only parts of the scene data. For example, one path
     * could draw onlu opaque objects when another could draw everything.
     */
    public abstract void renderSceneData();

    /**
     * Set the {@link SceneRenderData} to render at the next {@link AbstractRenderingPath#renderSceneData()}
     * call.
     *
     * @param sceneRenderData The scene data to later render.
     */
    public void setSceneRenderData(final SceneRenderData sceneRenderData) {
        this.sceneRenderData = sceneRenderData;
    }

    /**
     * Set the {@link CascadedShadowMap} to use when rendering.
     *
     * @param cascadedShadowMap The shadow map to use when rendering.
     */
    public void setCascadedShadowMap(final CascadedShadowMap cascadedShadowMap) {
        this.cascadedShadowMap = cascadedShadowMap;
    }
}
