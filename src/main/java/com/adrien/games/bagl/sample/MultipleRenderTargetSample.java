package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.*;
import com.adrien.games.bagl.core.math.Matrix4;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.*;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.utils.MeshFactory;

import static org.lwjgl.opengl.GL11.*;

public class MultipleRenderTargetSample {

    private static final class TestGame implements Game {

        private static final String TITLE = "MRT";

        private int width;
        private int height;

        private FrameBuffer mrt;

        private Mesh mesh;
        private Matrix4 world;
        private Matrix4 wvp;

        private Shader shader;
        private Camera camera;

        private Spritebatch spritebatch;

        @Override
        public void init() {

            this.width = Configuration.getInstance().getXResolution();
            this.height = Configuration.getInstance().getYResolution();

            this.mrt = new FrameBuffer(this.width, this.height, 3);

            this.mesh = MeshFactory.createBox(5, 5, 5);

            this.world = new Matrix4();
            this.wvp = new Matrix4();

            this.shader = new Shader();
            this.shader.addVertexShader("/model.vert");
            this.shader.addFragmentShader("/mrt.frag");
            this.shader.compile();

            this.camera = new Camera(new Vector3(0, 3, 8), new Vector3(0, -3, -8), Vector3.UP,
                    (float)Math.toRadians(70f), (float)this.width/(float)this.height, 1, 1000);

            this.spritebatch = new Spritebatch(1024, this.width, this.height);

            glEnable(GL_DEPTH_TEST);
            glEnable(GL_CULL_FACE);
        }

        @Override
        public void update(Time time) {
            Matrix4.mul(this.camera.getViewProj(), this.world, this.wvp);
        }

        @Override
        public void render() {

            this.mesh.getVertices().bind();
            this.mesh.getIndices().bind();

            this.shader.bind();
            this.shader.setUniform("uMatrices.world", this.world);
            this.shader.setUniform("uMatrices.wvp", this.wvp);

            this.mrt.bind();
            FrameBuffer.clear();
            glDrawElements(GL_TRIANGLES, this.mesh.getIndices().getSize(), GL_UNSIGNED_INT, 0);
            FrameBuffer.unbind();

            IndexBuffer.unbind();
            VertexBuffer.unbind();
            Texture.unbind();
            Shader.unbind();

            this.spritebatch.start();
            this.spritebatch.draw(this.mrt.getColorTexture(0), Vector2.ZERO);
            this.spritebatch.draw(this.mrt.getColorTexture(1), new Vector2(0, 2*this.height/3), this.width/3, this.height/3);
            this.spritebatch.draw(this.mrt.getColorTexture(2), Vector2.ZERO, this.width/3, this.height/3);
            this.spritebatch.end();
        }

        @Override
        public void destroy() {
            this.shader.destroy();
            this.mesh.destroy();
            this.mrt.destroy();
        }

    }

    public static void main(String [] args) {
        new Engine(new TestGame(), TestGame.TITLE).start();
    }

}
