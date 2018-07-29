package com.adrien.games.bagl.core;

import com.adrien.games.bagl.assets.AssetFactory;
import com.adrien.games.bagl.assets.AssetStore;
import com.adrien.games.bagl.rendering.environment.EnvironmentMapGenerator;
import com.adrien.games.bagl.resource.asset.AssetsDescriptorLoader;
import com.adrien.games.bagl.resource.scene.ComponentFactory;
import com.adrien.games.bagl.resource.scene.SceneLoader;
import com.adrien.games.bagl.utils.ResourcePath;

/**
 * Default implementation of {@link Game}.
 * <p>
 * Takes care of initializing the {@link AssetStore}.
 */
public abstract class DefaultGame implements Game {

    private static final String DEFAULT_ASSETS_DESCRIPTOR_PATH = "classpath:/assets.json";

    private EnvironmentMapGenerator environmentMapGenerator;
    private ComponentFactory componentFactory;
    private AssetStore assetStore;

    /**
     * Initialize asset store.
     * <p>
     * Whe overriding this method, call {@code super.init()} first.
     */
    @Override
    public void init() {
        environmentMapGenerator = new EnvironmentMapGenerator();
        componentFactory = new ComponentFactory(environmentMapGenerator);
        initAssetStore();
    }

    private void initAssetStore() {
        final var assetDescriptorRepo = new AssetsDescriptorLoader().load(ResourcePath.get(DEFAULT_ASSETS_DESCRIPTOR_PATH));
        final var sceneLoader = new SceneLoader(componentFactory);
        final var assetFactory = new AssetFactory(sceneLoader);
        assetStore = new AssetStore(assetDescriptorRepo, assetFactory);
        componentFactory.setAssetStore(assetStore);
    }

    /**
     * Destroy asset store.
     * <p>
     * Whe overriding this method, call {@code super.destroy()} first.
     */
    @Override
    public void destroy() {
        assetStore.destroyAssets();
        environmentMapGenerator.destroy();
    }

    protected AssetStore getAssetStore() {
        return assetStore;
    }
}