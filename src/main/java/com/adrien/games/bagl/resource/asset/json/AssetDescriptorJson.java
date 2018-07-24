package com.adrien.games.bagl.resource.asset.json;

import java.util.Map;

public class AssetDescriptorJson {

    private String id;
    private String type;
    private String path;
    private boolean lazyLoading;
    private Map<String, Object> parameters;

    public String getId() {
        return id;
    }

    public String getType() {
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
