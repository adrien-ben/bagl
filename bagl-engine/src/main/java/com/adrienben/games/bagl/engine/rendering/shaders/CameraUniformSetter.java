package com.adrienben.games.bagl.engine.rendering.shaders;

import com.adrienben.games.bagl.engine.camera.Camera;
import com.adrienben.games.bagl.opengl.shader.Shader;

/**
 * Class responsible for setting shader uniforms related to camera.
 *
 * @author adrien
 */
public class CameraUniformSetter {

    private final Shader shader;

    public CameraUniformSetter(final Shader shader) {
        this.shader = shader;
    }

    public void setPositionUniform(final Camera camera) {
        shader.setUniform("uCamera.position", camera.getPosition());
    }

    public void setViewUniform(final Camera camera) {
        shader.setUniform("uCamera.view", camera.getView());
    }

    public void setViewProjectionUniform(final Camera camera) {
        shader.setUniform("uCamera.viewProj", camera.getViewProj());
    }

    public void setInvertedViewProjectionUniform(final Camera camera) {
        shader.setUniform("uCamera.invertedViewProj", camera.getInvertedViewProj());
    }
}
