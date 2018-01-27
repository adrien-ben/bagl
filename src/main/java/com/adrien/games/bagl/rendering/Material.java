package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.rendering.texture.Texture;

import java.util.Objects;

/**
 * Material class
 * <p>
 * Represents the material of a mesh. It contains:
 * <ul>
 * <li>The diffuse color (default: {@link Color#WHITE}
 * <li>An emissive color (default {@link Color#WHITE}
 * <li>An emissive intensity (default: 0f)
 * <li>A roughness factor (default: 0.5f)
 * <li>A metalness factor (default: 0f)
 * <p>
 * <li>A diffuse texture (default: null)
 * <li>An emissive texture (default: null)
 * <li>An ORM (Occlusion/Roughness/Metalness) texture (default: null)
 * <li>A normal texture (default: null)
 * </ul>
 *
 * @author adrien
 */
public class Material {

    private static final int DIFFUSE_MAP_CHANNEL = 0;
    private static final int EMISSIVE_MAP_CHANNEL = 1;
    private static final int ORM_MAP_CHANNEL = 2;
    private static final int NORMAL_MAP_CHANNEL = 3;

    private Color diffuseColor = Color.WHITE;
    private Color emissiveColor = Color.WHITE;
    private float emissiveIntensity = 0f;
    private float roughness = 0.5f;
    private float metallic = 0f;

    private Texture diffuseMap = null;
    private Texture emissiveMap = null;
    private Texture ormMap = null;
    private Texture normalMap = null;

    /**
     * Apply the current material to a shader
     *
     * @param shader The shader to apply the material to
     */
    public void applyTo(final Shader shader) {
        shader.setUniform("uMaterial.diffuseColor", this.diffuseColor);
        shader.setUniform("uMaterial.emissiveColor", this.emissiveColor);
        shader.setUniform("uMaterial.emissiveIntensity", this.emissiveIntensity);
        shader.setUniform("uMaterial.roughness", this.roughness);
        shader.setUniform("uMaterial.metallic", this.metallic);

        final boolean hasDiffuseMap = Objects.nonNull(this.diffuseMap);
        shader.setUniform("uMaterial.hasDiffuseMap", hasDiffuseMap);
        if (hasDiffuseMap) {
            shader.setUniform("uMaterial.diffuseMap", DIFFUSE_MAP_CHANNEL);
            this.diffuseMap.bind(DIFFUSE_MAP_CHANNEL);
        }

        final boolean hasEmissiveMap = Objects.nonNull(this.emissiveMap);
        shader.setUniform("uMaterial.hasEmissiveMap", hasEmissiveMap);
        if (hasEmissiveMap) {
            shader.setUniform("uMaterial.emissiveMap", EMISSIVE_MAP_CHANNEL);
            this.emissiveMap.bind(EMISSIVE_MAP_CHANNEL);
        }

        final boolean hasOrmMap = Objects.nonNull(this.ormMap);
        shader.setUniform("uMaterial.hasOrmMap", hasOrmMap);
        if (hasOrmMap) {
            shader.setUniform("uMaterial.ormMap", ORM_MAP_CHANNEL);
            this.ormMap.bind(ORM_MAP_CHANNEL);
        }

        final boolean hasNormalMap = Objects.nonNull(this.normalMap);
        shader.setUniform("uMaterial.hasNormalMap", hasNormalMap);
        if (hasNormalMap) {
            shader.setUniform("uMaterial.normalMap", NORMAL_MAP_CHANNEL);
            this.normalMap.bind(NORMAL_MAP_CHANNEL);
        }
    }

    /**
     * Release resources
     */
    public void destroy() {
        if (Objects.nonNull(this.diffuseMap)) {
            this.diffuseMap.destroy();
        }
        if (Objects.nonNull(this.emissiveMap)) {
            this.emissiveMap.destroy();
        }
        if (Objects.nonNull(this.ormMap)) {
            this.ormMap.destroy();
        }
        if (Objects.nonNull(this.normalMap)) {
            this.normalMap.destroy();
        }
    }

    public boolean hasNormalMap() {
        return Objects.nonNull(this.normalMap);
    }

    public Color getDiffuseColor() {
        return diffuseColor;
    }

    public Material setDiffuseColor(final Color diffuseColor) {
        this.diffuseColor = diffuseColor;
        return this;
    }

    public Color getEmissiveColor() {
        return this.emissiveColor;
    }

    public Material setEmissiveColor(final Color emissiveColor) {
        this.emissiveColor = emissiveColor;
        return this;
    }

    public float getEmissiveIntensity() {
        return this.emissiveIntensity;
    }

    public Material setEmissiveIntensity(final float emissiveIntensity) {
        this.emissiveIntensity = emissiveIntensity;
        return this;
    }

    public float getRoughness() {
        return roughness;
    }

    public Material setRoughness(final float roughness) {
        this.roughness = roughness;
        return this;
    }

    public float getMetallic() {
        return metallic;
    }

    public Material setMetallic(final float metallic) {
        this.metallic = metallic;
        return this;
    }


    public Texture getDiffuseMap() {
        return diffuseMap;
    }

    public Material setDiffuseMap(final Texture diffuseMap) {
        this.diffuseMap = diffuseMap;
        return this;
    }

    public Texture getEmissiveMap() {
        return this.emissiveMap;
    }

    public Material setEmissiveMap(final Texture emissiveMap) {
        this.emissiveMap = emissiveMap;
        return this;
    }

    public Texture getOrmMap() {
        return this.ormMap;
    }

    public Material setOrmMap(final Texture ormMap) {
        this.ormMap = ormMap;
        return this;
    }

    public Texture getNormalMap() {
        return normalMap;
    }

    public Material setNormalMap(final Texture normalMap) {
        this.normalMap = normalMap;
        return this;
    }
}
