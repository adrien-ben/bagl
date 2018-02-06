package com.adrien.games.bagl.utils;

import com.adrien.games.bagl.exception.EngineException;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

/**
 * This class represents a image
 * <p>
 * An image has a width, an height, a number of channels and can be
 * flagged as HDR.
 */
public class Image implements AutoCloseable {

    private final int width;
    private final int height;
    private final int channelCount;
    private final boolean isHdr;
    private final FloatBuffer data;

    private Image(final int width, final int height, final int channelCount, final Boolean isHdr, final FloatBuffer data) {
        this.width = width;
        this.height = height;
        this.channelCount = channelCount;
        this.isHdr = isHdr;
        this.data = data;
    }

    /**
     * Load an image from a file
     *
     * @param filePath The path of the file
     * @return A new image
     */
    public static Image fromFile(final String filePath) {
        return Image.fromFile(filePath, false);
    }

    /**
     * Load an image from a file
     * <p>
     * You can choose to flip the image vertically
     *
     * @param filePath       The path to the image
     * @param flipVertically Should the image be flipped vertically
     * @return A new image
     */
    public static Image fromFile(final String filePath, final boolean flipVertically) {
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer width = stack.mallocInt(1);
            final IntBuffer height = stack.mallocInt(1);
            final IntBuffer comp = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(flipVertically);

            final boolean isHdr = STBImage.stbi_is_hdr(filePath);
            final FloatBuffer data = isHdr
                    ? STBImage.stbi_loadf(filePath, width, height, comp, 0)
                    : STBImage.stbi_load(filePath, width, height, comp, 0).asFloatBuffer();

            if (Objects.isNull(data)) {
                throw new EngineException("Failed to load image : '" + filePath + "'. Cause: "
                        + STBImage.stbi_failure_reason());
            }

            final FloatBuffer copy = Image.copyImageData(data);

            return new Image(width.get(), height.get(), comp.get(), isHdr, copy);
        }
    }

    /**
     * Load an image from memory
     *
     * @param image The image to load
     * @return A new image
     */
    public static Image fromMemory(final ByteBuffer image) {
        return Image.fromMemory(image, false);
    }

    /**
     * Load an image from memory
     * <p>
     * You can choose to flip the image vertically
     *
     * @param image          The image to load
     * @param flipVertically Should the image be flipped vertically
     * @return A new image
     */
    public static Image fromMemory(final ByteBuffer image, final boolean flipVertically) {
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer width = stack.mallocInt(1);
            final IntBuffer height = stack.mallocInt(1);
            final IntBuffer comp = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(flipVertically);

            final boolean isHdr = STBImage.stbi_is_hdr_from_memory(image);
            final FloatBuffer data = isHdr
                    ? STBImage.stbi_loadf_from_memory(image, width, height, comp, 0)
                    : STBImage.stbi_load_from_memory(image, width, height, comp, 0).asFloatBuffer();

            if (Objects.isNull(data)) {
                throw new EngineException("Failed to load image from memory. Cause: "
                        + STBImage.stbi_failure_reason());
            }

            final FloatBuffer copy = Image.copyImageData(data);

            return new Image(width.get(), height.get(), comp.get(), isHdr, copy);
        }
    }

    /**
     * Copy image data and free the original buffer
     *
     * @param toCopy The buffer to copy then free
     * @return A new float buffer
     */
    private static FloatBuffer copyImageData(final FloatBuffer toCopy) {
        final FloatBuffer copy = MemoryUtil.memAllocFloat(toCopy.capacity());
        MemoryUtil.memCopy(toCopy, copy);
        STBImage.stbi_image_free(toCopy);
        return copy;
    }

    @Override
    public void close() {
        MemoryUtil.memFree(this.data);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getChannelCount() {
        return this.channelCount;
    }

    public boolean isHdr() {
        return this.isHdr;
    }

    public FloatBuffer getData() {
        return this.data;
    }
}
