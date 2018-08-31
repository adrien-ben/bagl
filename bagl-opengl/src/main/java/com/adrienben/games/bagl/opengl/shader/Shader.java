package com.adrienben.games.bagl.opengl.shader;

import com.adrienben.games.bagl.core.Color;
import com.adrienben.games.bagl.core.exception.EngineException;
import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.core.utils.ObjectUtils;
import com.adrienben.games.bagl.opengl.shader.subshader.SubShader;
import com.adrienben.games.bagl.opengl.shader.subshader.SubShaderType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4fc;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.*;

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
    private final Map<String, Integer> uniformToLocationMap;
    private final List<SubShader> attachedShaders;
    private final int handle;

    /**
     * Construct a shader
     * <p>
     * This create a openGL shader program
     */
    private Shader(final PipelineBuilder pipelineBuilder) {
        this.matrix4fBuffer = MemoryUtil.memAllocFloat(16);
        this.uniformToLocationMap = new HashMap<>();
        this.attachedShaders = new ArrayList<>();
        this.handle = GL20.glCreateProgram();

        Objects.requireNonNull(pipelineBuilder.vertexPath, "You cannot build a shader with no vertex shader source");
        addSubShader(pipelineBuilder.vertexPath, SubShaderType.VERTEX);
        ObjectUtils.consumeIfPresent(pipelineBuilder.fragmentPath, path -> addSubShader(path, SubShaderType.FRAGMENT));
        ObjectUtils.consumeIfPresent(pipelineBuilder.geometryPath, path -> addSubShader(path, SubShaderType.GEOMETRY));
        compile();
    }

    /**
     * Return a new instance of a {@link PipelineBuilder}. A pipeline builder is used
     * to create shaders that will be performed by the graphic pipeline.
     *
     * @return A new builder
     */
    public static PipelineBuilder pipelineBuilder() {
        return new PipelineBuilder();
    }

    /**
     * Release OpenGL resources
     */
    public void destroy() {
        MemoryUtil.memFree(matrix4fBuffer);
        attachedShaders.forEach(SubShader::destroy);
        GL20.glDeleteProgram(handle);
    }

    /**
     * Load the source code of the shader.
     * <p>
     * Create a new {@link SubShader} and attach it to this shader.
     *
     * @param filePath The path file
     * @param type     The type of shader to load
     */
    private void addSubShader(final ResourcePath filePath, final SubShaderType type) {
        final var source = loadSource(filePath);
        final var subShader = new SubShader(source, type);
        attachSubShader(subShader);
    }

    private String loadSource(final ResourcePath filePath) {
        return new ShaderSourceParser().parse(filePath);
    }

    private void attachSubShader(final SubShader subShader) {
        GL20.glAttachShader(handle, subShader.getHandle());
        attachedShaders.add(subShader);
    }

    /**
     * Compile the OpenGL program object. If it fails, displays the program's log on the
     * error output. Adds all the uniforms parsed in the shaders' source
     */
    private void compile() {
        LOG.trace("Compiling shader");
        GL20.glLinkProgram(handle);
        checkLinkStatus();
        fetchActiveUniforms();
    }

    private void checkLinkStatus() {
        if (GL20.glGetProgrami(handle, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            final var logLength = GL20.glGetProgrami(handle, GL20.GL_INFO_LOG_LENGTH);
            final var message = GL20.glGetProgramInfoLog(handle, logLength);
            throw new EngineException("Shader linking error : " + message);
        }
    }

    private void fetchActiveUniforms() {
        final var uniformCount = GL20.glGetProgrami(handle, GL20.GL_ACTIVE_UNIFORMS);
        final var maxLength = GL20.glGetProgrami(handle, GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH);
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final var size = stack.mallocInt(1);
            final var type = stack.mallocInt(1);
            for (var i = 0; i < uniformCount; i++) {
                final var name = GL20.glGetActiveUniform(handle, i, maxLength, size, type);
                uniformToLocationMap.put(name, i);
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
        checkIsShaderBound();
        final var location = getLocation(name);
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
        checkIsShaderBound();
        final var location = getLocation(name);
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
        checkIsShaderBound();
        final var location = getLocation(name);
        GL20.glUniformMatrix4fv(location, false, matrix.get(matrix4fBuffer));
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
        checkIsShaderBound();
        final var location = getLocation(name);
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
        checkIsShaderBound();
        final var location = getLocation(name);
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
        checkIsShaderBound();
        final var location = getLocation(name);
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
        setUniform(name, bool ? 1 : 0);
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
        final var location = uniformToLocationMap.get(name);
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
     * A pipeline builder used to create shaders that will be performed by the graphic pipeline.
     */
    public static class PipelineBuilder {

        private ResourcePath vertexPath;
        private ResourcePath fragmentPath;
        private ResourcePath geometryPath;

        /**
         * Private constructor to prevent instantiation
         */
        private PipelineBuilder() {
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
        public PipelineBuilder vertexPath(final ResourcePath path) {
            vertexPath = path;
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
        public PipelineBuilder fragmentPath(final ResourcePath path) {
            fragmentPath = path;
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
        public PipelineBuilder geometryPath(final ResourcePath path) {
            geometryPath = path;
            return this;
        }
    }
}
