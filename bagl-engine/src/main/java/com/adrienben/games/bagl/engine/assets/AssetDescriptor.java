package com.adrienben.games.bagl.engine.assets;

import com.adrienben.games.bagl.core.io.ResourcePath;

import java.util.Map;

/**
 * Description of an asset.
 * <p>
 * An asset has an id, a type, a {@link ResourcePath} and a parameters map.
 *
 * @author adrien
 */
public record AssetDescriptor(
        String id,
        AssetType type,
        ResourcePath path,
        Map<String, Object> parameters
) {

    public AssetDescriptor(final String id, final AssetType type, final ResourcePath path) {
        this(id, type, path, null);
    }
}
