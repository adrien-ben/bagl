package com.adrienben.games.bagl.opengl.vertex;

import com.adrienben.games.bagl.core.exception.EngineException;
import com.adrienben.games.bagl.opengl.DataType;
import org.lwjgl.opengl.GL15;

import java.nio.*;

/**
 * Vertex buffer
 *
 * @author adrien
 */
public class VertexBuffer {

    private static int boundBuffer = 0;

    private final int vboId;
    private final int vertexCount;
    private final int bufferSize;
    private final int stride;
    private final VertexBufferParams params;

    /**
     * Construct a vertex buffer
     * <p>
     * dataType must be {@link DataType#DOUBLE}
     *
     * @param buffer The vertex data
     * @param params The buffer parameters
     */
    public VertexBuffer(final DoubleBuffer buffer, final VertexBufferParams params) {
        this.checkType(params.getDataType(), DataType.DOUBLE, "DoubleBuffer is only compatible with DataType.DOUBLE");

        this.vertexCount = this.checkBufferConsistenceWithParams(buffer, DataType.DOUBLE, params);
        this.bufferSize = buffer.capacity();
        this.stride = this.computeStride(params);
        this.params = params;

        this.vboId = GL15.glGenBuffers();
        this.bind(this.vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, params.getUsage().getGlCode());
        this.bind(0);
    }

    /**
     * Construct a vertex buffer
     * <p>
     * dataType must be {@link DataType#FLOAT}
     *
     * @param buffer The vertex data
     * @param params The buffer parameters
     */
    public VertexBuffer(final FloatBuffer buffer, final VertexBufferParams params) {
        this.checkType(params.getDataType(), DataType.FLOAT, "FloatBuffer is only compatible with DataType.FLOAT");

        this.vertexCount = this.checkBufferConsistenceWithParams(buffer, DataType.FLOAT, params);
        this.bufferSize = buffer.capacity();
        this.stride = this.computeStride(params);
        this.params = params;

        this.vboId = GL15.glGenBuffers();
        this.bind(this.vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, params.getUsage().getGlCode());
        this.bind(0);
    }

    /**
     * Construct a vertex buffer
     * <p>
     * dataType must be {@link DataType#INT}
     *
     * @param buffer The vertex data
     * @param params The buffer parameters
     */
    public VertexBuffer(final IntBuffer buffer, final VertexBufferParams params) {
        this.checkType(params.getDataType(), DataType.INT, "IntBuffer is only compatible with DataType.INT");

        this.vertexCount = this.checkBufferConsistenceWithParams(buffer, DataType.INT, params);
        this.bufferSize = buffer.capacity();
        this.stride = this.computeStride(params);
        this.params = params;

        this.vboId = GL15.glGenBuffers();
        this.bind(this.vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, params.getUsage().getGlCode());
        this.bind(0);
    }

    /**
     * Construct a vertex buffer
     * <p>
     * dataType must be {@link DataType#SHORT}
     *
     * @param buffer The vertex data
     * @param params The buffer parameters
     */
    public VertexBuffer(final ShortBuffer buffer, final VertexBufferParams params) {
        this.checkType(params.getDataType(), DataType.SHORT, "ShortBuffer is only compatible with DataType.SHORT");

        this.vertexCount = this.checkBufferConsistenceWithParams(buffer, DataType.SHORT, params);
        this.bufferSize = buffer.capacity();
        this.stride = this.computeStride(params);
        this.params = params;

        this.vboId = GL15.glGenBuffers();
        this.bind(this.vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, params.getUsage().getGlCode());
        this.bind(0);
    }

    /**
     * Construct a vertex buffer
     * <p>
     * Unlike other constructor this one allows and {@code dataType}.
     *
     * @param buffer The vertex data
     * @param params The buffer parameters
     */
    public VertexBuffer(final ByteBuffer buffer, final VertexBufferParams params) {
        this.vertexCount = this.checkBufferConsistenceWithParams(buffer, DataType.BYTE, params);
        this.bufferSize = buffer.capacity() / params.getDataType().getSize();
        this.stride = this.computeStride(params);
        this.params = params;

        this.vboId = GL15.glGenBuffers();
        this.bind(this.vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, params.getUsage().getGlCode());
        this.bind(0);
    }

    /**
     * Compute buffer stride
     * <p>
     * The stride is the number of bytes between the start of two
     * elements (positions for example) in the buffer. If vertex
     * elements are packed the stride is 0. Otherwise it it the
     * sum of the sizes of the {@link VertexElement} of the buffer.
     *
     * @param params The parameters of the buffer
     * @return The stride of the buffer
     */
    private int computeStride(final VertexBufferParams params) {
        return params.isInterleaved()
                ? params.getElements().stream().mapToInt(VertexElement::getSize).sum() * params.getDataType().getSize()
                : 0;
    }

    /**
     * Check that the data type of the buffer match the expected data type
     *
     * @param dataType The data type of the buffer
     * @param expected The expected data type
     * @param message  The error message if they don't match
     */
    private void checkType(final DataType dataType, final DataType expected, final String message) {
        if (!dataType.equals(expected)) {
            throw new EngineException(message);
        }
    }

    /**
     * Check that buffer size is consistent with buffer parameters
     *
     * @param buffer         The buffer to check
     * @param bufferDataType The data type of elements in the buffer
     * @param params         The parameters of the buffer
     * @return The number of vertex contained in the buffer
     */
    private int checkBufferConsistenceWithParams(
            final Buffer buffer,
            final DataType bufferDataType,
            final VertexBufferParams params
    ) {
        final var capacity = buffer.capacity() / (params.getDataType().getSize() / bufferDataType.getSize());
        final var vertexSize = params.getElements().stream().mapToInt(VertexElement::getSize).sum();
        if (capacity % vertexSize != 0) {
            throw new EngineException("The number of elements in the buffer (" + capacity + ") is incorrect. It should be a multiple of "
                    + vertexSize + " (sum of the sizes of the vertex elements)");
        }
        return capacity / vertexSize;
    }

    /**
     * Release resources
     * <p>
     * Unbinds the buffer if it is still bound
     */
    public void destroy() {
        if (VertexBuffer.boundBuffer == this.vboId) {
            this.bind(0);
        }
        GL15.glDeleteBuffers(this.vboId);
    }

    /**
     * Update the vertex buffer data
     * <p>
     * dataType must be {@link DataType#DOUBLE}
     *
     * @param buffer The vertex data
     * @throws EngineException if the buffer is not bound
     */
    public void update(final DoubleBuffer buffer) {
        this.checkIsBound("You cannot update a vertex buffer which is not bound");
        this.checkType(params.getDataType(), DataType.DOUBLE, "DoubleBuffer is only compatible with DataType.DOUBLE. Current data type is "
                + this.params.getDataType());
        this.checkUpdateBufferSize(buffer);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
    }

    /**
     * Update the vertex buffer data
     * <p>
     * dataType must be {@link DataType#FLOAT}
     *
     * @param buffer The vertex data
     * @throws EngineException if the buffer is not bound
     */
    public void update(final FloatBuffer buffer) {
        this.checkIsBound("You cannot update a vertex buffer which is not bound");
        this.checkType(params.getDataType(), DataType.FLOAT, "FloatBuffer is only compatible with DataType.FLOAT. Current data type is "
                + this.params.getDataType());
        this.checkUpdateBufferSize(buffer);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
    }

    /**
     * Update the vertex buffer data
     * <p>
     * dataType must be {@link DataType#INT}
     *
     * @param buffer The vertex data
     * @throws EngineException if the buffer is not bound
     */
    public void update(final IntBuffer buffer) {
        this.checkIsBound("You cannot update a vertex buffer which is not bound");
        this.checkType(params.getDataType(), DataType.INT, "IntBuffer is only compatible with DataType.INT. Current data type is "
                + this.params.getDataType());
        this.checkUpdateBufferSize(buffer);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
    }

    /**
     * Update the vertex buffer data
     * <p>
     * dataType must be {@link DataType#SHORT}
     *
     * @param buffer The vertex data
     * @throws EngineException if the buffer is not bound
     */
    public void update(final ShortBuffer buffer) {
        this.checkIsBound("You cannot update a vertex buffer which is not bound");
        this.checkType(params.getDataType(), DataType.SHORT, "ShortBuffer is only compatible with DataType.SHORT. Current data type is "
                + this.params.getDataType());
        this.checkUpdateBufferSize(buffer);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
    }

    /**
     * Update the vertex buffer data
     * <p>
     * dataType must be {@link DataType#BYTE}
     *
     * @param buffer The vertex data
     * @throws EngineException if the buffer is not bound
     */
    public void update(final ByteBuffer buffer) {
        this.checkIsBound("You cannot update a vertex buffer which is not bound");
        this.checkUpdateBufferSize(buffer);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
    }

    /**
     * Check that the size of the buffer used when updating is not bigger than
     * the size of the buffer
     *
     * @param buffer The update buffer
     */
    private void checkUpdateBufferSize(final Buffer buffer) {
        if (buffer.capacity() / this.params.getDataType().getSize() > this.bufferSize) {
            throw new EngineException("The buffer has too much element. Max size is " + this.bufferSize);
        }
    }

    /**
     * Bind the buffer
     */
    public void bind() {
        if (VertexBuffer.boundBuffer != this.vboId) {
            this.bind(this.vboId);
        }
    }

    /**
     * Unbind the buffer
     *
     * @throws EngineException if the buffer is not bound
     */
    public void unbind() {
        this.checkIsBound("You cannot unbind a vertex buffer which is not bound");
        this.bind(0);
    }

    /**
     * Bind a buffer ou unbind any buffer if vboId is 0
     *
     * @param vboId The id of the buffer to bind
     */
    private void bind(final int vboId) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        VertexBuffer.boundBuffer = vboId;
    }

    /**
     * Check that the buffer is bound
     *
     * @throws EngineException if the buffer is not bound
     */
    private void checkIsBound(final String message) {
        if (VertexBuffer.boundBuffer != this.vboId) {
            throw new EngineException(message);
        }
    }

    public int getVertexCount() {
        return this.vertexCount;
    }

    public int getStride() {
        return this.stride;
    }

    public VertexBufferParams getParams() {
        return this.params;
    }
}
