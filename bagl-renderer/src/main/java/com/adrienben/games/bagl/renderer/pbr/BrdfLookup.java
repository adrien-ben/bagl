package com.adrienben.games.bagl.renderer.pbr;

import com.adrienben.games.bagl.engine.Configuration;
import com.adrienben.games.bagl.engine.rendering.model.Mesh;
import com.adrienben.games.bagl.engine.rendering.model.MeshFactory;
import com.adrienben.games.bagl.engine.rendering.renderer.MeshRenderer;
import com.adrienben.games.bagl.opengl.FrameBuffer;
import com.adrienben.games.bagl.opengl.FrameBufferParameters;
import com.adrienben.games.bagl.opengl.shader.Shader;
import com.adrienben.games.bagl.opengl.texture.Format;
import com.adrienben.games.bagl.opengl.texture.Texture2D;
import com.adrienben.games.bagl.renderer.shaders.ShaderFactory;

import static org.lwjgl.opengl.GL11.glViewport;

/**
 * BRDF lookup for specular IBL.
 *
 * @author adrien
 */
public class BrdfLookup {

    private static final int BRDF_RESOLUTION = 512;

    private final FrameBuffer brdfBuffer;
    private final Shader brdfShader;
    private final Mesh screenQuad;
    private final MeshRenderer meshRenderer;

    public BrdfLookup() {
        this.brdfBuffer = new FrameBuffer(BRDF_RESOLUTION, BRDF_RESOLUTION, FrameBufferParameters.builder()
                .depthStencilTextureParameters(null)
                .colorOutputFormat(Format.RG16F)
                .build());
        this.brdfShader = ShaderFactory.createBrdfShader();
        this.screenQuad = MeshFactory.createScreenQuad();
        this.meshRenderer = new MeshRenderer();

        generateBrdfLookup();
    }

    private void generateBrdfLookup() {
        bindResources();
        renderBrdfLookup();
        unbindResources();
    }

    private void bindResources() {
        brdfBuffer.bind();
        brdfShader.bind();
    }

    private void renderBrdfLookup() {
        final var config = Configuration.getInstance();
        glViewport(0, 0, BRDF_RESOLUTION, BRDF_RESOLUTION);
        meshRenderer.render(screenQuad);
        glViewport(0, 0, config.getXResolution(), config.getYResolution());
    }

    private void unbindResources() {
        Shader.unbind();
        brdfBuffer.unbind();
    }

    public void destroy() {
        screenQuad.destroy();
        brdfShader.destroy();
        brdfBuffer.destroy();
    }

    public Texture2D getTexture() {
        return brdfBuffer.getColorTexture(0);
    }
}
