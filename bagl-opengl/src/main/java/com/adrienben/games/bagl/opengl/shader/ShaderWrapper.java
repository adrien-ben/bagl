package com.adrienben.games.bagl.opengl.shader;


/**
 * This class is responsible to wrap a shader. It will allows basic action on shaders
 * as binding and destroying.
 * <p>
 * The extensions of the class can expose method to set up complex uniforms rather.
 *
 * @author adrien
 */
public abstract class ShaderWrapper {

    protected final Shader shader;

    public ShaderWrapper(final Shader shader) {
        this.shader = shader;
    }

    public void destroy() {
        shader.destroy();
    }

    public void bind() {
        shader.bind();
    }
}
