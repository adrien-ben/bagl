package com.adrien.games.bagl.utils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

public final class ImageUtils {

	private ImageUtils() {
	}
	
	public static Image loadImage(String filePath) {
		IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer comp = BufferUtils.createIntBuffer(1);
		STBImage.stbi_set_flip_vertically_on_load(0);
		ByteBuffer data = STBImage.stbi_load(filePath, width, height, comp, 0);
		if(data == null) {
			throw new RuntimeException("Failed to load image : '" + filePath + "'.");
		}
		return new Image(width.get(), height.get(), comp.get(), data);
	}
	
}