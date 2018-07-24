package com.adrien.games.bagl.assets;

import com.adrien.games.bagl.utils.ResourcePath;

import java.util.Map;

/**
 * Description of an asset.
 * <p>
 * An asset has an id, a type, a {@link ResourcePath}, a flag to say whether it is loaded at startup or
 * when first requested an a parameters map.
 *
 * @author adrien
 */
public class AssetDescriptor {

    private final String id;
    private final String type;
    private final ResourcePath path;
    private final boolean lazyLoading;
    private final Map<String, Object> parameters;

    public AssetDescriptor(final String id, final String type, final ResourcePath path, final boolean lazyLoading, final Map<String, Object> parameters) {
        this.id = id;
        this.type = type;
        this.path = path;
        this.lazyLoading = lazyLoading;
        this.parameters = parameters;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public ResourcePath getPath() {
        return path;
    }

    public boolean isLazyLoading() {
        return lazyLoading;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

}
