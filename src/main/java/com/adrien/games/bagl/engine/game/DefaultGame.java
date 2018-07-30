package com.adrien.games.bagl.engine.game;

import com.adrien.games.bagl.engine.Configuration;
import com.adrien.games.bagl.engine.assets.AssetDescriptorRepository;
import com.adrien.games.bagl.engine.assets.AssetFactory;
import com.adrien.games.bagl.engine.assets.AssetStore;
import com.adrien.games.bagl.engine.rendering.environment.EnvironmentMapGenerator;
import com.adrien.games.bagl.engine.resource.asset.AssetsDescriptorLoader;
import com.adrien.games.bagl.engine.resource.scene.ComponentFactory;
import com.adrien.games.bagl.engine.resource.scene.SceneLoader;

import java.util.HashMap;

/**
 * Default implementation of {@link Game}.
 * <p>
 * Takes care of initializing the {@link AssetStore}.
 */
public abstract class DefaultGame implements Game {

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
        final var assetDescriptorRepo = getAssetDescriptorRepository();
        final var sceneLoader = new SceneLoader(componentFactory);
        final var assetFactory = new AssetFactory(sceneLoader);
        assetStore = new AssetStore(assetDescriptorRepo, assetFactory);
        componentFactory.setAssetStore(assetStore);
    }

    private AssetDescriptorRepository getAssetDescriptorRepository() {
        final var assetDescriptorFilePath = Configuration.getInstance().getAssetDescriptorFilePath();
        if (assetDescriptorFilePath.exists()) {
            return new AssetsDescriptorLoader().load(assetDescriptorFilePath);
        }
        return new AssetDescriptorRepository(new HashMap<>());
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
