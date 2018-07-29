package com.adrien.games.bagl.resource.asset.json;

import com.adrien.games.bagl.assets.AssetType;

import java.util.Map;

public class AssetDescriptorJson {

    private String id;
    private AssetType type;
    private String path;
    private boolean lazyLoading;
    private Map<String, Object> parameters;

    public String getId() {
        return id;
    }

    public AssetType getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public boolean isLazyLoading() {
        return lazyLoading;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
