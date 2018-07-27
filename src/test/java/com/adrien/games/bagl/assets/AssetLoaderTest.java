package com.adrien.games.bagl.assets;

import com.adrien.games.bagl.exception.EngineException;
import com.adrien.games.bagl.extensions.OGLExtension;
import com.adrien.games.bagl.rendering.model.Model;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.utils.ResourcePath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(OGLExtension.class)
class AssetLoaderTest {

    private final AssetLoader assetLoader;

    AssetLoaderTest() {
        final var assetDescriptorRepository = createTestAssetDescriptorRepo();
        final var assetFactory = new AssetFactory(null);
        assetLoader = new AssetLoader(assetDescriptorRepository, assetFactory);
    }

    private AssetDescriptorRepository createTestAssetDescriptorRepo() {
        return new AssetDescriptorRepository(createTestAssetDescriptorMap());
    }

    private Map<String, AssetDescriptor> createTestAssetDescriptorMap() {
        final var assetDescriptor = createTestAssetDescriptor();
        return Collections.singletonMap(assetDescriptor.getId(), assetDescriptor);
    }

    private AssetDescriptor createTestAssetDescriptor() {
        return new AssetDescriptor("test", "texture", ResourcePath.get("classpath:/test.png"),
                false, new HashMap<>());
    }

    @Test
    void itShouldLoadAsset() {
        final Texture texture = assetLoader.load("test", Texture.class);
        assertNotNull(texture);
    }

    @Test
    void itShouldLoadAssetsOnlyOnce() {
        final Texture texture0 = assetLoader.load("test", Texture.class);
        final Texture texture1 = assetLoader.load("test", Texture.class);
        assertSame(texture0, texture1);
    }

    @Test
    void itShouldFailToLoadNonExistingAsset() {
        assertThrows(EngineException.class, () -> assetLoader.load("unknown", Texture.class));
    }

    @Test
    void itShouldFailToLoadAssetIfTypeDoesNotMatch() {
        assertThrows(EngineException.class, () -> assetLoader.load("test", Model.class));
    }

}