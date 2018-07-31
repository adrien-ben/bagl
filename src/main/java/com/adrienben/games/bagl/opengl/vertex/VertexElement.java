package com.adrienben.games.bagl.opengl.vertex;

/**
 * Vertex element
 * <p>
 * This class represent an element of a vertex, it could be the position,
 * a normal or the color
 * <p>
 * It has a position, which is the position of the element in the buffer
 * and will has to match the 'location' from the shaders. It also has a size
 * which is the number of attributes of the element. For example, a position
 * element might have 3 attributes (x, y, and z). And it has a normalized flag.
 * If this flag is set to false then data is send without modification to the
 * gpu otherwise, il will be mapped to the range [0, 1]
 *
 * @author adrien
 */
public class VertexElement {

    private int position;
    private int size;
    private boolean normalized;

    /**
     * Construct a vertex element
     * <p>
     * Normalized flag is set to false
     *
     * @param position The position of the vertex in the buffer
     * @param size     The number of attributes of the element
     */
    public VertexElement(final int position, final int size) {
        this(position, size, false);
    }

    /**
     * Construct a vertex element
     *
     * @param position   The position of the vertex in the buffer
     * @param size       The number of attributes of the element
     * @param normalized Is the element data normalized ?
     */
    public VertexElement(final int position, final int size, final boolean normalized) {
        this.position = position;
        this.size = size;
        this.normalized = normalized;
    }

    public int getPosition() {
        return this.position;
    }

    public int getSize() {
        return this.size;
    }

    public boolean isNormalized() {
        return this.normalized;
    }
}
