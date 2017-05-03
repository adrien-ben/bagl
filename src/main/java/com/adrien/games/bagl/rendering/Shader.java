package com.adrien.games.bagl.rendering;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.math.Matrix4;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.parser.GLSLParser;

/**
 * @author Adrien
 *
 */
public class Shader {
	
	private static final Logger log = LogManager.getLogger(Shader.class);
	
	private static final String BASE_SHADER_DIRECTORY = "/shaders/";
	
	/**
	 * Currently bound shader.
	 */
	private static Shader boundShader;
	
	private ArrayList<String> uniforms;
	private HashMap<String, Integer> uniformToLocationMap;
	private ArrayList<Integer> attachedShaders; 
	private int handle;

	public Shader() {
		this.uniforms = new ArrayList<String>();
		this.uniformToLocationMap = new HashMap<String, Integer>();
		this.attachedShaders = new ArrayList<Integer>();
		//TODO: error check
		this.handle = GL20.glCreateProgram();
	}

	/**
	 * Calls the addShader function for a vertex shader.
	 * @param file The path to the source file;
	 * @return This for chaining.
	 */
	public Shader addVertexShader(String file) {
		this.addShader(file, GL20.GL_VERTEX_SHADER);
		return this;
	}

	/**
	 * Calls the addShader function for a fragment shader.
	 * @param file The path to the source file;
	 * @return This for chaining.
	 */
	public Shader addFragmentShader(String file) {
		this.addShader(file, GL20.GL_FRAGMENT_SHADER);
		return this;
	}

	/**
	 * Load the source code of the shader. Parses it. Creates a new OpenGL shader object
	 * and try to compile it. If it fails, displays the shader's log on the error output. 
	 * Finally it attaches the shader to the OpenGL program object and adds the handle
	 * to the attachedShaders list.
	 * @param file The path to the shader's source code.
	 * @param type The type of shader to load.
	 * @author Adrien.
	 */
	private void addShader(String file, int type) {
		final String source = loadSource(file);
		//TODO: check
		this.parseShader(source);

		//TODO: error check
		final int shader = GL20.glCreateShader(type);
		GL20.glShaderSource(shader, source);
		GL20.glCompileShader(shader);

		if(GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			final int loglength = GL20.glGetShaderi(shader, GL20.GL_INFO_LOG_LENGTH);
			final String message = GL20.glGetShaderInfoLog(shader, loglength);
			log.error("Shader compilation error : {}", message);
			throw new RuntimeException("Shader compilation error : " + message);
		}
		GL20.glAttachShader(this.handle, shader);
		this.attachedShaders.add(shader);
	}

	/**
	 * Loads the shader's source code from a file.
	 * @param file The path to the file containing the source code.
	 * @return The shader's source code.
	 * @author Adrien.
	 */
	private static String loadSource(String name) {
		final String resourcePath = BASE_SHADER_DIRECTORY + name;
		final StringBuilder sourceBuilder = new StringBuilder();
		try (final BufferedReader sourceReader = new BufferedReader(new InputStreamReader(Shader.class.getResourceAsStream(resourcePath)))) {
			while(sourceReader.ready()) {
				sourceBuilder.append(sourceReader.readLine());
				sourceBuilder.append('\n');
			}
		} catch (IOException e) {
			log.error("Error while loading shader source file.", e);
		}
		return sourceBuilder.toString();
	}

	/**
	 * Parses a glsl source. Finds data structures and add uniforms to the uniform's list.
	 * @param source The glsl source code.
	 * @author Adrien.
	 */
	private void parseShader(String source) {
		final GLSLParser glslParser = new GLSLParser();
		glslParser.parse(source);
		this.uniforms.addAll(glslParser.getUniforms());
	}

	/**
	 * Asks OpenGL for the uniform location and adds it to uniformToLocationMap.
	 * @param name The name of the uniform.
	 * @author Adrien.
	 */
	private void addUniform(String name) {
		final int location = GL20.glGetUniformLocation(handle, name);
		this.uniformToLocationMap.put(name, location);
	}

	/**
	 * Sets the value of a float uniform.
	 * @param name The name of the uniform.
	 * @param value The value of the uniform.
	 * @return This for chaining.
	 * @author Adrien.
	 */
	public Shader setUniform(String name, float value) {
		this.checkIsShaderBound();
		final int location = this.getLocation(name);
		GL20.glUniform1f(location, value);
		return this;
	}
	
	/**
	 * Sets the value of a int uniform.
	 * @param name The name of the uniform.
	 * @param value The value of the uniform.
	 * @return This for chaining.
	 * @author Adrien.
	 */
	public Shader setUniform(String name, int value) {
		this.checkIsShaderBound();
		final int location = this.getLocation(name);
		GL20.glUniform1i(location, value);
		return this;
	}

	/**
	 * Sets the value of a Matrix4 uniform.
	 * @param name The name of the uniform.
	 * @param matrix The value of the uniform.
	 * @return This for chaining.
	 * @author Adrien.
	 */
	public Shader setUniform(String name, Matrix4 matrix) {
		this.checkIsShaderBound();
		final int location = this.getLocation(name);
		GL20.glUniformMatrix4fv(location, false, matrix.get());
		return this;
	}

	/**
	 * Sets the value of a Vector3 uniform.
	 * @param name The name of the uniform.
	 * @param vector The value of the uniform.
	 * @return This for chaining.
	 * @author Adrien.
	 */
	public Shader setUniform(String name, Vector3 vector) {
		this.checkIsShaderBound();
		final int location = this.getLocation(name);
		GL20.glUniform3f(location, vector.getX(), vector.getY(), vector.getZ());
		return this;
	}
	
	/**
	 * Sets the value of a {@link Color} uniform.
	 * @param name The name of the uniform.
	 * @param color The value of the uniform.
	 * @return This for chaining.
	 * @author Adrien.
	 */
	public Shader setUniform(String name, Color color) {
		this.checkIsShaderBound();
		final int location = this.getLocation(name);
		GL20.glUniform4f(location, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		return this;
	}

	/**
	 * Sets the value of a boolean uniform.
	 * @param name The name of the uniform.
	 * @param bool The value of the uniform.
	 * @return This for chaining.
	 * @author Adrien.
	 */
	public Shader setUniform(String name, boolean bool) {
		this.setUniform(name, bool ? 1 :0);
		return this;
	}
	
	/**
	 * Gets the location of a uniform parameter.
	 * @param name the name of the uniform parameter.
	 * @return The location of it.
	 * @throws IllegalArgumentException if it does not exists.
	 */
	private int getLocation(String name) {
		final Integer location = this.uniformToLocationMap.get(name);
		if(Objects.isNull(location)) {
			throw new IllegalArgumentException("The uniform '" + name +"' does not exist for the current shader.");
		}
		return location;
	}
	
	/**
	 * Compile the OpenGL program object. If it fails, displays the program's log on the 
	 * error output. Adds all the uniforms parsed in the shaders' source.
	 * @return This for chaining.
	 * @author Adrien.
	 */
	public Shader compile() {
		GL20.glLinkProgram(this.handle);

		if(GL20.glGetProgrami(this.handle, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
			final int logLength = GL20.glGetProgrami(this.handle, GL20.GL_INFO_LOG_LENGTH);
			final String message = GL20.glGetProgramInfoLog(this.handle, logLength);
			log.error("Shader linking error : {}", message);
			throw new RuntimeException("Shader linking error : " + message);
		}

		for(String uniform : this.uniforms) {
			addUniform(uniform);
		}
		this.uniforms.clear();
		return this;
	}

	/**
	 * Bind the OpenGL program object.
	 * @return This for chaining.
	 * @author Adrien.
	 */
	public Shader bind() {
		GL20.glUseProgram(handle);
		boundShader = this;
		return this;
	}

	/**
	 * Checks whether the shader is bound.
	 * <p>If not bound throws a runtime exception. This is used to prevent any operation on  
	 * unbound shader.
	 */
	private void checkIsShaderBound() {
		if(this != boundShader) {
			throw new RuntimeException("You're trying to do an operation on an unbound shader."
					+ "Please bind the shader first.");
		}
	}
	
	/**
	 * Unbind currently bound OpenGL program object.
	 * @author Adrien.
	 */
	public static void unbind() {
		GL20.glUseProgram(0);
		boundShader = null;
	}

	/**
	 * Release OpenGL resources.
	 * @author Adrien.
	 */
	public void destroy() {
		for(Integer i : this.attachedShaders) {
			GL20.glDeleteShader(i);
		}
		GL20.glDeleteProgram(this.handle);
	}
	
}
