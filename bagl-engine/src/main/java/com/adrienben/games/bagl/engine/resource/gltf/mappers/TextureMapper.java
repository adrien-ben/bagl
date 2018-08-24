package com.adrienben.games.bagl.engine.resource.gltf.mappers;

import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.engine.Configuration;
import com.adrienben.games.bagl.opengl.texture.Filter;
import com.adrienben.games.bagl.opengl.texture.Texture2D;
import com.adrienben.games.bagl.opengl.texture.TextureParameters;
import com.adrienben.tools.gltf.models.GltfBufferView;
import com.adrienben.tools.gltf.models.GltfImage;
import com.adrienben.tools.gltf.models.GltfSampler;
import com.adrienben.tools.gltf.models.GltfTexture;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;

/**
 * Map {@link GltfTexture} into {@link Texture2D}.
 *
 * @author adrien
 */
public class TextureMapper {

    private final FilterMapper filterMapper = new FilterMapper();
    private final WrapMapper wrapMapper = new WrapMapper();

    private String assetDirectory;

    /**
     * Map {@code gltfTexture} into a {@link Texture2D}.
     *
     * @param gltfTexture    The texture to map.
     * @param assetDirectory The root directory containing the asset to where to load images from.
     * @return A new {@link Texture2D}.
     */
    public Texture2D map(final GltfTexture gltfTexture, final String assetDirectory) {
        this.assetDirectory = assetDirectory;
        if (Objects.isNull(gltfTexture) || Objects.isNull(gltfTexture.getSource())) {
            return null;
        }
        final var params = mapTextureParameters(gltfTexture.getSampler());
        return generateTextureFromImage(gltfTexture.getSource(), params);
    }

    private TextureParameters.Builder mapTextureParameters(final GltfSampler sampler) {
        final var magFilter = Optional.ofNullable(sampler.getMagFilter()).map(filterMapper::map);
        final var minFilter = Optional.ofNullable(sampler.getMinFilter()).map(filterMapper::map);
        final var isMipmap = magFilter.map(Filter::isMipmap).orElse(false)
                || minFilter.map(Filter::isMipmap).orElse(false);

        final var params = TextureParameters.builder();
        magFilter.ifPresent(params::magFilter);
        minFilter.ifPresent(params::minFilter);
        params.mipmaps(isMipmap);
        params.sWrap(wrapMapper.map(sampler.getWrapS()));
        params.tWrap(wrapMapper.map(sampler.getWrapT()));
        params.anisotropic(Configuration.getInstance().getAnisotropicLevel());
        return params;
    }

    /**
     * Generate a texture from a {@link GltfImage}
     *
     * @param gltfImage The gltf image from which to generate the texture
     * @param params    The parameters of the texture
     * @return A new texture
     */
    private Texture2D generateTextureFromImage(final GltfImage gltfImage, final TextureParameters.Builder params) {
        if (Objects.nonNull(gltfImage.getBufferView())) {
            return generateTextureFromBufferView(gltfImage.getBufferView(), params);
        } else if (Objects.nonNull(gltfImage.getData())) {
            return generateTextureFromImageData(gltfImage.getData(), params);
        }
        return generateTextureFromFile(gltfImage.getUri(), params);
    }

    private Texture2D generateTextureFromBufferView(final GltfBufferView bufferView, final TextureParameters.Builder params) {
        final var length = bufferView.getByteLength();
        final var imageData = MemoryUtil.memAlloc(length)
                .put(bufferView.getBuffer().getData(), bufferView.getByteOffset(), length).flip();
        return generateTextureFromByteBufferAndFreeBuffer(imageData, params);
    }

    private Texture2D generateTextureFromImageData(final byte[] data, final TextureParameters.Builder params) {
        final var imageData = MemoryUtil.memAlloc(data.length).put(data).flip();
        return generateTextureFromByteBufferAndFreeBuffer(imageData, params);
    }

    private Texture2D generateTextureFromByteBufferAndFreeBuffer(final ByteBuffer byteBuffer, final TextureParameters.Builder params) {
        final var texture = Texture2D.fromMemory(byteBuffer, params);
        MemoryUtil.memFree(byteBuffer);
        return texture;
    }

    private Texture2D generateTextureFromFile(final String path, final TextureParameters.Builder params) {
        return Texture2D.fromFile(ResourcePath.get(assetDirectory, path), params);
    }

}
