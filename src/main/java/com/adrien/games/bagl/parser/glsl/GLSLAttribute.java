package com.adrien.games.bagl.parser.glsl;

public class GLSLAttribute {

    private String type;
    private String name;

    public GLSLAttribute(String type, String name) {

        if (type == null || name == null) {
            throw new IllegalArgumentException("neither type nor name can be null");
        }

        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return type + " " + name + ";";
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

}