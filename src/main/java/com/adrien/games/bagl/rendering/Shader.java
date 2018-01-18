package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.EngineException;
import com.adrien.games.bagl.resource.ShaderLoader;
import com.adrien.games.bagl.utils.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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

    private static final String BASE_SHADER_DIRECTORY = "/shaders/";

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
    public Shader() {
        this.matrix4fBuffer = MemoryUtil.memAllocFloat(16);
        this.uniformToLocationMap = new HashMap<>();
        this.attachedShaders = new ArrayList<>();
        this.handle = GL20.glCreateProgram();
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
     * Call the addShader function for a vertex shader
     *
     * @param file The path to the source file
     * @return This for chaining
     */
    public Shader addVertexShader(final String file) {
        this.addShader(file, GL20.GL_VERTEX_SHADER);
        return this;
    }

    /**
     * Call the addShader function for a fragment shader
     *
     * @param file The path to the source file
     * @return This for chaining
     */
    public Shader addFragmentShader(final String file) {
        this.addShader(file, GL20.GL_FRAGMENT_SHADER);
        return this;
    }

    /**
     * Call the addShader function for a geometry shader
     *
     * @param file The path to the source file
     * @return This for chaining
     */
    public Shader addGeometryShader(final String file) {
        this.addShader(file, GL32.GL_GEOMETRY_SHADER);
        return this;
    }

    /**
     * Load the source code of the shader. Parses it. Creates a new OpenGL shader object
     * and try to compile it. If it fails, displays the shader's log on the error output.
     * Finally it attaches the shader to the OpenGL program object and adds the handle
     * to the attachedShaders list
     *
     * @param file The path to the shader's source code
     * @param type The type of shader to load
     */
    private void addShader(final String file, final int type) {
        final String resourcePath = BASE_SHADER_DIRECTORY + file.replaceAll("^/*", "");
        final String source = new ShaderLoader().loadSourceFile(FileUtils.getResourceAbsolutePath(resourcePath));

        final int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);

        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            final int logLength = GL20.glGetShaderi(shader, GL20.GL_INFO_LOG_LENGTH);
            final String message = GL20.glGetShaderInfoLog(shader, logLength);
            LOG.error("Shader compilation error : {}", message);
            throw new EngineException("Shader compilation error : " + message);
        }
        GL20.glAttachShader(this.handle, shader);
        this.attachedShaders.add(shader);
    }

    /**
     * Compile the OpenGL program object. If it fails, displays the program's log on the
     * error output. Adds all the uniforms parsed in the shaders' source
     *
     * @return This for chaining
     */
    public Shader compile() {
        LOG.trace("Compiling shader");
        GL20.glLinkProgram(this.handle);
        if (GL20.glGetProgrami(this.handle, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            final int logLength = GL20.glGetProgrami(this.handle, GL20.GL_INFO_LOG_LENGTH);
            final String message = GL20.glGetProgramInfoLog(this.handle, logLength);
            throw new EngineException("Shader linking error : " + message);
        }

        final int uniformCount = GL20.glGetProgrami(this.handle, GL20.GL_ACTIVE_UNIFORMS);
        final int maxLength = GL20.glGetProgrami(this.handle, GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH);
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer size = stack.mallocInt(1);
            final IntBuffer type = stack.mallocInt(1);
            for (int i = 0; i < uniformCount; i++) {
                final String name = GL20.glGetActiveUniform(this.handle, i, maxLength, size, type);
                this.uniformToLocationMap.put(name, i);
            }
        }
        return this;
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
        final int location = this.getLocation(name);
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
        final int location = this.getLocation(name);
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
    public Shader setUniform(final String name, final Matrix4f matrix) {
        this.checkIsShaderBound();
        final int location = this.getLocation(name);
        GL20.glUniformMatrix4fv(location, false, matrix.get(this.matrix4fBuffer));
        return this;
    }

    /**
     * Set the value of a Vector3 uniform
     *
     * @param name   The name of the uniform
     * @param vector The value of the uniform
     * @return This for chaining
     */
    public Shader setUniform(final String name, final Vector3f vector) {
        this.checkIsShaderBound();
        final int location = this.getLocation(name);
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
        final int location = this.getLocation(name);
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
        final Integer location = this.uniformToLocationMap.get(name);
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
}
