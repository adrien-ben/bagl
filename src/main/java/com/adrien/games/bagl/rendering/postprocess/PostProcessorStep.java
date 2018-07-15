package com.adrien.games.bagl.rendering.postprocess;

import com.adrien.games.bagl.rendering.model.Mesh;
import com.adrien.games.bagl.rendering.model.MeshFactory;
import com.adrien.games.bagl.rendering.texture.Texture;

import static org.lwjgl.opengl.GL11.glDrawArrays;

/**
 * Abstract post processing step.
 * <p>
 * Post processing classes will need to extend this class.
 *
 * @author adrien
 */
public abstract class PostProcessorStep {

    private final Mesh screenQuadMesh;

    public PostProcessorStep() {
        this.screenQuadMesh = MeshFactory.createScreenQuad();
    }

    /**
     * Release resources instantiated by the step.
     */
    protected abstract void onDestroy();

    /**
     * Release resources.
     */
    public void destroy() {
        screenQuadMesh.destroy();
        onDestroy();
    }

    /**
     * Execute the step.
     * <p>
     * Process an input texture and return a modified version of it.
     *
     * @implSpec This method must return a non null {@link Texture}.
     */
    protected abstract Texture onProcess(Texture image);

    /**
     * Process {@code image}.
     */
    public Texture process(final Texture image) {
        screenQuadMesh.getVertexArray().bind();
        final var output = onProcess(image);
        screenQuadMesh.getVertexArray().unbind();
        return output;
    }

    protected void renderQuad() {
        glDrawArrays(screenQuadMesh.getPrimitiveType().getGlCode(), 0, screenQuadMesh.getVertexCount());
    }
}
