package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.rendering.texture.Format;

import java.util.ArrayList;
import java.util.List;

/**
 * Parameters for {@link FrameBuffer}
 * <p>
 * This class allows to specify the format of each of the color output textures
 * and the format of the depth texture.
 */
public class FrameBufferParameters {

    private boolean hasDepth = true;

    private Format depthTextureFormat = Format.DEPTH_32F;

    private List<Format> colorOutputFormats = new ArrayList<>();

    public FrameBufferParameters hasDepth(final boolean depth) {
        this.hasDepth = depth;
        return this;
    }

    public boolean hadDepth() {
        return this.hasDepth;
    }

    /**
     * A color output texture will be created in the frame buffer for
     * each color output contained in the passed in parameter instance.
     * The outputs will be created in the order that they were added to
     * the parameters.
     *
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

}
