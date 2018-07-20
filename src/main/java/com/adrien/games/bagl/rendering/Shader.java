package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.exception.EngineException;
import com.adrien.games.bagl.utils.ResourcePath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4fc;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Shader class
 *
 * @author adrien
 */
public class Shader {

    private static final Logger LOG = LogManager.getLogger(Shader.class);

    /** Currently bound shader */
    private static Shader boundShader;

    private final FloatBuffer matrix4fBuffer;
    private final HashMap<String, Integer> uniformToLocationMap;
    private final ArrayList<Integer> attachedShaders;
    private final int handle;

    /**
     * Construct a shader
     * <p>
     * This create a openGL shader program
     */
    private Shader(final Builder builder) {
        this.matrix4fBuffer = MemoryUtil.memAllocFloat(16);
        this.uniformToLocationMap = new HashMap<>();
        this.attachedShaders = new ArrayList<>();
        this.handle = GL20.glCreateProgram();

        if (Objects.isNull(builder.vertexPath)) {
            throw new IllegalArgumentException("You cannot build a shader with no vertex shader source");
        }

        this.addShader(builder.vertexPath, GL20.GL_VERTEX_SHADER);
        if (Objects.nonNull(builder.fragmentPath)) {
            this.addShader(builder.fragmentPath, GL20.GL_FRAGMENT_SHADER);
        }
        if (Objects.nonNull(builder.geometryPath)) {
            this.addShader(builder.geometryPath, GL32.GL_GEOMETRY_SHADER);
        }
        this.compile();
    }

    /**
     * Generate a shader builder
     *
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Release OpenGL resources
     */
    public void destroy() {
        MemoryUtil.memFree(this.matrix4fBuffer);
        for (final Integer i : this.attachedShaders) {
            GL20.glDeleteShader(i);
        }
        GL20.glDeleteProgram(this.handle);
    }

    /**
     * Load the source code of the shader.
     * <p>
     * Create a new OpenGL shader object and compile it. Finally
     * it attaches the shader to the OpenGL program object and add
     * the handle to the attached shaders list
     *
     * @param filePath The path file
     * @param type     The type of shader to load
     * @throws EngineException If source loading or shader compilation fails
     */
    private void addShader(final ResourcePath filePath, final int type) {
        final var source = loadSource(filePath);

        final var shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);

        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            final var logLength = GL20.glGetShaderi(shader, GL20.GL_INFO_LOG_LENGTH);
            final var message = GL20.glGetShaderInfoLog(shader, logLength);
            throw new EngineException("Shader compilation error : " + message);
        }
        GL20.glAttachShader(this.handle, shader);
        this.attachedShaders.add(shader);
    }

    private String loadSource(final ResourcePath filePath) {
        try {
            return new String(filePath.openInputStream().readAllBytes());
        } catch (final IOException exception) {
            throw new EngineException(String.format("Failed to load shader source file %s", filePath), exception);
        }
    }

    /**
     * Compile the OpenGL program object. If it fails, displays the program's log on the
     * error output. Adds all the uniforms parsed in the shaders' source
     */
    private void compile() {
        LOG.trace("Compiling shader");
        GL20.glLinkProgram(this.handle);
        if (GL20.glGetProgrami(this.handle, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            final var logLength = GL20.glGetProgrami(this.handle, GL20.GL_INFO_LOG_LENGTH);
            final var message = GL20.glGetProgramInfoLog(this.handle, logLength);
            throw new EngineException("Shader linking error : " + message);
        }

        final var uniformCount = GL20.glGetProgrami(this.handle, GL20.GL_ACTIVE_UNIFORMS);
        final var maxLength = GL20.glGetProgrami(this.handle, GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH);
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final var size = stack.mallocInt(1);
            final var type = stack.mallocInt(1);
            for (var i = 0; i < uniformCount; i++) {
                final var name = GL20.glGetActiveUniform(this.handle, i, maxLength, size, type);
                this.uniformToLocationMap.put(name, i);
            }
        }
    }

    /**
     * Set the value of a float uniform
     *
     * @param name  The name of the uniform
     * @param value The value of the uniform
     * @return This for chaining
     */
    public Shader setUniform(final String name, final float value) {
        this.checkIsShaderBound();
        final var location = this.getLocation(name);
        GL20.glUniform1f(location, value);
        return this;
    }

    /**
     * Set the value of a int uniform
     *
     * @param name  The name of the uniform
     * @param value The value of the uniform
     * @return This for chaining
     */
    public Shader setUniform(final String name, final int value) {
        this.checkIsShaderBound();
        final var location = this.getLocation(name);
        GL20.glUniform1i(location, value);
        return this;
    }

    /**
     * Set the value of a Matrix4 uniform
     *
     * @param name   The name of the uniform
     * @param matrix The value of the uniform
     * @return This for chaining
     */
    public Shader setUniform(final String name, final Matrix4fc matrix) {
        this.checkIsShaderBound();
        final var location = this.getLocation(name);
        GL20.glUniformMatrix4fv(location, false, matrix.get(this.matrix4fBuffer));
        return this;
    }

    /**
     * Set the value of a Vector2 uniform
     *
     * @param name   The name of the uniform
     * @param vector The value of the uniform
     * @return This for chaining
     */
    public Shader setUniform(final String name, final Vector2fc vector) {
        this.checkIsShaderBound();
        final var location = this.getLocation(name);
        GL20.glUniform2f(location, vector.x(), vector.y());
        return this;
    }

    /**
     * Set the value of a Vector3 uniform
     *
     * @param name   The name of the uniform
     * @param vector The value of the uniform
     * @return This for chaining
     */
    public Shader setUniform(final String name, final Vector3fc vector) {
        this.checkIsShaderBound();
        final var location = this.getLocation(name);
        GL20.glUniform3f(location, vector.x(), vector.y(), vector.z());
        return this;
    }

    /**
     * Set the value of a {@link Color} uniform
     *
     * @param name  The name of the uniform
     * @param color The value of the uniform
     * @return This for chaining
     */
    public Shader setUniform(final String name, final Color color) {
        this.checkIsShaderBound();
        final var location = this.getLocation(name);
        GL20.glUniform4f(location, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        return this;
    }

    /**
     * Set the value of a boolean uniform
     *
     * @param name The name of the uniform
     * @param bool The value of the uniform
     * @return This for chaining
     */
    public Shader setUniform(final String name, final boolean bool) {
        this.setUniform(name, bool ? 1 : 0);
        return this;
    }

    /**
     * Get the location of a uniform parameter
     *
     * @param name the name of the uniform parameter
     * @return The location of it
     * @throws IllegalArgumentException if it does not exists
     */
    private int getLocation(final String name) {
        final var location = this.uniformToLocationMap.get(name);
        if (Objects.isNull(location)) {
            throw new IllegalArgumentException("The uniform '" + name + "' does not exist for the current shader.");
        }
        return location;
    }

    /**
     * Bind the OpenGL program object
     *
     * @return This for chaining
     */
    public Shader bind() {
        GL20.glUseProgram(handle);
        Shader.boundShader = this;
        return this;
    }

    /**
     * Check whether the shader is bound
     * <p>
     * If not bound throws a runtime exception. This is used to prevent any operation on
     * unbound shader
     */
    private void checkIsShaderBound() {
        if (this != Shader.boundShader) {
            throw new EngineException("You're trying to do an operation on an unbound shader."
                    + "Please bind the shader first.");
        }
    }

    /**
     * Unbind currently bound OpenGL program object
     */
    public static void unbind() {
        GL20.glUseProgram(0);
        Shader.boundShader = null;
    }

    /**
     * Shader builder
     */
    public static class Builder {

        private ResourcePath vertexPath;
        private ResourcePath fragmentPath;
        private ResourcePath geometryPath;

        /**
         * Private constructor to prevent instantiation
         */
        private Builder() {
        }

        /**
         * Build the shader
         *
         * @return The built shader
         */
        public Shader build() {
            return new Shader(this);
        }

        /**
         * Sets the path of the resource file containing vertex shader
         * source code
         * <p>
         * The file must be in the resource folder 'shaders'
         *
         * @param path The path of the resource
         * @return This
         */
        public Builder vertexPath(final ResourcePath path) {
            this.vertexPath = path;
            return this;
        }

        /**
         * Sets the path of the resource file containing fragment shader
         * source code
         * <p>
         * The file must be in the resource folder 'shaders'
         *
         * @param path The path of the resource
         * @return This
         */
        public Builder fragmentPath(final ResourcePath path) {
            this.fragmentPath = path;
            return this;
        }

        /**
         * Sets the path of the resource file containing geometry shader
         * source code
         * <p>
         * The file must be in the resource folder 'shaders'
         *
         * @param path The path of the resource
         * @return This
         */
        public Builder geometryPath(final ResourcePath path) {
            this.geometryPath = path;
            return this;
        }
    }
}
