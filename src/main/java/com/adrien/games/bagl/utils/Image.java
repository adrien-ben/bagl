package com.adrien.games.bagl.utils;

import com.adrien.games.bagl.core.EngineException;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

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

    public static Image fromFile(final String filePath) {
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer width = stack.mallocInt(1);
            final IntBuffer height = stack.mallocInt(1);
            final IntBuffer comp = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(true);

            final boolean isHdr = STBImage.stbi_is_hdr(filePath);
            final FloatBuffer data = isHdr
                    ? STBImage.stbi_loadf(filePath, width, height, comp, 0)
                    : STBImage.stbi_load(filePath, width, height, comp, 0).asFloatBuffer();

            if (Objects.isNull(data)) {
                throw new EngineException("Failed to load image : '" + filePath + "'. Cause: "
                        + STBImage.stbi_failure_reason());
            }

            final FloatBuffer copy = MemoryUtil.memAllocFloat(data.capacity());
            MemoryUtil.memCopy(data, copy);
            STBImage.stbi_image_free(data);

            return new Image(width.get(), height.get(), comp.get(), isHdr, copy);
        }
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
