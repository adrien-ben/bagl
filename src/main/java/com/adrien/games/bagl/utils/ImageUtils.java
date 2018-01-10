package com.adrien.games.bagl.utils;

import com.adrien.games.bagl.core.EngineException;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Image utility class
 *
 * @author adrien
 */
public final class ImageUtils {

    private ImageUtils() {
    }

    /**
     * Load an image file
     * <p>
     * Image loaded should then be destroyed using {@link ImageUtils#destroy(Image)} to
     * ensure memory is properly freed
     *
     * @param filePath The path of the image file to load
     * @return An {@link Image}
     */
    public static Image loadImage(final String filePath) {
        if (!new File(filePath).exists()) {
            throw new EngineException("The image file '" + filePath + "' does not exists");
        }
        final IntBuffer width = BufferUtils.createIntBuffer(1);
        final IntBuffer height = BufferUtils.createIntBuffer(1);
        final IntBuffer comp = BufferUtils.createIntBuffer(1);
        STBImage.stbi_set_flip_vertically_on_load(true);
        final ByteBuffer data = STBImage.stbi_load(filePath, width, height, comp, 0);
        if (data == null) {
            throw new EngineException("Failed to load image : '" + filePath + "'");
        }
        return new Image(width.get(), height.get(), comp.get(), data);
    }

    /**
     * Free memory allocated when loading an image
     *
     * @param image The image whose memory must be free
     */
    public static void destroy(final Image image) {
        STBImage.stbi_image_free(image.getData());
    }
}
