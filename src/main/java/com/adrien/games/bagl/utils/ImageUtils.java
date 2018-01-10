package com.adrien.games.bagl.utils;

import com.adrien.games.bagl.core.EngineException;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

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
     * Image loaded should then be destroyed using {@link ImageUtils#free(Image)} to
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
     * Load an HDR image file
     * <p>
     * Image loaded should then be destroyed using {@link ImageUtils#free(HDRImage)} to
     * ensure memory is properly freed
     *
     * @param filePath The path of the image file to load
     * @return An {@link HDRImage}
     */
    public static HDRImage loadHDRImage(final String filePath) {
        if (!new File(filePath).exists()) {
            throw new EngineException("The image file '" + filePath + "' does not exists");
        }
        STBImage.stbi_set_flip_vertically_on_load(true);
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer width = stack.mallocInt(1);
            final IntBuffer height = stack.mallocInt(1);
            final IntBuffer channels = stack.mallocInt(1);
            final FloatBuffer pixels = STBImage.stbi_loadf(filePath, width, height, channels, 0);
            if (Objects.isNull(pixels)) {
                throw new EngineException("Failed to load image : '" + filePath + "'");
            }
            return new HDRImage(width.get(), height.get(), channels.get(), pixels);
        }

    }

    /**
     * Free memory allocated when loading an image
     *
     * @param image The image whose memory must be free
     */
    public static void free(final Image image) {
        STBImage.stbi_image_free(image.getData());
    }

    /**
     * Free memory allocated when loading an HDR image
     *
     * @param image The image whose memory must be free
     */
    public static void free(final HDRImage image) {
        STBImage.stbi_image_free(image.getData());
    }
}
