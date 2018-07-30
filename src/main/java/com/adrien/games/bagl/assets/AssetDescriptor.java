package com.adrien.games.bagl.assets;

import com.adrien.games.bagl.utils.ResourcePath;

import java.util.Map;

/**
 * Description of an asset.
 * <p>
 * An asset has an id, a type, a {@link ResourcePath} and a parameters map.
 *
 * @author adrien
 */
public class AssetDescriptor {

    private final String id;
    private final AssetType type;
    private final ResourcePath path;
    private final Map<String, Object> parameters;

    public AssetDescriptor(final String id, final AssetType type, final ResourcePath path) {
        this(id, type, path, null);
    }

    public AssetDescriptor(final String id, final AssetType type, final ResourcePath path, final Map<String, Object> parameters) {
        this.id = id;
        this.type = type;
        this.path = path;
        this.parameters = parameters;
    }


    public String getId() {
        return id;
    }

    public AssetType getType() {
        return type;
    }

    public ResourcePath getPath() {
        return path;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

}
