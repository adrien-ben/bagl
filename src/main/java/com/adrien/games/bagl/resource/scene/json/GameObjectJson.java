package com.adrien.games.bagl.resource.scene.json;

import java.util.List;
import java.util.Map;

public class GameObjectJson {

    private String id;
    private List<String> tags;
    private boolean enabled = true;
    private TransformJson transform;
    private List<Map<String, Object>> components;
    private List<GameObjectJson> children;

    public String getId() {
        return id;
    }

    public List<String> getTags() {
        return tags;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public TransformJson getTransform() {
        return transform;
    }

    public List<Map<String, Object>> getComponents() {
        return components;
    }

    public List<GameObjectJson> getChildren() {
        return children;
    }
}
