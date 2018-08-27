package com.adrienben.games.bagl.deferred.skybox;

import com.adrienben.games.bagl.deferred.shaders.ShaderFactory;
import com.adrienben.games.bagl.engine.camera.Camera;
import com.adrienben.games.bagl.engine.rendering.model.Mesh;
import com.adrienben.games.bagl.engine.rendering.model.MeshFactory;
import com.adrienben.games.bagl.engine.rendering.renderer.MeshRenderer;
import com.adrienben.games.bagl.opengl.shader.Shader;
import com.adrienben.games.bagl.opengl.texture.Cubemap;

import static org.lwjgl.opengl.GL11.*;

/**
 * Skybox renderer.
 * <p>
 * This class is responsible for rendering a {@link Cubemap} as a skybox.
 *
 * @author adrien.
 */
public class SkyboxRenderer {

    private final Shader skyboxShader;
    private final MeshRenderer meshRenderer;
    private final Mesh cubeMapMesh;

    public SkyboxRenderer() {
        this.skyboxShader = ShaderFactory.createSkyboxShader();
        this.meshRenderer = new MeshRenderer();
        this.cubeMapMesh = MeshFactory.createCubeMapMesh();
    }

    /**
     * Release resources
     */
    public void destroy() {
        skyboxShader.destroy();
        cubeMapMesh.destroy();
    }

    /**
     * Render {@code skybox} from {@code camera}'s point of view.
     * <p>
     * The renderer is designed to be applied after all the scene objects have been render. It will
     * only cover pixels from the currently bound {@link com.adrienben.games.bagl.opengl.FrameBuffer}
     * (ot the default back buffer) whose depth has not yet been written.
     *
     * @param skybox The cubemap to render as skybox.
     * @param camera The camera from which to render the skybox.
     */
    public void renderSkybox(final Cubemap skybox, final Camera camera) {
        skybox.bind();
        skyboxShader.bind();
        skyboxShader.setUniform("viewProj", camera.getViewProjAtOrigin());

        glDepthFunc(GL_LEQUAL);
        meshRenderer.render(cubeMapMesh);
        glDepthFunc(GL_LESS);

        Shader.unbind();
        skybox.unbind();
    }
}
