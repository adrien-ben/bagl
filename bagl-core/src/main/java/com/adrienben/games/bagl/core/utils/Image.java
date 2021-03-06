package com.adrienben.games.bagl.core.utils;

import com.adrienben.games.bagl.core.exception.EngineException;
import com.adrienben.games.bagl.core.io.ResourcePath;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.Optional;

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
     * @param path The path of the file
     * @return A new image
     */
    public static Image fromFile(final ResourcePath path) {
        return Image.fromFile(path, false);
    }

    /**
     * Load an image from a file
     * <p>
     * You can choose to flip the image vertically
     *
     * @param path       The path to the image
     * @param flipVertically Should the image be flipped vertically
     * @return A new image
     */
    public static Image fromFile(final ResourcePath path, final boolean flipVertically) {
        try (final var stack = MemoryStack.stackPush()) {
            final var width = stack.mallocInt(1);
            final var height = stack.mallocInt(1);
            final var comp = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(flipVertically);

            final var absoluteFilePath = path.getAbsolutePath();
            final var isHdr = STBImage.stbi_is_hdr(absoluteFilePath);


            final var data = isHdr ? loadHDRImage(absoluteFilePath, width, height, comp) : loadSDRImage(absoluteFilePath, width, height, comp);

            if (data.isEmpty()) {
                throw new EngineException(String.format("Failed to load image : '%s'. Cause: %s", absoluteFilePath, STBImage.stbi_failure_reason()));
            }

            final var copy = Image.copyImageData(data.get());

            return new Image(width.get(), height.get(), comp.get(), isHdr, copy);
        }
    }

    private static Optional<FloatBuffer> loadSDRImage(final String absoluteFilePath, final IntBuffer width, final IntBuffer height, final IntBuffer componentCount) {
        final var byteBuffer = STBImage.stbi_load(absoluteFilePath, width, height, componentCount, 0);
        if (Objects.isNull(byteBuffer)) {
            return Optional.empty();
        }
        return Optional.of(byteBuffer.asFloatBuffer());
    }

    private static Optional<FloatBuffer> loadHDRImage(final String absoluteFilePath, final IntBuffer width, final IntBuffer height, final IntBuffer componentCount) {
        return Optional.ofNullable(STBImage.stbi_loadf(absoluteFilePath, width, height, componentCount, 0));
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
        try (final var stack = MemoryStack.stackPush()) {
            final var width = stack.mallocInt(1);
            final var height = stack.mallocInt(1);
            final var comp = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(flipVertically);

            final var isHdr = STBImage.stbi_is_hdr_from_memory(image);
            final var data = isHdr
                    ? STBImage.stbi_loadf_from_memory(image, width, height, comp, 0)
                    : STBImage.stbi_load_from_memory(image, width, height, comp, 0).asFloatBuffer();

            if (Objects.isNull(data)) {
                throw new EngineException("Failed to load image from memory. Cause: "
                        + STBImage.stbi_failure_reason());
            }

            final var copy = Image.copyImageData(data);

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
        final var copy = MemoryUtil.memAllocFloat(toCopy.capacity());
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
