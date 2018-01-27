package com.adrien.games.bagl.rendering.texture;

import com.adrien.games.bagl.utils.AssertUtils;

import java.util.Objects;

/**
 * <p>Parameters for textures. The different parameters are :
 * <ul>
 * <li>format : The format of the texture. Default is RGBA8.
 * <li>minFilter : The minification filter of the texture. Default is LINEAR.
 * <li>magFilter : The magnification filter of the texture. Default is LINEAR.
 * <li>sWrap : The wrapping of the texture for the u component. Default is REPEAT.
 * <li>tWrap : The wrapping of the texture for the v component. Default is REPEAT.
 * <li>anisotropic : The level of anisotropic filtering (should be 0, 2, 4, 8 or 16). Default is 0;
 * <li>mipmaps : Flag indicating if mimaps must be generated. Default is false.
 * </ul>
 * <p>
 * Too construct an instance of this class you must use the provided builder as follows:
 * <pre>
 * final TextureParameters params = TextureParameters.builder().format(Format.RGBA8).mipmaps().build();
 * </pre>
 * Once built, the parameters can't be changed
 *
 * @author Adrien
 * @see Format
 * @see Filter
 * @see Wrap
 */
public final class TextureParameters {

    private final Format format;
    private final Filter minFilter;
    private final Filter magFilter;
    private final Wrap sWrap;
    private final Wrap tWrap;
    private final int anisotropic;
    private final boolean mipmaps;

    private TextureParameters(final Builder builder) {
        this.format = builder.format;
        this.minFilter = builder.minFilter;
        this.magFilter = builder.magFilter;
        this.sWrap = builder.sWrap;
        this.tWrap = builder.tWrap;
        this.anisotropic = builder.anisotropic;
        this.mipmaps = builder.mipmaps;
    }

    /**
     * Retrieve a texture builder
     *
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public Format getFormat() {
        return this.format;
    }

    public Filter getMinFilter() {
        return this.minFilter;
    }

    public Filter getMagFilter() {
        return this.magFilter;
    }

    public Wrap getsWrap() {
        return this.sWrap;
    }

    public Wrap gettWrap() {
        return this.tWrap;
    }

    public int getAnisotropic() {
        return this.anisotropic;
    }

    public boolean getMipmaps() {
        return this.mipmaps;
    }

    /**
     * Texture parameters builder
     */
    public static class Builder {

        private Format format = Format.RGBA8;
        private Filter minFilter = Filter.LINEAR;
        private Filter magFilter = Filter.LINEAR;
        private Wrap sWrap = Wrap.REPEAT;
        private Wrap tWrap = Wrap.REPEAT;
        private int anisotropic = 0;
        private boolean mipmaps = false;

        private Builder() {
            // left empty
        }

        public TextureParameters build() {
            return new TextureParameters(this);
        }

        public Builder format(final Format format) {
            this.format = Objects.requireNonNull(format, "format cannot be null");
            return this;
        }

        public Builder minFilter(final Filter filter) {
            this.minFilter = Objects.requireNonNull(filter, "filter cannot be null");
            return this;
        }

        public Builder magFilter(final Filter filter) {
            this.magFilter = Objects.requireNonNull(filter, "filter cannot be null");
            return this;
        }

        public Builder sWrap(final Wrap wrap) {
            this.sWrap = Objects.requireNonNull(wrap, "wrap cannot be null");
            return this;
        }

        public Builder tWrap(final Wrap wrap) {
            this.tWrap = Objects.requireNonNull(wrap, "wrap cannot be null");
            return this;
        }

        public Builder anisotropic(final int anisotropic) {
            this.anisotropic = AssertUtils.validate(anisotropic, v -> v >= 0,
                    "anisotropic must be at least 0");
            return this;
        }

        public Builder mipmaps(final boolean mipmaps) {
            this.mipmaps = mipmaps;
            return this;
        }
    }
}
