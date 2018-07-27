package com.adrien.games.bagl.assets;

import com.adrien.games.bagl.extensions.OGLExtension;
import com.adrien.games.bagl.rendering.model.Model;
import com.adrien.games.bagl.rendering.texture.Filter;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.Wrap;
import com.adrien.games.bagl.utils.ResourcePath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(OGLExtension.class)
class AssetFactoryTest {

    private final AssetFactory assetFactory = new AssetFactory();
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
        assetDescriptor = new AssetDescriptor("test", "texture", ResourcePath.get("classpath:/test.png"), false, parameters);
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
        assertIsModelIsCreated();
    }

    private void givenAModelDescriptor() {
        assetDescriptor = new AssetDescriptor("test", "model", ResourcePath.get("classpath:/test.glb"), false);
    }

    private void assertIsModelIsCreated() {
        assertNotNull(createdAsset);
        assertTrue(createdAsset instanceof Model);
    }

    private void whenCreatingAsset() {
        createdAsset = assetFactory.createAsset(assetDescriptor);
    }

}