package com.adrienben.games.bagl.opengl.buffer;

import java.nio.*;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;

/**
 * OpenGL buffer object.
 * <p>
 * When creating or updating the buffer the underlying OpenGL buffer is bound to the
 * {@link BufferTarget#COPY_WRITE} target.
 *
 * @author adrien
 */
public class Buffer {

    private final int handle;

    public Buffer(final ByteBuffer data, final BufferUsage usage) {
        this.handle = glGenBuffers();
        bindExecuteAndUnbind(() -> glBufferData(BufferTarget.COPY_WRITE.getGlCode(), data, usage.getGlCode()));
    }

    public Buffer(final ShortBuffer data, final BufferUsage usage) {
        this.handle = glGenBuffers();
        bindExecuteAndUnbind(() -> glBufferData(BufferTarget.COPY_WRITE.getGlCode(), data, usage.getGlCode()));
    }

    public Buffer(final IntBuffer data, final BufferUsage usage) {
        this.handle = glGenBuffers();
        bindExecuteAndUnbind(() -> glBufferData(BufferTarget.COPY_WRITE.getGlCode(), data, usage.getGlCode()));
    }

    public Buffer(final FloatBuffer data, final BufferUsage usage) {
        this.handle = glGenBuffers();
        bindExecuteAndUnbind(() -> glBufferData(BufferTarget.COPY_WRITE.getGlCode(), data, usage.getGlCode()));
    }

    public Buffer(final DoubleBuffer data, final BufferUsage usage) {
        this.handle = glGenBuffers();
        bindExecuteAndUnbind(() -> glBufferData(BufferTarget.COPY_WRITE.getGlCode(), data, usage.getGlCode()));
    }

    public void setSubData(final ByteBuffer data, final int offset) {
        bindExecuteAndUnbind(() -> glBufferSubData(BufferTarget.COPY_WRITE.getGlCode(), offset, data));
    }

    public void setSubData(final ShortBuffer data, final int offset) {
        bindExecuteAndUnbind(() -> glBufferSubData(BufferTarget.COPY_WRITE.getGlCode(), offset, data));
    }

    public void setSubData(final IntBuffer data, final int offset) {
        bindExecuteAndUnbind(() -> glBufferSubData(BufferTarget.COPY_WRITE.getGlCode(), offset, data));
    }

    public void setSubData(final FloatBuffer data, final int offset) {
        bindExecuteAndUnbind(() -> glBufferSubData(BufferTarget.COPY_WRITE.getGlCode(), offset, data));
    }

    public void setSubData(final DoubleBuffer data, final int offset) {
        bindExecuteAndUnbind(() -> glBufferSubData(BufferTarget.COPY_WRITE.getGlCode(), offset, data));
    }

    public void getSubData(final ByteBuffer data, final int offset) {
        bindExecuteAndUnbind(() -> glGetBufferSubData(BufferTarget.COPY_WRITE.getGlCode(), offset, data));
    }

    public void getSubData(final ShortBuffer data, final int offset) {
        bindExecuteAndUnbind(() -> glGetBufferSubData(BufferTarget.COPY_WRITE.getGlCode(), offset, data));
    }

    public void getSubData(final IntBuffer data, final int offset) {
        bindExecuteAndUnbind(() -> glGetBufferSubData(BufferTarget.COPY_WRITE.getGlCode(), offset, data));
    }

    public void getSubData(final FloatBuffer data, final int offset) {
        bindExecuteAndUnbind(() -> glGetBufferSubData(BufferTarget.COPY_WRITE.getGlCode(), offset, data));
    }

    public void getSubData(final DoubleBuffer data, final int offset) {
        bindExecuteAndUnbind(() -> glGetBufferSubData(BufferTarget.COPY_WRITE.getGlCode(), offset, data));
    }

    private void bindExecuteAndUnbind(final Runnable bufferAction) {
        glBindBuffer(BufferTarget.COPY_WRITE.getGlCode(), handle);
        bufferAction.run();
        glBindBuffer(BufferTarget.COPY_WRITE.getGlCode(), 0);
    }

    /**
     * Release OpenGL resources.
     */
    public void destroy() {
        glDeleteBuffers(handle);
    }

    public void bind(final BufferTarget target) {
        glBindBuffer(target.getGlCode(), handle);
    }

    public void unbind(final BufferTarget target) {
        glBindBuffer(target.getGlCode(), 0);
    }

    public void bind(final BufferTarget target, final int index) {
        glBindBufferBase(target.getGlCode(), index, handle);
    }

    public void unbind(final BufferTarget target, final int index) {
        glBindBufferBase(target.getGlCode(), index, 0);
    }

    public int getHandle() {
        return handle;
    }
}
