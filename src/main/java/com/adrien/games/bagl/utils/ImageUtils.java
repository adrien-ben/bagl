package com.adrien.games.bagl.utils;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public final class ImageUtils {

    private ImageUtils() {
    }

    public static Image loadImage(String filePath) {
        if(!new File(filePath).exists()) {
            throw  new RuntimeException("The image file '" + filePath + "' does not exists");
        }
        final IntBuffer width = BufferUtils.createIntBuffer(1);
        final IntBuffer height = BufferUtils.createIntBuffer(1);
        final IntBuffer comp = BufferUtils.createIntBuffer(1);
        STBImage.stbi_set_flip_vertically_on_load(true);
        final ByteBuffer data = STBImage.stbi_load(filePath, width, height, comp, 0);
        if(data == null) {
            throw new RuntimeException("Failed to load image : '" + filePath + "'.");
        }
        return new Image(width.get(), height.get(), comp.get(), data);
    }

}
