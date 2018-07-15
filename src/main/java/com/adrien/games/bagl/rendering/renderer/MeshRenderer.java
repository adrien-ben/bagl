package com.adrien.games.bagl.rendering.renderer;

import com.adrien.games.bagl.rendering.model.Mesh;

import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glDrawElements;

/**
 * Mesh renderer is responsible for rendering {@link Mesh}.
 *
 * @author adrien
 */
public class MeshRenderer implements Renderer<Mesh> {

    /**
     * Render {@code mesh}.
     * <p>
     * Rendering will happened in the currently bound back buffer with the currently bound shader.
     *
     * @implNote This method binds the necessary buffers and calls glDrawElements or glDrawArrays if the mesh is indexed or not.
     */
    @Override
    public void render(final Mesh mesh) {
        mesh.getVertexArray().bind();
        mesh.getIndexBuffer().ifPresentOrElse(
                iBuffer -> {
                    iBuffer.bind();
                    glDrawElements(mesh.getPrimitiveType().getGlCode(), iBuffer.getSize(), iBuffer.getDataType().getGlCode(), 0);
                    iBuffer.unbind();
                },
                () -> glDrawArrays(mesh.getPrimitiveType().getGlCode(), 0, mesh.getVertexCount())
        );
        mesh.getVertexArray().unbind();
    }
}
