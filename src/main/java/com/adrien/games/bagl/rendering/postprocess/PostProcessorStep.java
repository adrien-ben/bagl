package com.adrien.games.bagl.rendering.postprocess;

import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.model.Mesh;
import com.adrien.games.bagl.rendering.model.MeshFactory;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.utils.ResourcePath;

import static org.lwjgl.opengl.GL11.glDrawArrays;

/**
 * Abstract post processing step.
 * <p>
 * Post processing classes will need to extend this class.
 *
 * @author adrien
 */
public abstract class PostProcessorStep {

    private static final String POST_PROCESS_VERTEX_SHADER_FILE = "classpath:/shaders/post/post_process.vert";

    private final Mesh screenQuadMesh;

    public PostProcessorStep() {
        this.screenQuadMesh = MeshFactory.createScreenQuad();
    }

    /**
     * Build a post process shader.
     * <p>
     * Post process shaders share the same vertex shader stage. (See
     * the shader file {@value POST_PROCESS_VERTEX_SHADER_FILE}).
     * <p>
     * They will have different fragment shader stages though. You specify
     * the fragment shader to you by passing the path to that shader file as
     * {@code fragmentPath}.
     */
    protected Shader buildProcessShader(final ResourcePath fragmentPath) {
        return Shader.builder()
                .vertexPath(ResourcePath.get(POST_PROCESS_VERTEX_SHADER_FILE))
                .fragmentPath(fragmentPath)
                .build();
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
