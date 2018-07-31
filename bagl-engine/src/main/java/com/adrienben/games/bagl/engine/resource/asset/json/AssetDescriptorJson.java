package com.adrienben.games.bagl.engine.resource.asset.json;

import com.adrienben.games.bagl.engine.assets.AssetType;

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
