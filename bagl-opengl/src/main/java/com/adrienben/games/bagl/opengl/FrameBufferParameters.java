package com.adrienben.games.bagl.opengl;

import com.adrienben.games.bagl.opengl.texture.CompareFunction;
import com.adrienben.games.bagl.opengl.texture.Format;
import com.adrienben.games.bagl.opengl.texture.TextureParameters;

import java.util.*;
import java.util.stream.Stream;

/**
 * Parameters for {@link FrameBuffer}
 * <p>
 * Available parameters:
 * <ul>
 * <li>hasDepthStencil (default = true)
 * <li>depthStencilFormat (default = {@link Format#DEPTH_32F})
 * <li>compareFunction (default = {@link CompareFunction#NONE})
 * <li>colorOutputFormats (default = empty)
 * </ul>
 * <p>
 * {@code hasDepthStencil} specifies if a depth/stencil texture will be attached
 * to the frame buffer. {@code depthStencilFormat} specifies the format of the
 * depth/stencil channel if requested. {@code colorOutputFormats} is a list of
 * {@link Format}; for each of the specified format, a color texture will be
 * attached to the frame buffer. This list can stay empty. {@code compareFunction}
 * the compare function parameter for the depth buffer texture. This can be used for
 * shadow mapping.
 * <p>
 * To create a instance of this class, you have to use the inner builder:
 * <pre>
 * final FrameBufferParameters params = FrameBufferParameters.builder()
 *     .hasDepthStencil(false)
 *     .build();
 * </pre>
 * Or you can create a default
 * Frame buffer parameters instances are immutable
 *
 * @author adrien
 */
public final class FrameBufferParameters {

    private static final FrameBufferParameters DEFAULT = new FrameBufferParameters(new Builder());

    private final TextureParameters depthStencilTextureParameters;
    private final List<Format> colorOutputFormats;

    private FrameBufferParameters(final Builder builder) {
        this.depthStencilTextureParameters = builder.depthTextureParameters;
        this.colorOutputFormats = Collections.unmodifiableList(builder.colorOutputFormats);
    }

    /***
     * Get the default instance
     *
     * @return The default instance
     */
    public static FrameBufferParameters getDefault() {
        return DEFAULT;
    }

    /**
     * Return a frame buffer parameters builder
     *
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public Optional<TextureParameters> getDepthStencilTextureParameters() {
        return Optional.ofNullable(depthStencilTextureParameters);
    }

    public List<Format> getColorOutputs() {
        return this.colorOutputFormats;
    }

    /**
     * Frame buffer parameters builder
     */
    public static class Builder {

        private TextureParameters depthTextureParameters = TextureParameters.builder().format(Format.DEPTH_32F).build();
        private List<Format> colorOutputFormats = new ArrayList<>();

        private Builder() {
        }

        public FrameBufferParameters build() {
            return new FrameBufferParameters(this);
        }

        public Builder depthStencilTextureParameters(final TextureParameters depthTextureParameters) {
            this.depthTextureParameters = depthTextureParameters;
            return this;
        }

        /**
         * Add one or more color output formats
         * <p>
         * Passing no format will have no effect
         *
         * @param format The format(s) to add
         * @return This
         */
        public Builder colorOutputFormat(final Format... format) {
            if (Objects.nonNull(format)) {
                Stream.of(format).filter(Objects::nonNull).forEach(this.colorOutputFormats::add);
            }
            return this;
        }
    }
}
