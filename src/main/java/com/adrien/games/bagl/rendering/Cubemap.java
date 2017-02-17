package com.adrien.games.bagl.rendering;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

/**
 * 
 * OpenGL cube map.
 *
 */
public class Cubemap {

	private int handle;
	
	/**
	 * Creates a new CubeMap from the different faces file path. 
	 * @param left The path to the file containing the left face.
	 * @param right The path to the file containing the right face.
	 * @param bottom The path to the file containing the bottom face.
	 * @param top The path to the file containing the top face.
	 * @param back The path to the file containing the back face.
	 * @param front The path to the file containing the front face.
	 */
	public Cubemap(String left, String right, String bottom, String top, String back, String front) {
		this.handle = glGenTextures();
		glBindTexture(GL_TEXTURE_CUBE_MAP, this.handle);
        
        this.loadImageFromPath(left, GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
        this.loadImageFromPath(right, GL_TEXTURE_CUBE_MAP_POSITIVE_X);
        this.loadImageFromPath(bottom, GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);
        this.loadImageFromPath(top, GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
        this.loadImageFromPath(back, GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
        this.loadImageFromPath(front, GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);
        
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
		
		glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
	}
	
	private void loadImageFromPath(String path, int target) {
		IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer comp = BufferUtils.createIntBuffer(1);
		STBImage.stbi_set_flip_vertically_on_load(0);
		ByteBuffer image = STBImage.stbi_load(path, width, height, comp, 3);
		if(image == null) {
			throw new RuntimeException("Failed to load a face from the cubemap : '" + path + "'.");
		}
		glTexImage2D(target, 0, GL_RGB8, width.get(), height.get(), 0, GL_RGB, GL_UNSIGNED_BYTE, image);
	}
	
	/**
	 * Binds the cube map.
	 */
	public void bind() {
		glBindTexture(GL_TEXTURE_CUBE_MAP, this.handle);
	}
	
	/**
	 * Unbinds the cube map.
	 */
	public static void unbind() {
		glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
	}
	
	/**
	 * Release resources.
	 */
	public void destroy() {
		glDeleteTextures(this.handle);
	}
	
}
