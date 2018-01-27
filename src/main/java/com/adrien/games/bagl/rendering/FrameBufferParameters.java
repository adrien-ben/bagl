package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.rendering.texture.Format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Parameters for {@link FrameBuffer}
 * <p>
 * Available parameters:
 * <ul>
 * <li>hasDepthStencil (default = true)
 * <li>depthStencilFormat (default = {@link Format#DEPTH_32F})
 * <li>colorOutputFormats (default = empty)
 * </ul>
 * <p>
 * {@code hasDepthStencil} specifies if a depth/stencil texture will be attached
 * to the frame buffer. {@code depthStencilFormat} specifies the format of the
 * depth/stencil channel if requested. {@code colorOutputFormats} is a list of
 * {@link Format}; for each of the specified format, a color texture will be
 * attached to the frame buffer. This list can stay empty
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

    private final boolean hasDepthStencil;
    private final Format depthStencilFormat;
    private final List<Format> colorOutputFormats;

    private FrameBufferParameters(final Builder builder) {
        this.hasDepthStencil = builder.hasDepthStencil;
        this.depthStencilFormat = builder.depthStencilFormat;
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

    public boolean hadDepthStencil() {
        return this.hasDepthStencil;
    }

    public Format getDepthStencilFormat() {
        return this.depthStencilFormat;
    }

    public List<Format> getColorOutputs() {
        return this.colorOutputFormats;
    }

    /**
     * Frame buffer parameters builder
     */
    public static class Builder {

        private boolean hasDepthStencil = true;
        private Format depthStencilFormat = Format.DEPTH_32F;
        private List<Format> colorOutputFormats = new ArrayList<>();

        private Builder() {
            // prevents initialization
        }

        public FrameBufferParameters build() {
            return new FrameBufferParameters(this);
        }

        /**
         * Enable/disable the depth stencil channel
         *
         * @param hasDepthStencil Depth/stencil flag
         * @return This
         */
        public Builder hasDepthStencil(final boolean hasDepthStencil) {
            this.hasDepthStencil = hasDepthStencil;
            return this;
        }

        /**
         * Set the format of the depth stencil channel
         *
         * @param depthStencilFormat The format to use
         * @return This
         */
        public Builder depthStencilFormat(final Format depthStencilFormat) {
            this.depthStencilFormat = Objects.requireNonNull(depthStencilFormat,
                    "depthStencilFormat cannot be null");
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
