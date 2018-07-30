package com.adrien.games.bagl.engine.assets;

import com.adrien.games.bagl.core.Asset;
import com.adrien.games.bagl.core.exception.EngineException;
import com.adrien.games.bagl.core.io.ResourcePath;
import com.adrien.games.bagl.engine.rendering.environment.EnvironmentMapGenerator;
import com.adrien.games.bagl.engine.rendering.model.Model;
import com.adrien.games.bagl.engine.rendering.text.Font;
import com.adrien.games.bagl.engine.resource.scene.ComponentFactory;
import com.adrien.games.bagl.engine.resource.scene.SceneLoader;
import com.adrien.games.bagl.engine.scene.Scene;
import com.adrien.games.bagl.extensions.OGLExtension;
import com.adrien.games.bagl.opengl.texture.Filter;
import com.adrien.games.bagl.opengl.texture.Texture;
import com.adrien.games.bagl.opengl.texture.Wrap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(OGLExtension.class)
class AssetFactoryTest {

    private final AssetFactory assetFactory = new AssetFactory(new SceneLoader(new ComponentFactory(new EnvironmentMapGenerator())));
    private AssetDescriptor assetDescriptor;
    private Asset createdAsset;

    @Test
    void itShouldCreateTexture() {
        givenATextureDescriptor();
        whenCreatingAsset();
        assertTextureIsCreated();
    }

    private void givenATextureDescriptor() {
        final var parameters = new HashMap<String, Object>();
        parameters.put("minFilter", "NEAREST");
        parameters.put("magFilter", "NEAREST");
        parameters.put("sWrap", "CLAMP_TO_EDGE");
        parameters.put("tWrap", "CLAMP_TO_EDGE");
        parameters.put("anisotropic", 8);
        parameters.put("mipmaps", true);
        assetDescriptor = new AssetDescriptor("test", AssetType.TEXTURE, ResourcePath.get("classpath:/test.png"), parameters);
    }

    private void assertTextureIsCreated() {
        assertNotNull(createdAsset);
        assertTrue(createdAsset instanceof Texture);

        final var texture = (Texture) createdAsset;
        assertEquals(1, texture.getWidth());
        assertEquals(1, texture.getHeight());

        final var parameters = texture.getParameters();
        assertEquals(Filter.NEAREST, parameters.getMinFilter());
        assertEquals(Filter.NEAREST, parameters.getMagFilter());
        assertEquals(Wrap.CLAMP_TO_EDGE, parameters.getsWrap());
        assertEquals(Wrap.CLAMP_TO_EDGE, parameters.gettWrap());
        assertEquals(8, parameters.getAnisotropic());
        assertTrue(parameters.getMipmaps());
    }

    @Test
    void itShouldCreateModel() {
        givenAModelDescriptor();
        whenCreatingAsset();
        assertModelIsCreated();
    }

    private void givenAModelDescriptor() {
        assetDescriptor = new AssetDescriptor("test", AssetType.MODEL, ResourcePath.get("classpath:/test.glb"));
    }

    private void assertModelIsCreated() {
        assertNotNull(createdAsset);
        assertTrue(createdAsset instanceof Model);
    }

    @Test
    void itShouldCreateScene() {
        givenASceneDescriptor();
        whenCreatingAsset();
        assertSceneIsCreated();
    }

    private void givenASceneDescriptor() {
        assetDescriptor = new AssetDescriptor("test", AssetType.SCENE, ResourcePath.get("classpath:/test_scene.json"));
    }

    private void assertSceneIsCreated() {
        assertNotNull(createdAsset);
        assertTrue(createdAsset instanceof Scene);
    }

    @Test
    void itShouldCreateFont() {
        givenAFontDescriptor();
        whenCreatingAsset();
        assertFontIsLoaded();
    }

    private void givenAFontDescriptor() {
        assetDescriptor = new AssetDescriptor("test", AssetType.FONT, ResourcePath.get("classpath:/test_font/segoe.fnt"));
    }

    private void assertFontIsLoaded() {
        assertNotNull(createdAsset);
        assertTrue(createdAsset instanceof Font);
    }

    private void whenCreatingAsset() {
        createdAsset = assetFactory.createAsset(assetDescriptor);
    }

    @Test
    void itShouldFailToLoadUnsupportedAssetType() {
        givenAnUnsupportedAssetDescriptor();
        assertThrows(EngineException.class, this::whenCreatingAsset);
    }

    private void givenAnUnsupportedAssetDescriptor() {
        assetDescriptor = new AssetDescriptor("test", null, ResourcePath.get(""));
    }

}