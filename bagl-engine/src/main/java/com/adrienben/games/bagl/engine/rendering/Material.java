package com.adrienben.games.bagl.engine.rendering;

import com.adrienben.games.bagl.core.Color;
import com.adrienben.games.bagl.core.validation.Validation;
import com.adrienben.games.bagl.engine.rendering.model.AlphaMode;
import com.adrienben.games.bagl.opengl.texture.Texture2D;

import java.util.Objects;
import java.util.Optional;

/**
 * Material class
 * <p>
 * Represents the material of a mesh. It contains:
 * <ul>
 * <li>The diffuse color (default: {@link Color#WHITE})
 * <li>An emissive color (default {@link Color#WHITE})
 * <li>An emissive intensity (default: 0f)
 * <li>A roughness factor (default: 0.5f)
 * <li>A metalness factor (default: 0f)
 * <p>
 * <li>A diffuse texture (default: null)
 * <li>An emissive texture (default: null)
 * <li>An ORM (Occlusion/Roughness/Metalness) texture (default: null)
 * <li>A normal texture (default: null)
 * <p>
 * <li>A double sided flag (default: false)
 * <li>The alpha mode (default: {@link AlphaMode#OPAQUE})
 * <li>The alpha cutoff for {@link AlphaMode#MASK} (default 0f)
 * </ul>
 * <p>
 * To construct a material you have to use a material builder :
 * <pre>
 *     final Material material = Material.builder()
 *         .diffuse(Color.RED)
 *         .metallic(1f)
 *         .roughness(0.1f)
 *         .build();
 * </pre>
 * Once built, a material cannot be changed
 *
 * @author adrien
 */
public class Material {

    public static final int DIFFUSE_MAP_CHANNEL = 0;
    public static final int EMISSIVE_MAP_CHANNEL = 1;
    public static final int ORM_MAP_CHANNEL = 2;
    public static final int NORMAL_MAP_CHANNEL = 3;

    private final Color diffuseColor;
    private final Color emissiveColor;
    private final float emissiveIntensity;
    private final float roughness;
    private final float metallic;

    private final Texture2D diffuseMap;
    private final Texture2D emissiveMap;
    private final Texture2D ormMap;
    private final Texture2D normalMap;

    private final boolean doubleSided;
    private final AlphaMode alphaMode;
    private final float alphaCutoff;

    private Material(final Builder builder) {
        this.diffuseColor = builder.diffuseColor;
        this.emissiveColor = builder.emissiveColor;
        this.emissiveIntensity = builder.emissiveIntensity;
        this.roughness = builder.roughness;
        this.metallic = builder.metallic;

        this.diffuseMap = builder.diffuseMap;
        this.emissiveMap = builder.emissiveMap;
        this.ormMap = builder.ormMap;
        this.normalMap = builder.normalMap;

        this.doubleSided = builder.doubleSided;
        this.alphaMode = builder.alphaMode;
        this.alphaCutoff = builder.alphaCutoff;
    }

    /**
     * Return a material builder
     *
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Release resources
     */
    public void destroy() {
        getDiffuseMap().ifPresent(Texture2D::destroy);
        getEmissiveMap().ifPresent(Texture2D::destroy);
        getOrmMap().ifPresent(Texture2D::destroy);
        getNormalMap().ifPresent(Texture2D::destroy);
    }

    public Color getDiffuseColor() {
        return diffuseColor;
    }

    public Color getEmissiveColor() {
        return emissiveColor;
    }

    public float getEmissiveIntensity() {
        return emissiveIntensity;
    }

    public float getRoughness() {
        return roughness;
    }

    public float getMetallic() {
        return metallic;
    }

    public Optional<Texture2D> getDiffuseMap() {
        return Optional.ofNullable(diffuseMap);
    }

    public Optional<Texture2D> getEmissiveMap() {
        return Optional.ofNullable(emissiveMap);
    }

    public Optional<Texture2D> getOrmMap() {
        return Optional.ofNullable(ormMap);
    }

    public Optional<Texture2D> getNormalMap() {
        return Optional.ofNullable(normalMap);
    }

    public boolean isDoubleSided() {
        return doubleSided;
    }

    public AlphaMode getAlphaMode() {
        return alphaMode;
    }

    public float getAlphaCutoff() {
        return alphaCutoff;
    }

    /**
     * Material builder
     */
    public static class Builder {
        private Color diffuseColor = Color.WHITE;
        private Color emissiveColor = Color.BLACK;
        private float emissiveIntensity = 0f;
        private float roughness = 1.0f;
        private float metallic = 1.0f;

        private Texture2D diffuseMap = null;
        private Texture2D emissiveMap = null;
        private Texture2D ormMap = null;
        private Texture2D normalMap = null;

        private boolean doubleSided = false;
        private AlphaMode alphaMode = AlphaMode.OPAQUE;
        private float alphaCutoff = 0f;

        /**
         * Private constructor to private instantiation
         */
        private Builder() {
        }

        /**
         * Build a new material
         *
         * @return A new material
         */
        public Material build() {
            return new Material(this);
        }

        public Builder diffuse(final Color color) {
            this.diffuseColor = Objects.requireNonNull(color, "color cannot be null");
            return this;
        }

        public Builder emissive(final Color color) {
            this.emissiveColor = Objects.requireNonNull(color, "color cannot be null");
            return this;
        }

        public Builder emissiveIntensity(final float intensity) {
            this.emissiveIntensity = Validation.validate(intensity, v -> v >= 0,
                    "intensity must be positive");
            return this;
        }

        public Builder roughness(final float roughness) {
            this.roughness = Validation.validate(roughness, v -> v >= 0 && v <= 1,
                    "roughness must be in [0..1]");
            return this;
        }

        public Builder metallic(final float metallic) {
            this.metallic = Validation.validate(metallic, v -> v >= 0 && v <= 1,
                    "metallic must be in [0..1]");
            return this;
        }

        public Builder diffuse(final Texture2D diffuseMap) {
            this.diffuseMap = diffuseMap;
            return this;
        }

        public Builder emissive(final Texture2D emissiveMap) {
            this.emissiveMap = emissiveMap;
            return this;
        }

        public Builder orm(final Texture2D ormMap) {
            this.ormMap = ormMap;
            return this;
        }

        public Builder normals(final Texture2D normalMap) {
            this.normalMap = normalMap;
            return this;
        }

        public Builder doubleSided(final boolean doubleSided) {
            this.doubleSided = doubleSided;
            return this;
        }

        public Builder alphaMode(final AlphaMode alphaMode) {
            this.alphaMode = alphaMode;
            return this;
        }

        public Builder alphaCutoff(final float alphaCutoff) {
            this.alphaCutoff = alphaCutoff;
            return this;
        }
    }
}
