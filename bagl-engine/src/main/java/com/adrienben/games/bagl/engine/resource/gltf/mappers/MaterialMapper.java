package com.adrienben.games.bagl.engine.resource.gltf.mappers;

import com.adrienben.games.bagl.core.Color;
import com.adrienben.games.bagl.engine.rendering.Material;
import com.adrienben.games.bagl.opengl.texture.Texture2D;
import com.adrienben.tools.gltf.models.*;

import java.util.List;
import java.util.Optional;

/**
 * Map {@link GltfMaterial} into {@link Material}.
 *
 * @author adrien
 */
public class MaterialMapper {

    private static final float DEFAULT_EMISSIVE_INTENSITY = 1f;

    private final AlphaModeMapper alphaModeMapper = new AlphaModeMapper();

    private List<Texture2D> textureIndex;

    /**
     * Map {@code gltfMaterial} into a {@link Material}.
     * <p>
     * Textures are picked up from an index and not created here because some texture might be shared by several material.
     *
     * @param gltfMaterial The material to map.
     * @param textureIndex The asset texture index.
     * @return A new {@link Material}.
     */
    public Material map(final GltfMaterial gltfMaterial, final List<Texture2D> textureIndex) {
        this.textureIndex = textureIndex;
        final var color = gltfMaterial.getPbrMetallicRoughness().getBaseColorFactor();
        final var emissive = gltfMaterial.getEmissiveFactor();
        final var diffuseTexture = Optional.ofNullable(gltfMaterial.getPbrMetallicRoughness().getBaseColorTexture())
                .map(GltfTextureInfo::getTexture).map(this::getTexture).orElse(null);
        final var emissiveTexture = Optional.ofNullable(gltfMaterial.getEmissiveTexture())
                .map(GltfTextureInfo::getTexture).map(this::getTexture).orElse(null);
        final var roughnessMetallicMap = Optional.ofNullable(gltfMaterial.getPbrMetallicRoughness().getMetallicRoughnessTexture())
                .map(GltfTextureInfo::getTexture).map(this::getTexture).orElse(null);
        final var normalMap = Optional.ofNullable(gltfMaterial.getNormalTexture()).map(GltfNormalTextureInfo::getTexture)
                .map(this::getTexture).orElse(null);
        final var alphaMode = alphaModeMapper.map(gltfMaterial.getAlphaMode());
        final var alphaCutoff = gltfMaterial.getAlphaCutoff();

        final Material.Builder builder = Material.builder()
                .diffuse(mapColor(color))
                .emissive(mapColor(emissive))
                .emissiveIntensity(DEFAULT_EMISSIVE_INTENSITY)
                .roughness(gltfMaterial.getPbrMetallicRoughness().getRoughnessFactor())
                .metallic(gltfMaterial.getPbrMetallicRoughness().getMetallicFactor())
                .diffuse(diffuseTexture)
                .emissive(emissiveTexture)
                .roughnessMetallic(roughnessMetallicMap)
                .normals(normalMap)
                .doubleSided(gltfMaterial.getDoubleSided())
                .alphaMode(alphaMode)
                .alphaCutoff(alphaCutoff);

        Optional.ofNullable(gltfMaterial.getOcclusionTexture()).ifPresent(gltfOcclusion -> {
            builder.occlusionStrength(gltfOcclusion.getStrength());
            builder.occlusion(getTexture(gltfOcclusion.getTexture()));
        });

        return builder.build();
    }

    private Color mapColor(final GltfColor color) {
        return new Color(color.getR(), color.getG(), color.getB(), color.getA());
    }

    private Texture2D getTexture(final GltfTexture texture) {
        return textureIndex.get(texture.getIndex());
    }
}
