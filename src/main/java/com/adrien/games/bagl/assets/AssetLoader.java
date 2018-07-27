package com.adrien.games.bagl.assets;

import com.adrien.games.bagl.exception.EngineException;
import com.adrien.games.bagl.utils.repository.DefaultRepository;
import com.adrien.games.bagl.utils.repository.Repository;

/**
 * Load {@link Asset}s declared in a {@link AssetDescriptorRepository}.
 *
 * @author adrien
 */
public class AssetLoader {

    private final AssetDescriptorRepository assetDescriptorRepository;
    private final AssetFactory assetFactory;
    private final Repository<String, Asset> assetRepository;

    public AssetLoader(final AssetDescriptorRepository assetDescriptorRepository, final AssetFactory assetFactory) {
        this.assetDescriptorRepository = assetDescriptorRepository;
        this.assetFactory = assetFactory;
        this.assetRepository = new DefaultRepository<>();
    }

    /**
     * Load an asset by its id and cast it into the requested type.
     * <p>
     * Loaded assets are stored internally and when requested again, the stored
     * asset is returned.
     * <p>
     * The method will throw {@link EngineException} if the asset does not exist
     * or if the requested type is not compatible with the actual type of the asset.
     */
    public <T extends Asset> T load(final String id, final Class<T> assetClass) {
        final var asset = assetRepository.getById(id).orElseGet(() -> loadAndStoreAsset(id));
        return castAsset(id, asset, assetClass);
    }

    private Asset loadAndStoreAsset(final String id) {
        final var assetDescriptor = getAssetDescriptorById(id);
        final Asset asset = assetFactory.createAsset(assetDescriptor);
        assetRepository.put(id, asset);
        return asset;
    }

    private AssetDescriptor getAssetDescriptorById(final String id) {
        return assetDescriptorRepository.getById(id)
                .orElseThrow(() -> new EngineException("Asset with id " + id + "does not exist"));
    }

    private <T extends Asset> T castAsset(final String id, final Asset asset, final Class<T> targetClass) {
        if (!targetClass.isInstance(asset)) {
            throw new EngineException("Asset " + id + " has class " + asset.getClass() + " not " + targetClass);
        }
        return targetClass.cast(asset);
    }

    /**
     * Destroy all loaded assets.
     */
    public void destroyAssets() {
        assetRepository.getAll().forEach(Asset::destroy);
        assetRepository.clear();
    }

}
