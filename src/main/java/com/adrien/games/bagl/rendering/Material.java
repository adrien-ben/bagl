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
 * <li>A diffuse texture (default: null)
 * <li>A roughness factor (default: 0.5f)
 * <li>A roughness texture (default: null)
 * <li>A metalness factor (default: 0f)
 * <li>A metalness texture (default: null)
 * <li>An emissive color (default {@link Color#WHITE}
 * <li>An emissive intensity (default: 0f)
 * </ul>
 *
 * @author adrien
 */
public class Material {

    private static final int DIFFUSE_MAP_CHANNEL = 0;
    private static final int ROUGHNESS_MAP_CHANNEL = 1;
    private static final int METALLIC_MAP_CHANNEL = 2;
    private static final int NORMAL_MAP_CHANNEL = 3;

    private Color diffuseColor = Color.WHITE;
    private Texture diffuseMap = null;
    private float roughness = 0.5f;
    private Texture roughnessMap = null;
    private float metallic = 0f;
    private Texture metallicMap = null;
    private Texture normalMap = null;
    private Color emissiveColor = Color.WHITE;
    private float emissiveIntensity = 0f;

    /**
     * Apply the current material to a shader
     *
     * @param shader The shader to apply the material to
     */
    public void applyTo(final Shader shader) {
        shader.setUniform("uMaterial.diffuseColor", this.diffuseColor);
        final boolean hasDiffuseMap = Objects.nonNull(this.diffuseMap);
        shader.setUniform("uMaterial.hasDiffuseMap", hasDiffuseMap);
        if (hasDiffuseMap) {
            shader.setUniform("uMaterial.diffuseMap", DIFFUSE_MAP_CHANNEL);
            this.diffuseMap.bind(DIFFUSE_MAP_CHANNEL);
        }

        shader.setUniform("uMaterial.roughness", this.roughness);
        final boolean hasRoughnessMap = Objects.nonNull(this.roughnessMap);
        shader.setUniform("uMaterial.hasRoughnessMap", hasRoughnessMap);
        if (hasRoughnessMap) {
            shader.setUniform("uMaterial.roughnessMap", ROUGHNESS_MAP_CHANNEL);
            this.roughnessMap.bind(ROUGHNESS_MAP_CHANNEL);
        }

        shader.setUniform("uMaterial.metallic", this.metallic);
        final boolean hasMetallicMap = Objects.nonNull(this.metallicMap);
        shader.setUniform("uMaterial.hasMetallicMap", hasMetallicMap);
        if (hasMetallicMap) {
            shader.setUniform("uMaterial.metallicMap", METALLIC_MAP_CHANNEL);
            this.metallicMap.bind(METALLIC_MAP_CHANNEL);
        }

        final boolean hasNormalMap = Objects.nonNull(this.normalMap);
        shader.setUniform("uMaterial.hasNormalMap", hasNormalMap);
        if (hasNormalMap) {
            shader.setUniform("uMaterial.normalMap", NORMAL_MAP_CHANNEL);
            this.normalMap.bind(NORMAL_MAP_CHANNEL);
        }

        shader.setUniform("uMaterial.emissiveColor", this.emissiveColor);
        shader.setUniform("uMaterial.emissiveIntensity", this.emissiveIntensity);
    }

    public void destroy() {
        if (Objects.nonNull(this.diffuseMap)) {
            this.diffuseMap.destroy();
        }
        if (Objects.nonNull(this.roughnessMap)) {
            this.roughnessMap.destroy();
        }
        if (Objects.nonNull(this.metallicMap)) {
            this.metallicMap.destroy();
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

    public Texture getDiffuseMap() {
        return diffuseMap;
    }

    public Material setDiffuseMap(final Texture diffuseMap) {
        this.diffuseMap = diffuseMap;
        return this;
    }

    public float getRoughness() {
        return roughness;
    }

    public Material setRoughness(final float roughness) {
        this.roughness = roughness;
        return this;
    }

    public Texture getRoughnessMap() {
        return roughnessMap;
    }

    public Material setRoughnessMap(final Texture roughnessMap) {
        this.roughnessMap = roughnessMap;
        return this;
    }

    public float getMetallic() {
        return metallic;
    }

    public Material setMetallic(final float metallic) {
        this.metallic = metallic;
        return this;
    }

    public Texture getMetallicMap() {
        return metallicMap;
    }

    public Material setMetallicMap(final Texture metallicMap) {
        this.metallicMap = metallicMap;
        return this;
    }

    public Texture getNormalMap() {
        return normalMap;
    }

    public Material setNormalMap(final Texture normalMap) {
        this.normalMap = normalMap;
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
}
