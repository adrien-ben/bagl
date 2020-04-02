package com.adrienben.games.bagl.engine.rendering.shaders;

import com.adrienben.games.bagl.core.Color;
import com.adrienben.games.bagl.engine.rendering.model.AlphaMode;
import com.adrienben.games.bagl.opengl.shader.Shader;

/**
 * This class is responsible for setting shader uniforms related to materials.
 *
 * @author adrien
 */
public class MaterialUniformSetter {

    public static final int DIFFUSE_MAP_CHANNEL = 0;
    public static final int EMISSIVE_MAP_CHANNEL = 1;
    public static final int ROUGHNESS_METALLIC_MAP_CHANNEL = 2;
    public static final int NORMAL_MAP_CHANNEL = 3;
    public static final int OCCLUSION_MAP_CHANNEL = 4;

    private static final int ALPHA_MODE_OPAQUE_VALUE = 0;
    private static final int ALPHA_MODE_MASK_VALUE = 1;
    private static final int ALPHA_MODE_BLEND_VALUE = 2;

    private final Shader shader;

    public MaterialUniformSetter(final Shader shader) {
        this.shader = shader;
    }

    public void setDiffuseMapChannelUniform() {
        shader.setUniform("uMaterial.diffuseMap", DIFFUSE_MAP_CHANNEL);
    }

    public void setEmissiveMapChannelUniform() {
        shader.setUniform("uMaterial.emissiveMap", EMISSIVE_MAP_CHANNEL);
    }

    public void setRoughnessMetallicMapChannelUniform() {
        shader.setUniform("uMaterial.roughnessMetallicMap", ROUGHNESS_METALLIC_MAP_CHANNEL);
    }

    public void setNormalMapChannelUniform() {
        shader.setUniform("uMaterial.normalMap", NORMAL_MAP_CHANNEL);
    }

    public void setOcclusionMapChannelUniform() {
        shader.setUniform("uMaterial.occlusionMap", OCCLUSION_MAP_CHANNEL);
    }

    public void setDiffuseColorUniform(final Color diffuseColor) {
        shader.setUniform("uMaterial.diffuseColor", diffuseColor);
    }

    public void setEmissiveColorUniform(final Color emissiveColor) {
        shader.setUniform("uMaterial.emissiveColor", emissiveColor);
    }

    public void setEmissiveIntensityUniform(final float emissiveIntensity) {
        shader.setUniform("uMaterial.emissiveIntensity", emissiveIntensity);
    }

    public void setRoughnessUniform(final float roughness) {
        shader.setUniform("uMaterial.roughness", roughness);
    }

    public void setMetallicUniform(final float metallic) {
        shader.setUniform("uMaterial.metallic", metallic);
    }

    public void setOcclusionStrengthUniform(final float occlusionStrength) {
        shader.setUniform("uMaterial.occlusionStrength", occlusionStrength);
    }

    public void setHasDiffuseMapUniform(final boolean hasDiffuseMap) {
        shader.setUniform("uMaterial.hasDiffuseMap", hasDiffuseMap);
    }

    public void setHasEmissiveMapUniform(final boolean hasEmissiveMap) {
        shader.setUniform("uMaterial.hasEmissiveMap", hasEmissiveMap);
    }

    public void setHasRoughnessMetallicMapUniform(final boolean hasRoughnessMetallicMap) {
        shader.setUniform("uMaterial.hasRoughnessMetallicMap", hasRoughnessMetallicMap);
    }

    public void setHasNormalMapUniform(final boolean hasNormalMap) {
        shader.setUniform("uMaterial.hasNormalMap", hasNormalMap);
    }

    public void setHasOcclusionMapUniform(final boolean hasOcclusionMap) {
        shader.setUniform("uMaterial.hasOcclusionMap", hasOcclusionMap);
    }

    public void setAlphaMode(final AlphaMode alphaMode) {
        shader.setUniform("uMaterial.alphaMode", getAlphaModeValue(alphaMode));
    }

    private int getAlphaModeValue(final AlphaMode alphaMode) {
        return switch (alphaMode) {
            case OPAQUE -> ALPHA_MODE_OPAQUE_VALUE;
            case MASK -> ALPHA_MODE_MASK_VALUE;
            case BLEND -> ALPHA_MODE_BLEND_VALUE;
        };
    }

    public void setAlphaCutoffUniform(final float alphaCutoff) {
        shader.setUniform("uMaterial.alphaCutoff", alphaCutoff);
    }
}
