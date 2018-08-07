package com.adrienben.games.bagl.engine.assets;

import com.adrienben.games.bagl.core.Asset;
import com.adrienben.games.bagl.core.exception.EngineException;
import com.adrienben.games.bagl.core.utils.repository.DefaultRepository;
import com.adrienben.games.bagl.core.utils.repository.Repository;

/**
 * Asset store.
 * <p>
 * Allow retrieval of {@link Asset} based on an {@link AssetDescriptorRepository}.
 * Assets are loaded when first requested. Other requests will serve the same asset.
 *
 * @author adrien
 */
public class AssetStore {

    private final AssetDescriptorRepository assetDescriptorRepository;
    private final AssetFactory assetFactory;
    private final Repository<String, Asset> assetRepository;

    public AssetStore(final AssetDescriptorRepository assetDescriptorRepository, final AssetFactory assetFactory) {
        this.assetDescriptorRepository = assetDescriptorRepository;
        this.assetFactory = assetFactory;
        this.assetRepository = new DefaultRepository<>();
    }

    /**
     * Get an asset by its id and cast it into the requested type.
     * <p>
     * Assets are loaded at the first request then stored internally and when
     * requested again, the stored asset is returned.
     * <p>
     * The method will throw {@link EngineException} if the asset does not exist
     * or if the requested type is not compatible with the actual type of the asset.
     */
    public <T extends Asset> T getAsset(final String id, final Class<T> assetClass) {
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
        try {
            return targetClass.cast(asset);
        } catch (final ClassCastException exception) {
            throw new EngineException("Asset " + id + " has class " + asset.getClass() + " not " + targetClass, exception);
        }
    }

    /**
     * Destroy all loaded assets.
     */
    public void destroyAssets() {
        assetRepository.getAll().forEach(Asset::destroy);
        assetRepository.clear();
    }

}
