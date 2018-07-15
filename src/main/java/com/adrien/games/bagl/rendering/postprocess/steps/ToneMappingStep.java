package com.adrien.games.bagl.rendering.postprocess.steps;

import com.adrien.games.bagl.rendering.FrameBuffer;
import com.adrien.games.bagl.rendering.FrameBufferParameters;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.postprocess.PostProcessorStep;
import com.adrien.games.bagl.rendering.texture.Format;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.utils.FileUtils;

import static com.adrien.games.bagl.rendering.postprocess.PostProcessor.POST_PROCESS_VERTEX_SHADER_FILE;

/**
 * Tone mapping and gamma correction step.
 *
 * @author adrien
 */
public class ToneMappingStep extends PostProcessorStep {

    private FrameBuffer frameBuffer;
    private Shader toneMappingShader;

    public ToneMappingStep(final int xResolution, final int yResolution) {
        final var parameters = FrameBufferParameters.builder().hasDepthStencil(false).colorOutputFormat(Format.RGBA8).build();
        this.frameBuffer = new FrameBuffer(xResolution, yResolution, parameters);
        this.toneMappingShader = Shader.builder()
                .vertexPath(FileUtils.getResourceAbsolutePath(POST_PROCESS_VERTEX_SHADER_FILE))
                .fragmentPath(FileUtils.getResourceAbsolutePath("/shaders/post/tone_mapping.frag"))
                .build();
    }

    /**
     * {@inheritDoc}
     *
     * @see PostProcessorStep#onDestroy()
     */
    @Override
    public void onDestroy() {
        frameBuffer.destroy();
        toneMappingShader.destroy();
    }

    /**
     * Apply tone mapping and gamma correction on {@code image}.
     */
    @Override
    public Texture onProcess(final Texture image) {
        frameBuffer.bind();
        frameBuffer.clear();
        image.bind();
        toneMappingShader.bind();

        renderQuad();

        Shader.unbind();
        Texture.unbind();
        frameBuffer.unbind();

        return frameBuffer.getColorTexture(0);
    }
}