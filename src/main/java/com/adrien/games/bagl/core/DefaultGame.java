package com.adrien.games.bagl.core;

import com.adrien.games.bagl.assets.AssetFactory;
import com.adrien.games.bagl.assets.AssetStore;
import com.adrien.games.bagl.resource.asset.AssetsDescriptorLoader;
import com.adrien.games.bagl.resource.scene.SceneLoader;
import com.adrien.games.bagl.utils.ResourcePath;

/**
 * Default implementation of {@link Game}.
 * <p>
 * Takes care of initializing the {@link AssetStore}.
 */
public abstract class DefaultGame implements Game {

    private static final String DEFAULT_ASSETS_DESCRIPTOR_PATH = "classpath:/assets.json";

    private AssetStore assetStore;

    /**
     * Initialize asset store.
     * <p>
     * Whe overriding this method, call {@code super.init()} first.
     */
    @Override
    public void init() {
        initAssetStore();
    }

    private void initAssetStore() {
        final var assetDescriptorRepo = new AssetsDescriptorLoader().load(ResourcePath.get(DEFAULT_ASSETS_DESCRIPTOR_PATH));
        final var assetFactory = new AssetFactory(new SceneLoader());
        assetStore = new AssetStore(assetDescriptorRepo, assetFactory);
    }

    /**
     * Destroy asset store.
     * <p>
     * Whe overriding this method, call {@code super.destroy()} first.
     */
    @Override
    public void destroy() {
        assetStore.destroyAssets();
    }

    protected AssetStore getAssetStore() {
        return assetStore;
    }
}
