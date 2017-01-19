package com.adrien.games.bagl.rendering;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.parser.GLSLParser;

/**
 * @author Adrien
 *
 */
public class Shader
{

	private ArrayList<String> uniforms;
	private HashMap<String, Integer> uniformToLocationMap;
	private ArrayList<Integer> attachedShaders; 
	private int handle;

	public Shader()
	{
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
	public void addVertexShader(String file)
	{
		addShader(file, GL20.GL_VERTEX_SHADER);
	}

	/**
	 * Calls the addShader function for a fragment shader.
	 * @param file The path to the source file;
	 */
	public void addFragmentShader(String file)
	{
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
	private void addShader(String file, int type)
	{
		String source = loadSource(file);
		//TODO: check
		parseShader(source);

		//TODO: error check
		int shader = GL20.glCreateShader(type);
		GL20.glShaderSource(shader, source);
		GL20.glCompileShader(shader);

		if(GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
		{
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
	private static String loadSource(String name)
	{
		StringBuilder sourceBuilder = new StringBuilder();
		try (BufferedReader sourceReader = new BufferedReader(new InputStreamReader(Shader.class.getResourceAsStream(name)))) {
			while(sourceReader.ready())
			{
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
	private void parseShader(String source)
	{
		GLSLParser glslParser = new GLSLParser();
		glslParser.parse(source);
		uniforms.addAll(glslParser.getUniforms());
	}

	/**
	 * Asks OpenGL for the uniform location and adds it to uniformToLocationMap.
	 * @param name The name of the uniform.
	 * @author Adrien.
	 */
	private void addUniform(String name)
	{
		int location = GL20.glGetUniformLocation(handle, name);
		uniformToLocationMap.put(name, location);
	}

	/**
	 * Sets the value of a float uniform.
	 * @param name The name of the uniform.
	 * @param value The value of the uniform.
	 * @author Adrien.
	 */
	public void setUniform(String name, float value)
	{
		int location = uniformToLocationMap.get(name);
		GL20.glUniform1f(location, value);
	}

	/**
	 * Sets the value of a Matrix4 uniform.
	 * @param name The name of the uniform.
	 * @param matrix The value of the uniform.
	 * @author Adrien.
	 */
	public void setUniform(String name, Matrix4 matrix)
	{
		//TODO: find a better way (using pools ?)
		int location = uniformToLocationMap.get(name);
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
	public void setUniform(String name, Vector3 vector)
	{
		int location = uniformToLocationMap.get(name);
		GL20.glUniform3f(location, vector.getX(), vector.getY(), vector.getZ());
	}

	/**
	 * Compile the OpenGL program object. If it fails, displays the program's log on the 
	 * error output. Adds all the uniforms parsed in the shaders' source.
	 * @author Adrien.
	 */
	public void compile()
	{
		GL20.glLinkProgram(handle);

		if(GL20.glGetProgrami(handle, GL20.GL_LINK_STATUS) == GL11.GL_FALSE)
		{
			int logLength = GL20.glGetProgrami(handle, GL20.GL_INFO_LOG_LENGTH);
			System.err.println(GL20.glGetProgramInfoLog(handle, logLength));
		}

		for(String uniform : uniforms)
		{
			addUniform(uniform);
		}
		uniforms.clear();
	}

	/**
	 * Bind the OpenGL program object.
	 * @author Adrien.
	 */
	public void bind()
	{
		GL20.glUseProgram(handle);
	}

	/**
	 * Unbind currently bound OpenGL program object.
	 * @author Adrien.
	 */
	public static void unbind()
	{
		GL20.glUseProgram(0);
	}

	/**
	 * Release OpenGL resources.
	 * @author Adrien.
	 */
	public void destroy()
	{
		for(Integer i : attachedShaders)
		{
			GL20.glDeleteShader(i);
		}
		GL20.glDeleteProgram(handle);
	}
}
