package com.adrien.games.bagl.rendering;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.parser.GLSLParser;

/**
 * @author Adrien
 *
 */
public class Shader {
	
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
	 */
	public void addVertexShader(String file) {
		addShader(file, GL20.GL_VERTEX_SHADER);
	}

	/**
	 * Calls the addShader function for a fragment shader.
	 * @param file The path to the source file;
	 */
	public void addFragmentShader(String file) {
		addShader(file, GL20.GL_FRAGMENT_SHADER);
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
		String source = loadSource(file);
		//TODO: check
		parseShader(source);

		//TODO: error check
		int shader = GL20.glCreateShader(type);
		GL20.glShaderSource(shader, source);
		GL20.glCompileShader(shader);

		if(GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			int loglength = GL20.glGetShaderi(shader, GL20.GL_INFO_LOG_LENGTH);
			System.err.println(GL20.glGetShaderInfoLog(shader, loglength));
		}
		GL20.glAttachShader(handle, shader);
		attachedShaders.add(shader);
	}

	/**
	 * Loads the shader's source code from a file.
	 * @param file The path to the file containing the source code.
	 * @return The shader's source code.
	 * @author Adrien.
	 */
	private static String loadSource(String name) {
		String resourcePath = BASE_SHADER_DIRECTORY + name;
		StringBuilder sourceBuilder = new StringBuilder();
		try (BufferedReader sourceReader = new BufferedReader(new InputStreamReader(Shader.class.getResourceAsStream(resourcePath)))) {
			while(sourceReader.ready()) {
				sourceBuilder.append(sourceReader.readLine());
				sourceBuilder.append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sourceBuilder.toString();
	}

	/**
	 * Parses a glsl source. Finds data structures and add uniforms to the uniform's list.
	 * @param source The glsl source code.
	 * @author Adrien.
	 */
	private void parseShader(String source) {
		GLSLParser glslParser = new GLSLParser();
		glslParser.parse(source);
		uniforms.addAll(glslParser.getUniforms());
	}

	/**
	 * Asks OpenGL for the uniform location and adds it to uniformToLocationMap.
	 * @param name The name of the uniform.
	 * @author Adrien.
	 */
	private void addUniform(String name) {
		int location = GL20.glGetUniformLocation(handle, name);
		uniformToLocationMap.put(name, location);
	}

	/**
	 * Sets the value of a float uniform.
	 * @param name The name of the uniform.
	 * @param value The value of the uniform.
	 * @author Adrien.
	 */
	public void setUniform(String name, float value) {
		this.checkIsShaderBound();
		int location = this.getLocation(name);
		GL20.glUniform1f(location, value);
	}
	
	/**
	 * Sets the value of a int uniform.
	 * @param name The name of the uniform.
	 * @param value The value of the uniform.
	 * @author Adrien.
	 */
	public void setUniform(String name, int value) {
		this.checkIsShaderBound();
		int location = this.getLocation(name);
		GL20.glUniform1i(location, value);
	}

	/**
	 * Sets the value of a Matrix4 uniform.
	 * @param name The name of the uniform.
	 * @param matrix The value of the uniform.
	 * @author Adrien.
	 */
	public void setUniform(String name, Matrix4 matrix) {
		this.checkIsShaderBound();
		int location = this.getLocation(name);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
		buffer.put(matrix.get());		
		buffer.flip();
		GL20.glUniformMatrix4fv(location, false, buffer);
	}

	/**
	 * Sets the value of a Vector3 uniform.
	 * @param name The name of the uniform.
	 * @param vector The value of the uniform.
	 * @author Adrien.
	 */
	public void setUniform(String name, Vector3 vector) {
		this.checkIsShaderBound();
		int location = this.getLocation(name);
		GL20.glUniform3f(location, vector.getX(), vector.getY(), vector.getZ());
	}
	
	/**
	 * Sets the value of a {@link Color} uniform.
	 * @param name The name of the uniform.
	 * @param color The value of the uniform.
	 * @author Adrien.
	 */
	public void setUniform(String name, Color color) {
		this.checkIsShaderBound();
		int location = this.getLocation(name);
		GL20.glUniform4f(location, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	/**
	 * Sets the value of a boolean uniform.
	 * @param name The name of the uniform.
	 * @param bool The value of the uniform.
	 * @author Adrien.
	 */
	public void setUniform(String name, boolean bool) {
		this.setUniform(name, bool ? 1 :0);
	}
	
	/**
	 * Gets the location of a uniform parameter.
	 * @param name the name of the uniform parameter.
	 * @return The location of it.
	 * @throws IllegalArgumentException if it does not exists.
	 */
	private int getLocation(String name) {
		Integer location = uniformToLocationMap.get(name);
		if(Objects.isNull(location)) {
			throw new IllegalArgumentException("The uniform '" + name +"' does not exist for the current shader.");
		}
		return location;
	}
	
	/**
	 * Compile the OpenGL program object. If it fails, displays the program's log on the 
	 * error output. Adds all the uniforms parsed in the shaders' source.
	 * @author Adrien.
	 */
	public void compile() {
		GL20.glLinkProgram(handle);

		if(GL20.glGetProgrami(handle, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
			int logLength = GL20.glGetProgrami(handle, GL20.GL_INFO_LOG_LENGTH);
			System.err.println(GL20.glGetProgramInfoLog(handle, logLength));
		}

		for(String uniform : uniforms) {
			addUniform(uniform);
		}
		uniforms.clear();
	}

	/**
	 * Bind the OpenGL program object.
	 * @author Adrien.
	 */
	public void bind() {
		GL20.glUseProgram(handle);
		boundShader = this;
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
		for(Integer i : attachedShaders) {
			GL20.glDeleteShader(i);
		}
		GL20.glDeleteProgram(handle);
	}
	
}
