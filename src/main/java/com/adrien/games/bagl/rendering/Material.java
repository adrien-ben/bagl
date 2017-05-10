package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.rendering.texture.Texture;

import java.util.Objects;

public class Material {

    private static final String DIFFUSE_COLOR_SHADER_UNIFORM = "uMaterial.diffuseColor";
    private static final String DIFFUSE_MAP_SHADER_UNIFORM = "uMaterial.diffuseMap";
    private static final String DIFFUSE_MAP_FLAG_SHADER_UNIFORM = "uMaterial.hasDiffuseMap";
    private static final String SPECULAR_MAP_SHADER_UNIFORM = "uMaterial.specularMap";
    private static final String SPECULAR_MAP_FLAG_SHADER_UNIFORM = "uMaterial.hasSpecularMap";
    private static final String NORMAL_MAP_SHADER_UNIFORM = "uMaterial.bumpMap";
    private static final String NORMAL_MAP_FLAG_SHADER_UNIFORM = "uMaterial.hasBumpMap";
    private static final String SHININESS_SHADER_UNIFORM = "uMaterial.shininess";
    private static final String GLOSSINESS_SHADER_UNIFORM = "uMaterial.glossiness";

    private static final int DIFFUSE_MAP_CHANNEL = 0;
    private static final int SPECULAR_MAP_CHANNEL = 1;
    private static final int NORMAL_MAP_CHANNEL = 2;

    private Color diffuseColor = Color.WHITE;
    private Texture diffuseMap = null;
    private float specularExponent = 0.0f;
    private float specularIntensity = 0.0f;
    private Texture specularMap = null;
    private Texture bumpMap = null;

    /**
     * Apply the current material to a shader.
     * @param shader The shader to apply the material to.
     */
    public void applyTo(Shader shader) {
        if(this.hasDiffuseMap()) {
            shader.setUniform(DIFFUSE_MAP_SHADER_UNIFORM, DIFFUSE_MAP_CHANNEL);
            this.diffuseMap.bind(DIFFUSE_MAP_CHANNEL);
        }
        if(this.hasSpecularMap()) {
            shader.setUniform(SPECULAR_MAP_SHADER_UNIFORM, SPECULAR_MAP_CHANNEL);
            this.specularMap.bind(SPECULAR_MAP_CHANNEL);
        }
        if(this.hasBumpMap()) {
            shader.setUniform(NORMAL_MAP_SHADER_UNIFORM, NORMAL_MAP_CHANNEL);
            this.bumpMap.bind(NORMAL_MAP_CHANNEL);
        }
        shader.setUniform(DIFFUSE_COLOR_SHADER_UNIFORM, this.diffuseColor);
        shader.setUniform(DIFFUSE_MAP_FLAG_SHADER_UNIFORM, this.hasDiffuseMap());
        shader.setUniform(SHININESS_SHADER_UNIFORM, this.specularIntensity);
        shader.setUniform(SPECULAR_MAP_FLAG_SHADER_UNIFORM, this.hasSpecularMap());
        shader.setUniform(GLOSSINESS_SHADER_UNIFORM, this.specularExponent);
        shader.setUniform(NORMAL_MAP_FLAG_SHADER_UNIFORM, this.hasBumpMap());
    }

    public void destroy() {
        if(Objects.nonNull(diffuseMap)) {
            this.diffuseMap.destroy();
        }
    }

    public boolean hasDiffuseMap() {
        return Objects.nonNull(this.diffuseMap);
    }

    public boolean hasSpecularMap() {
        return Objects.nonNull(this.specularMap);
    }

    public boolean hasBumpMap() {
        return Objects.nonNull(this.bumpMap);
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

    public float getSpecularExponent() {
        return specularExponent;
    }

    public void setSpecularExponent(float specularExponent) {
        this.specularExponent = specularExponent;
    }

    public float getSpecularIntensity() {
        return specularIntensity;
    }

    public void setSpecularIntensity(float specularIntensity) {
        this.specularIntensity = specularIntensity;
    }

    public Texture getSpecularMap() {
        return specularMap;
    }

    public void setSpecularMap(Texture specularMap) {
        this.specularMap = specularMap;
    }

    public Texture getBumpMap() {
        return bumpMap;
    }

    public void setBumpMap(Texture bumpMap) {
        this.bumpMap = bumpMap;
    }

}
