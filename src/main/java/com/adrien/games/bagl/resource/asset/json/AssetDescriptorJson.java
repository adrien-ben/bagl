package com.adrien.games.bagl.resource.asset.json;

import com.adrien.games.bagl.assets.AssetType;

import java.util.Map;

public class AssetDescriptorJson {

    private String id;
    private AssetType type;
    private String path;
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

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
