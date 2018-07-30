package com.adrien.games.bagl.engine.resource.asset;

import com.adrien.games.bagl.core.exception.EngineException;
import com.adrien.games.bagl.core.io.ResourcePath;
import com.adrien.games.bagl.engine.assets.AssetType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class AssetDescriptorLoaderTest {

    private AssetsDescriptorLoader assetsDescriptorLoader = new AssetsDescriptorLoader();

    @Test
    public void itShouldLoadAssetsDescriptor() {
        final var assetsDescriptor = assetsDescriptorLoader.load(ResourcePath.get("classpath:/assets_descriptor/test_assets.json"));
        final var testAssetDescriptor = assetsDescriptor.getById("test_asset").orElseThrow();

        assertNotNull(assetsDescriptor);
        assertNotNull(testAssetDescriptor);
        assertEquals("test_asset", testAssetDescriptor.getId());
        assertEquals(AssetType.TEXTURE, testAssetDescriptor.getType());
        assertEquals(Paths.get(getTestFilePath()).toAbsolutePath().toString(), testAssetDescriptor.getPath().getAbsolutePath());
        assertEquals("NEAREST", testAssetDescriptor.getParameters().get("minFilter"));
    }

    @Test
    public void itShouldFailToLoadNotExistingFile() {
        assertThrows(EngineException.class, () -> assetsDescriptorLoader.load(ResourcePath.get("classpath:/not_test_assets.json")));
    }

    private String getTestFilePath() {
        return getExecutionPath() + File.separator + "target/test-classes/test.png";
    }

    private String getExecutionPath() {
        return System.getProperty("user.dir");
    }

}