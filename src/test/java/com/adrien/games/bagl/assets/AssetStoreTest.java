package com.adrien.games.bagl.assets;

import com.adrien.games.bagl.exception.EngineException;
import com.adrien.games.bagl.extensions.OGLExtension;
import com.adrien.games.bagl.rendering.model.Model;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.utils.ResourcePath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(OGLExtension.class)
class AssetStoreTest {

    private AssetStore assetStore;

    @BeforeEach
    void beforeEach() {
        final var assetDescriptorRepository = createTestAssetDescriptorRepo();
        final var assetFactory = new AssetFactory(null);
        assetStore = new AssetStore(assetDescriptorRepository, assetFactory);
    }

    private AssetDescriptorRepository createTestAssetDescriptorRepo() {
        return new AssetDescriptorRepository(createTestAssetDescriptorMap());
    }

    private Map<String, AssetDescriptor> createTestAssetDescriptorMap() {
        final var assetDescriptor = createTestAssetDescriptor();
        return Collections.singletonMap(assetDescriptor.getId(), assetDescriptor);
    }

    private AssetDescriptor createTestAssetDescriptor() {
        return new AssetDescriptor("test", AssetType.TEXTURE, ResourcePath.get("classpath:/test.png"),
                false, new HashMap<>());
    }

    @Test
    void itShouldLoadAsset() {
        final Texture texture = assetStore.getAsset("test", Texture.class);
        assertNotNull(texture);
    }

    @Test
    void itShouldLoadAssetsOnlyOnce() {
        final Texture texture0 = assetStore.getAsset("test", Texture.class);
        final Texture texture1 = assetStore.getAsset("test", Texture.class);
        assertSame(texture0, texture1);
    }

    @Test
    void itShouldDestroyLoadedAssets() {
        final Texture texture0 = assetStore.getAsset("test", Texture.class);
        assetStore.destroyAssets();
        final Texture texture1 = assetStore.getAsset("test", Texture.class);
        assertNotSame(texture0, texture1);
    }

    @Test
    void itShouldFailToLoadNonExistingAsset() {
        assertThrows(EngineException.class, () -> assetStore.getAsset("unknown", Texture.class));
    }

    @Test
    void itShouldFailToLoadAssetIfTypeDoesNotMatch() {
        assertThrows(EngineException.class, () -> assetStore.getAsset("test", Model.class));
    }

}