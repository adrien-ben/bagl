package com.adrienben.games.bagl.opengl.vertex;

import com.adrienben.games.bagl.core.exception.EngineException;
import com.adrienben.games.bagl.opengl.DataType;
import com.adrienben.games.bagl.opengl.buffer.Buffer;
import com.adrienben.games.bagl.opengl.buffer.BufferTarget;
import com.adrienben.games.bagl.opengl.buffer.BufferUsage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Index buffer
 *
 * @author adrien
 */
public class IndexBuffer {

    private static int boundBuffer = 0;

    private final DataType dataType;
    private final int size;
    private final Buffer buffer;

    public IndexBuffer(final IntBuffer buffer, final BufferUsage usage) {
        this.dataType = DataType.UNSIGNED_INT;
        this.size = buffer.capacity();
        this.buffer = new Buffer(buffer, usage);
    }

    public IndexBuffer(final ShortBuffer buffer, final BufferUsage usage) {
        this.dataType = DataType.UNSIGNED_SHORT;
        this.size = buffer.capacity();
        this.buffer = new Buffer(buffer, usage);
    }

    public IndexBuffer(final ByteBuffer buffer, final BufferUsage usage) {
        this(buffer, DataType.UNSIGNED_BYTE, usage);
    }

    /**
     * Construct a new index buffer
     * <p>
     * Indices are stored in a byte buffer but the actual type of the indices
     * can be any {@link DataType}
     *
     * @param buffer   The buffer containing indices data
     * @param dataType The data type of each index
     * @param usage    The buffer usage
     */
    public IndexBuffer(final ByteBuffer buffer, final DataType dataType, final BufferUsage usage) {
        this.dataType = dataType;
        this.size = buffer.capacity() / dataType.getSize();
        this.buffer = new Buffer(buffer, usage);
    }

    /**
     * Release resources
     * <p>
     * Unbinds the buffer if it is still bound
     */
    public void destroy() {
        if (boundBuffer == buffer.getHandle()) {
            bind(0);
        }
        buffer.destroy();
    }

    /**
     * Bind the buffer
     */
    public void bind() {
        if (boundBuffer != buffer.getHandle()) {
            bind(buffer.getHandle());
        }
    }

    /**
     * Unbind the buffer
     *
     * @throws EngineException if the buffer is not bound
     */
    public void unbind() {
        checkIsBound("You cannot unbind an index buffer which is not bound");
        bind(0);
    }

    /**
     * Bind a buffer ou unbind any buffer if iboId is 0
     *
     * @param iboId The id of the buffer to bind
     */
    private void bind(final int iboId) {
        buffer.bind(BufferTarget.ELEMENT_ARRAY);
        boundBuffer = iboId;
    }

    /**
     * Check that the buffer is bound
     *
     * @throws EngineException if the buffer is not bound
     */
    private void checkIsBound(final String message) {
        if (boundBuffer != buffer.getHandle()) {
            throw new EngineException(message);
        }
    }

    public DataType getDataType() {
        return dataType;
    }

    public int getSize() {
        return size;
    }
}
