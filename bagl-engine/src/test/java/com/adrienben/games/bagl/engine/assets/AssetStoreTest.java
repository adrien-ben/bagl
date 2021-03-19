package com.adrienben.games.bagl.engine.assets;

import com.adrienben.games.bagl.core.exception.EngineException;
import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.engine.rendering.model.Model;
import com.adrienben.games.bagl.opengl.texture.Texture2D;
import com.adrienben.games.bagl.tests.OGLExtension;
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
        return Collections.singletonMap(assetDescriptor.id(), assetDescriptor);
    }

    private AssetDescriptor createTestAssetDescriptor() {
        return new AssetDescriptor("test", AssetType.TEXTURE, ResourcePath.get("classpath:/test.png"), new HashMap<>());
    }

    @Test
    void itShouldLoadAsset() {
        final Texture2D texture = assetStore.getAsset("test", Texture2D.class);
        assertNotNull(texture);
    }

    @Test
    void itShouldLoadAssetsOnlyOnce() {
        final Texture2D texture0 = assetStore.getAsset("test", Texture2D.class);
        final Texture2D texture1 = assetStore.getAsset("test", Texture2D.class);
        assertSame(texture0, texture1);
    }

    @Test
    void itShouldDestroyLoadedAssets() {
        final Texture2D texture0 = assetStore.getAsset("test", Texture2D.class);
        assetStore.destroyAssets();
        final Texture2D texture1 = assetStore.getAsset("test", Texture2D.class);
        assertNotSame(texture0, texture1);
    }

    @Test
    void itShouldFailToLoadNonExistingAsset() {
        assertThrows(EngineException.class, () -> assetStore.getAsset("unknown", Texture2D.class));
    }

    @Test
    void itShouldFailToLoadAssetIfTypeDoesNotMatch() {
        assertThrows(EngineException.class, () -> assetStore.getAsset("test", Model.class));
    }

}