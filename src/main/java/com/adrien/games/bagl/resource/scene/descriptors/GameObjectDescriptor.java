package com.adrien.games.bagl.resource.scene.descriptors;

import java.util.List;
import java.util.Map;

public class GameObjectDescriptor {

    private String id;
    private List<String> tags;
    private TransformDescriptor transform;
    private List<Map<String, Object>> components;
    private List<GameObjectDescriptor> children;

    public String getId() {
        return id;
    }

    public List<String> getTags() {
        return tags;
    }

    public TransformDescriptor getTransform() {
        return transform;
    }

    public List<Map<String, Object>> getComponents() {
        return components;
    }

    public List<GameObjectDescriptor> getChildren() {
        return children;
    }
}
