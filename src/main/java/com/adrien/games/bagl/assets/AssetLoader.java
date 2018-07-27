package com.adrien.games.bagl.assets;

import com.adrien.games.bagl.exception.EngineException;

/**
 * Load {@link Asset}s declared in a {@link AssetDescriptorRepository}.
 *
 * @author adrien
 */
public class AssetLoader {

    private final AssetDescriptorRepository assetDescriptorRepository;
    private final AssetFactory assetFactory;

    public AssetLoader(final AssetDescriptorRepository assetDescriptorRepository, final AssetFactory assetFactory) {
        this.assetDescriptorRepository = assetDescriptorRepository;
        this.assetFactory = assetFactory;
    }

    /**
     * Load an asset by its id and cast it into the requested type.
     * <p>
     * The method will throw {@link EngineException} if the asset does not exist
     * or if the requested type is not compatible with the actual type of the asset.
     */
    public <T extends Asset> T load(final String id, final Class<T> assetClass) {
        final var assetDescriptor = getAssetDescriptorById(id);
        return createAndCastAsset(id, assetDescriptor, assetClass);
    }

    private AssetDescriptor getAssetDescriptorById(final String id) {
        return assetDescriptorRepository.getById(id)
                .orElseThrow(() -> new EngineException("Asset with id " + id + "does not exist"));
    }

    private <T extends Asset> T createAndCastAsset(
            final String id,
            final AssetDescriptor assetDescriptor,
            final Class<T> targetClass
    ) {
        final var asset = assetFactory.createAsset(assetDescriptor);
        if (!targetClass.isInstance(asset)) {
            throw new EngineException("Asset " + id + " has class " + asset.getClass() + " not " + targetClass);
        }
        return targetClass.cast(asset);
    }
}
