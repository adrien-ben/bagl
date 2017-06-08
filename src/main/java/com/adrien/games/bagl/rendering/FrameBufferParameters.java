package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.rendering.texture.Format;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Parameters for {@link FrameBuffer}.
 * <p>This class allows to specify the format of each of the color output textures
 * and the format of the depth texture.
 */
public class FrameBufferParameters {

    private Format depthTextureFormat = Format.DEPTH_32F;

    private List<Format> colorOutputFormats = new ArrayList<>();

    /**
     * A color output texture will be created in the framebuffer for
     * each color output contained in the passed in parameter instance.
     * The outputs will be created in the order that they were added to
     * the parameters.
     * @param format The format of the color output texture.
     * @return This for chaining.
     */
    public FrameBufferParameters addColorOutput(Format format) {
        this.colorOutputFormats.add(format);
        return this;
    }

    public Format getDepthTextureFormat() {
        return this.depthTextureFormat;
    }

    public FrameBufferParameters setDepthTextureFormat(Format depthTextureFormat) {
        this.depthTextureFormat = depthTextureFormat;
        return this;
    }

    public List<Format> getColorOutputs() {
        return this.colorOutputFormats;
    }

    /**
     * Generates parameters for an RGB8 framebuffer.
     * @param colorOutputs The number of RGBA color outputs.
     * @return A new instance of {@link FrameBufferParameters}.
     */
    public static FrameBufferParameters generatesRGBA8Parameters(int colorOutputs) {
        final FrameBufferParameters parameters = new FrameBufferParameters();
        for(int i = 0; i < colorOutputs; i++) {
            parameters.addColorOutput(Format.RGBA8);
        }
        return parameters;
    }

}
