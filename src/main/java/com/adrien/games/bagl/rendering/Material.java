package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.rendering.texture.Texture;

import java.util.Objects;

public class Material {

    private static final String DIFFUSE_COLOR_SHADER_UNIFORM = "uMaterial.diffuseColor";
    private static final String DIFFUSE_MAP_SHADER_UNIFORM = "uMaterial.diffuseMap";
    private static final String DIFFUSE_MAP_FLAG_SHADER_UNIFORM = "uMaterial.hasDiffuseMap";
    private static final String ROUGHNESS_SHADER_UNIFORM = "uMaterial.roughness";
    private static final String ROUGHNESS_MAP_SHADER_UNIFORM = "uMaterial.roughnessMap";
    private static final String ROUGHNESS_MAP_FLAG_SHADER_UNIFORM = "uMaterial.hasRoughnessMap";
    private static final String METALLIC_SHADER_UNIFORM = "uMaterial.metallic";
    private static final String METALLIC_MAP_SHADER_UNIFORM = "uMaterial.metallicMap";
    private static final String METALLIC_MAP_FLAG_SHADER_UNIFORM = "uMaterial.hasMetallicMap";
    private static final String NORMAL_MAP_SHADER_UNIFORM = "uMaterial.normalMap";
    private static final String NORMAL_MAP_FLAG_SHADER_UNIFORM = "uMaterial.hasNormalMap";

    private static final int DIFFUSE_MAP_CHANNEL = 0;
    private static final int ROUGHNESS_MAP_CHANNEL = 1;
    private static final int METALLIC_MAP_CHANNEL = 2;
    private static final int NORMAL_MAP_CHANNEL = 3;

    private Color diffuseColor = new Color(1, 1, 1);
    private Texture diffuseMap = null;
    private float roughness = 0.5f;
    private Texture roughnessMap = null;
    private float metallic = 0f;
    private Texture metallicMap = null;
    private Texture normalMap = null;

    /**
     * Apply the current material to a shader.
     *
     * @param shader The shader to apply the material to.
     */
    public void applyTo(Shader shader) {
        shader.setUniform(DIFFUSE_COLOR_SHADER_UNIFORM, this.diffuseColor);
        final boolean hasDiffuseMap = Objects.nonNull(this.diffuseMap);
        shader.setUniform(DIFFUSE_MAP_FLAG_SHADER_UNIFORM, hasDiffuseMap);
        if (hasDiffuseMap) {
            shader.setUniform(DIFFUSE_MAP_SHADER_UNIFORM, DIFFUSE_MAP_CHANNEL);
            this.diffuseMap.bind(DIFFUSE_MAP_CHANNEL);
        }

        shader.setUniform(ROUGHNESS_SHADER_UNIFORM, this.roughness);
        final boolean hasRoughnessMap = Objects.nonNull(this.roughnessMap);
        shader.setUniform(ROUGHNESS_MAP_FLAG_SHADER_UNIFORM, hasRoughnessMap);
        if (hasRoughnessMap) {
            shader.setUniform(ROUGHNESS_MAP_SHADER_UNIFORM, ROUGHNESS_MAP_CHANNEL);
            this.roughnessMap.bind(ROUGHNESS_MAP_CHANNEL);
        }

        shader.setUniform(METALLIC_SHADER_UNIFORM, this.metallic);
        final boolean hasMetallicMap = Objects.nonNull(this.metallicMap);
        shader.setUniform(METALLIC_MAP_FLAG_SHADER_UNIFORM, hasMetallicMap);
        if (hasMetallicMap) {
            shader.setUniform(METALLIC_MAP_SHADER_UNIFORM, METALLIC_MAP_CHANNEL);
            this.metallicMap.bind(METALLIC_MAP_CHANNEL);
        }

        final boolean hasNormalMap = Objects.nonNull(this.normalMap);
        shader.setUniform(NORMAL_MAP_FLAG_SHADER_UNIFORM, hasNormalMap);
        if (hasNormalMap) {
            shader.setUniform(NORMAL_MAP_SHADER_UNIFORM, NORMAL_MAP_CHANNEL);
            this.normalMap.bind(NORMAL_MAP_CHANNEL);
        }
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

    public void setDiffuseColor(Color diffuseColor) {
        this.diffuseColor = diffuseColor;
    }

    public Texture getDiffuseMap() {
        return diffuseMap;
    }

    public void setDiffuseMap(Texture diffuseMap) {
        this.diffuseMap = diffuseMap;
    }

    public float getRoughness() {
        return roughness;
    }

    public void setRoughness(float roughness) {
        this.roughness = roughness;
    }

    public Texture getRoughnessMap() {
        return roughnessMap;
    }

    public void setRoughnessMap(Texture roughnessMap) {
        this.roughnessMap = roughnessMap;
    }

    public float getMetallic() {
        return metallic;
    }

    public void setMetallic(float metallic) {
        this.metallic = metallic;
    }

    public Texture getMetallicMap() {
        return metallicMap;
    }

    public void setMetallicMap(Texture metallicMap) {
        this.metallicMap = metallicMap;
    }

    public Texture getNormalMap() {
        return normalMap;
    }

    public void setNormalMap(Texture normalMap) {
        this.normalMap = normalMap;
    }

}
