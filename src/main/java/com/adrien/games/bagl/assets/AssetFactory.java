package com.adrien.games.bagl.assets;

import com.adrien.games.bagl.exception.EngineException;
import com.adrien.games.bagl.rendering.model.Model;
import com.adrien.games.bagl.rendering.model.ModelFactory;
import com.adrien.games.bagl.rendering.texture.Filter;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureParameters;
import com.adrien.games.bagl.rendering.texture.Wrap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class AssetFactory {

    private final Map<String, Function<AssetDescriptor, Asset>> assetCreationCommands;

    public AssetFactory() {
        assetCreationCommands = new HashMap<>();
        initAssetCreationCommands();
    }

    private void initAssetCreationCommands() {
        assetCreationCommands.put("texture", this::createTexture);
        assetCreationCommands.put("model", this::createModel);
    }

    public Asset createAsset(final AssetDescriptor assetDescriptor) {
        final var type = assetDescriptor.getType();
        final var assetCreationCommand = assetCreationCommands.get(type);
        if (Objects.isNull(assetCreationCommand)) {
            throw new EngineException("Unsupported asset type " + type);
        }
        return assetCreationCommand.apply(assetDescriptor);
    }

    private Texture createTexture(final AssetDescriptor assetDescriptor) {
        final var textureParametersBuilder = createTextureParametersBuilder(assetDescriptor.getParameters());
        return Texture.fromFile(assetDescriptor.getPath(), textureParametersBuilder);
    }

    private TextureParameters.Builder createTextureParametersBuilder(final Map<String, Object> parametersMap) {
        final var builder = TextureParameters.builder();
        mapIfNotNull(parametersMap.get("minFilter"), String.class, Filter::valueOf).ifPresent(builder::minFilter);
        mapIfNotNull(parametersMap.get("magFilter"), String.class, Filter::valueOf).ifPresent(builder::magFilter);
        mapIfNotNull(parametersMap.get("sWrap"), String.class, Wrap::valueOf).ifPresent(builder::sWrap);
        mapIfNotNull(parametersMap.get("tWrap"), String.class, Wrap::valueOf).ifPresent(builder::tWrap);
        mapIfNotNull(parametersMap.get("anisotropic"), Integer.class, Function.identity()).ifPresent(builder::anisotropic);
        mapIfNotNull(parametersMap.get("mipmaps"), Boolean.class, Function.identity()).ifPresent(builder::mipmaps);
        return builder;
    }

    private <T, S> Optional<T> mapIfNotNull(final Object toMap, final Class<S> actualType, final Function<S, T> mapper) {
        if (Objects.isNull(toMap)) {
            return Optional.empty();
        }
        final var mapped = mapper.apply(actualType.cast(toMap));
        return Optional.of(mapped);
    }

    public Model createModel(final AssetDescriptor assetDescriptor) {
        return ModelFactory.fromFile(assetDescriptor.getPath());
    }

}
