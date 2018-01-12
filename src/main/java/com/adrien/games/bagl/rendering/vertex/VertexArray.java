package com.adrien.games.bagl.rendering.vertex;

import com.adrien.games.bagl.core.EngineException;
import com.adrien.games.bagl.rendering.DataType;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Vertex Array
 *
 * @author adrien
 */
public class VertexArray {

    private static int boundArray = 0;

    private final int vaoId;

    /**
     * Construct a vertex array
     */
    public VertexArray() {
        this.vaoId = GL30.glGenVertexArrays();
    }

    /**
     * Release resources
     * <p>
     * Unbind the vertex array if it is bound
     */
    public void destroy() {
        if (VertexArray.boundArray == this.vaoId) {
            this.bind(0);
        }
        GL30.glDeleteVertexArrays(this.vaoId);
    }

    /**
     * Attach a vertex buffer to this vertex array
     * <p>
     * It take care of enabling the vertex attribute arrays for each
     * element of the buffer
     *
     * @param buffer The buffer to attach
     * @throws EngineException if the vertex is not bound
     */
    public void attachVertexBuffer(final VertexBuffer buffer) {
        this.checkIsBound("You cannot attach a vertex buffer to a vertex array which is not bound");

        final VertexBufferParams params = buffer.getParams();

        if (!params.isInterlaced()) {
            // TODO: implement
            throw new EngineException("Not interlaced buffer are not implemented yet");
        }

        final DataType dataType = params.getDataType();
        final List<VertexElement> elements = params.getElements();
        final int stride = elements.stream().mapToInt(VertexElement::getSize).sum() * dataType.getSize();

        final AtomicInteger offset = new AtomicInteger(0);
        buffer.bind();
        elements.forEach(element -> this.enableVertexAttributePointer(element, dataType, stride, offset));
        buffer.unbind();
    }

    /**
     * Enable OpenGL vertex attribute array for one element of the buffer
     *
     * @param element  The element for which to enable the array
     * @param dataType The data type of the element
     * @param stride   The stride of the buffer
     * @param offset   The current offset of the array
     */
    private void enableVertexAttributePointer(final VertexElement element, final DataType dataType, final int stride, final AtomicInteger offset) {
        final int dataTypeSize = dataType.getSize();
        final int byteOffset = offset.getAndAdd(element.getSize()) * dataTypeSize;
        GL20.glEnableVertexAttribArray(element.getPosition());
        if (dataType.isWholeType() && !element.isNormalized()) {
            GL30.glVertexAttribIPointer(element.getPosition(), element.getSize(), dataType.getGlCode(), stride, byteOffset);
        } else {
            GL20.glVertexAttribPointer(element.getPosition(), element.getSize(), dataType.getGlCode(), element.isNormalized(), stride, byteOffset);
        }
    }

    /**
     * Bind the vertex array if it is not already bound
     */
    public void bind() {
        if (VertexArray.boundArray != this.vaoId) {
            this.bind(this.vaoId);
        }
    }

    /**
     * Unbind the buffer
     *
     * @throws EngineException if the buffer is not bound
     */
    public void unbind() {
        this.checkIsBound("You cannot unbind a vertex array which is not bound");
        this.bind(0);
    }

    /**
     * Bind a vertex array or unbind all vertex arrays if vaoId is 0
     *
     * @param vaoId The id of the vertex array to bind
     */
    private void bind(final int vaoId) {
        GL30.glBindVertexArray(vaoId);
        VertexArray.boundArray = vaoId;
    }

    /**
     * Check that the vertex array is bound
     *
     * @param errorMessage The error message if it is not bound
     * @throws EngineException if the vertex array is not bound
     */
    private void checkIsBound(final String errorMessage) {
        if (VertexArray.boundArray != this.vaoId) {
            throw new EngineException(errorMessage);
        }
    }
}
