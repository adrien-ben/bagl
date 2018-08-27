package com.adrienben.games.bagl.deferred.gbuffer;

import com.adrienben.games.bagl.opengl.FrameBuffer;
import com.adrienben.games.bagl.opengl.FrameBufferParameters;
import com.adrienben.games.bagl.opengl.texture.Format;
import com.adrienben.games.bagl.opengl.texture.Texture2D;

/**
 * Geometry buffer.
 *
 * @author adrien.
 */
public class GBuffer {

    private static final int COLOR_TEXTURE_CHANNEL = 0;
    private static final int NORMAL_TEXTURE_CHANNEL = 1;
    private static final int EMISSIVE_TEXTURE_CHANNEL = 2;
    private static final int OCCLUSION_TEXTURE_CHANNEL = 3;

    private final FrameBuffer frameBuffer;

    public GBuffer(final int xResolution, final int yResolution) {
        frameBuffer = new FrameBuffer(xResolution, yResolution, FrameBufferParameters.builder()
                .colorOutputFormat(Format.RGBA8, Format.RGBA16F, Format.RGB16F, Format.RG8)
                .build());
    }

    /**
     * Release resources.
     */
    public void destroy() {
        frameBuffer.destroy();
    }

    public void clear() {
        frameBuffer.clear();
    }

    public void bind() {
        frameBuffer.bind();
    }

    public void unbind() {
        frameBuffer.unbind();
    }

    /**
     * Copy the depth buffer into {@code target}.
     *
     * @param target The target frame buffer. It NEEDS to be bound.
     */
    public void copyDepthInto(final FrameBuffer target) {
        target.copyFrom(frameBuffer, true, false);
    }

    public Texture2D getColorTexture() {
        return frameBuffer.getColorTexture(COLOR_TEXTURE_CHANNEL);
    }

    public Texture2D getNormalTexture() {
        return frameBuffer.getColorTexture(NORMAL_TEXTURE_CHANNEL);
    }

    public Texture2D getEmissiveTexture() {
        return frameBuffer.getColorTexture(EMISSIVE_TEXTURE_CHANNEL);
    }

    public Texture2D getOcclusionTexture() {
        return frameBuffer.getColorTexture(OCCLUSION_TEXTURE_CHANNEL);
    }

    public Texture2D getDepthTexture() {
        return frameBuffer.getDepthTexture();
    }
}
