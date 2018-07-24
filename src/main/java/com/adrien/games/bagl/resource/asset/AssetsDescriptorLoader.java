package com.adrien.games.bagl.resource.asset;

import com.adrien.games.bagl.assets.AssetDescriptor;
import com.adrien.games.bagl.assets.AssetDescriptorRepository;
import com.adrien.games.bagl.resource.asset.json.AssetDescriptorJson;
import com.adrien.games.bagl.resource.asset.json.AssetsDescriptorJson;
import com.adrien.games.bagl.utils.ResourcePath;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.adrien.games.bagl.utils.AssertUtils.validate;

/**
 * Load asset descriptor file/
 *
 * @author adrien
 */
public class AssetsDescriptorLoader {

    /**
     * Load the asset descriptor file at {@code resourcePath}.
     */
    public AssetDescriptorRepository load(final ResourcePath resourcePath) {
        final var gson = new Gson();
        final var assetsDescriptorJson = gson.fromJson(new InputStreamReader(resourcePath.openInputStream()), AssetsDescriptorJson.class);
        return mapAssetsDescriptors(assetsDescriptorJson);
    }

    private AssetDescriptorRepository mapAssetsDescriptors(final AssetsDescriptorJson assetsDescriptorJson) {
        final var assetDescriptors = assetsDescriptorJson.getAssets().stream().collect(Collectors.toMap(AssetDescriptorJson::getId, this::mapAssetDescriptor));
        return new AssetDescriptorRepository(assetDescriptors);
    }

    private AssetDescriptor mapAssetDescriptor(final AssetDescriptorJson assetDescriptorJson) {
        final var id = checkAssetDescriptorMissingField(assetDescriptorJson.getId(), "id");
        final var type = checkAssetDescriptorMissingField(assetDescriptorJson.getType(), "type");
        final var path = checkAssetDescriptorMissingField(assetDescriptorJson.getPath(), "path");
        final var isLazyLoading = assetDescriptorJson.isLazyLoading();
        final var parameters = assetDescriptorJson.getParameters();

        return new AssetDescriptor(id, type, ResourcePath.get(path), isLazyLoading, parameters);
    }

    private <T> T checkAssetDescriptorMissingField(final T field, final String fieldName) {
        return validate(field, Objects::nonNull, "Asset descriptor should have a " + fieldName + " field");
    }

}
