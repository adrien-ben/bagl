package com.adrienben.games.bagl.engine.rendering.postprocess.steps;

import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.engine.rendering.postprocess.PostProcessorStep;
import com.adrienben.games.bagl.opengl.FrameBuffer;
import com.adrienben.games.bagl.opengl.FrameBufferParameters;
import com.adrienben.games.bagl.opengl.shader.Shader;
import com.adrienben.games.bagl.opengl.texture.Format;
import com.adrienben.games.bagl.opengl.texture.Texture2D;

/**
 * Luma post processing step.
 * <p>
 * This step will compute the luma of an input image and return a copy of that image with
 * the luma value stored in the alpha channel.
 * <p>
 * This step can be useful if the next step requires the luma to be encoded in the alpha channel
 * but that the previous step does not do that. (For example {@link FxaaStep} requires the luma).
 *
 * @author adrien
 */
public class LumaStep extends PostProcessorStep {

    private FrameBuffer frameBuffer;
    private Shader lumaShader;

    public LumaStep(final int xResolution, final int yResolution) {
        this.frameBuffer = new FrameBuffer(xResolution, yResolution, FrameBufferParameters.builder().depthStencilTextureParameters(null).colorOutputFormat(Format.RGBA8).build());
        this.lumaShader = buildProcessShader(ResourcePath.get("classpath:/shaders/post/luma.frag"));
    }

    /**
     * {@inheritDoc}
     *
     * @see PostProcessorStep#destroy()
     */
    @Override
    protected void onDestroy() {
        frameBuffer.destroy();
        lumaShader.destroy();
    }

    /**
     * Compute the luma of {@code image} and return a copy of it with the luma value stored
     * in the alpha channel.
     */
    @Override
    protected Texture2D onProcess(final Texture2D image) {
        frameBuffer.bind();
        lumaShader.bind();
        image.bind();

        renderQuad();

        image.unbind();
        Shader.unbind();
        frameBuffer.unbind();

        return frameBuffer.getColorTexture(0);
    }
}
