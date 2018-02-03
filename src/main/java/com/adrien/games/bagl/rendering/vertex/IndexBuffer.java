package com.adrien.games.bagl.rendering.vertex;

import com.adrien.games.bagl.core.EngineException;
import com.adrien.games.bagl.rendering.BufferUsage;
import com.adrien.games.bagl.rendering.DataType;
import org.lwjgl.opengl.GL15;

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
    private final int iboId;

    public IndexBuffer(final IntBuffer buffer, final BufferUsage usage) {
        this.dataType = DataType.UNSIGNED_INT;
        this.size = buffer.capacity();
        this.iboId = GL15.glGenBuffers();
        this.bind(this.iboId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, usage.getGlCode());
        this.bind(0);
    }

    public IndexBuffer(final ShortBuffer buffer, final BufferUsage usage) {
        this.dataType = DataType.UNSIGNED_SHORT;
        this.size = buffer.capacity();
        this.iboId = GL15.glGenBuffers();
        this.bind(this.iboId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, usage.getGlCode());
        this.bind(0);
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
        this.iboId = GL15.glGenBuffers();
        this.bind(this.iboId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, usage.getGlCode());
        this.bind(0);
    }

    /**
     * Release resources
     * <p>
     * Unbinds the buffer if it is still bound
     */
    public void destroy() {
        if (IndexBuffer.boundBuffer == this.iboId) {
            this.bind(0);
        }
        GL15.glDeleteBuffers(this.iboId);
    }

    /**
     * Bind the buffer
     */
    public void bind() {
        if (IndexBuffer.boundBuffer != this.iboId) {
            this.bind(this.iboId);
        }
    }

    /**
     * Unbind the buffer
     *
     * @throws EngineException if the buffer is not bound
     */
    public void unbind() {
        this.checkIsBound("You cannot unbind an index buffer which is not bound");
        this.bind(0);
    }

    /**
     * Bind a buffer ou unbind any buffer if iboId is 0
     *
     * @param iboId The id of the buffer to bind
     */
    private void bind(final int iboId) {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, iboId);
        IndexBuffer.boundBuffer = iboId;
    }

    /**
     * Check that the buffer is bound
     *
     * @throws EngineException if the buffer is not bound
     */
    private void checkIsBound(final String message) {
        if (IndexBuffer.boundBuffer != this.iboId) {
            throw new EngineException(message);
        }
    }

    public DataType getDataType() {
        return this.dataType;
    }

    public int getSize() {
        return this.size;
    }
}
