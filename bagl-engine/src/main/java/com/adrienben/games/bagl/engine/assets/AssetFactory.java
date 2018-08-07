package com.adrienben.games.bagl.engine.assets;

import com.adrienben.games.bagl.core.Asset;
import com.adrienben.games.bagl.core.exception.EngineException;
import com.adrienben.games.bagl.engine.rendering.model.Model;
import com.adrienben.games.bagl.engine.rendering.model.ModelFactory;
import com.adrienben.games.bagl.engine.rendering.text.Font;
import com.adrienben.games.bagl.engine.resource.scene.SceneLoader;
import com.adrienben.games.bagl.engine.scene.Scene;
import com.adrienben.games.bagl.opengl.texture.Filter;
import com.adrienben.games.bagl.opengl.texture.Texture;
import com.adrienben.games.bagl.opengl.texture.TextureParameters;
import com.adrienben.games.bagl.opengl.texture.Wrap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class AssetFactory {

    private final Map<AssetType, Function<AssetDescriptor, Asset>> assetCreationCommands;
    private final SceneLoader sceneLoader;

    public AssetFactory(final SceneLoader sceneLoader) {
        this.assetCreationCommands = new HashMap<>();
        this.sceneLoader = sceneLoader;
        initAssetCreationCommands();
    }

    private void initAssetCreationCommands() {
        assetCreationCommands.put(AssetType.TEXTURE, this::createTexture);
        assetCreationCommands.put(AssetType.MODEL, this::createModel);
        assetCreationCommands.put(AssetType.SCENE, this::createScene);
        assetCreationCommands.put(AssetType.FONT, this::createFont);
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
        if (Objects.nonNull(parametersMap)) {
            mapIfNotNull(parametersMap.get("minFilter"), String.class, Filter::valueOf).ifPresent(builder::minFilter);
            mapIfNotNull(parametersMap.get("magFilter"), String.class, Filter::valueOf).ifPresent(builder::magFilter);
            mapIfNotNull(parametersMap.get("sWrap"), String.class, Wrap::valueOf).ifPresent(builder::sWrap);
            mapIfNotNull(parametersMap.get("tWrap"), String.class, Wrap::valueOf).ifPresent(builder::tWrap);
            mapIfNotNull(parametersMap.get("anisotropic"), Integer.class, Function.identity()).ifPresent(builder::anisotropic);
            mapIfNotNull(parametersMap.get("mipmaps"), Boolean.class, Function.identity()).ifPresent(builder::mipmaps);
        }
        return builder;
    }

    private <T, S> Optional<T> mapIfNotNull(final Object toMap, final Class<S> actualType, final Function<S, T> mapper) {
        if (Objects.isNull(toMap)) {
            return Optional.empty();
        }
        final var mapped = mapper.apply(actualType.cast(toMap));
        return Optional.of(mapped);
    }

    private Model createModel(final AssetDescriptor assetDescriptor) {
        return ModelFactory.fromFile(assetDescriptor.getPath());
    }

    private Scene createScene(final AssetDescriptor assetDescriptor) {
        return sceneLoader.load(assetDescriptor.getPath());
    }

    private Font createFont(final AssetDescriptor assetDescriptor) {
        return new Font(assetDescriptor.getPath());
    }

}
