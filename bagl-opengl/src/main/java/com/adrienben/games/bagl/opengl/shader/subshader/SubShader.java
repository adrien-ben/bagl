package com.adrienben.games.bagl.opengl.shader.subshader;

import com.adrienben.games.bagl.core.exception.EngineException;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL20.*;

/**
 * Wrapper class for an OpenGL shader.
 *
 * @author adrien
 */
public class SubShader {

    private final int handle;

    public SubShader(final String source, final SubShaderType type) {
        this.handle = glCreateShader(type.getGlCode());
        compileShader(source);
    }

    private void compileShader(final String source) {
        glShaderSource(handle, source);
        glCompileShader(handle);
        checkCompileStatus();
    }

    private void checkCompileStatus() {
        if (glGetShaderi(handle, GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            final var logLength = glGetShaderi(handle, GL_INFO_LOG_LENGTH);
            final var message = glGetShaderInfoLog(handle, logLength);
            throw new EngineException("Shader compilation error : " + message);
        }
    }

    public void destroy() {
        glDeleteShader(handle);
    }

    public int getHandle() {
        return handle;
    }
}
