package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.*;
import com.adrien.games.bagl.core.math.Matrix4;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.*;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.utils.MeshFactory;

import static org.lwjgl.opengl.GL11.*;

public class RenderToTextureSample {

    private static final class TestGame implements Game {

        private static final String TITLE = "RTT";
        private final static Color BLUEISH = new Color(100f/255, 149f/255, 237f/255);

        private int width;
        private int height;
        private int paddingH;
        private int paddingV;

        private Mesh mesh;
        private Matrix4 world;
        private Matrix4 wvp;
        private float rotation;

        private Shader shader;

        private Camera camera;
        private Spritebatch spritebatch;
        private FrameBuffer frameBuffer;

        @Override
        public void init() {
            this.width = Configuration.getInstance().getXResolution();
            this.height = Configuration.getInstance().getYResolution();
            this.paddingH = (int)(this.height*0.2);
            this.paddingV = (int)(this.width*0.2);

            this.frameBuffer = new FrameBuffer(this.width, this.height);

            this.mesh = MeshFactory.createBox(5, 5, 5);

            this.world = new Matrix4();
            this.wvp = new Matrix4();
            this.rotation = 0f;

            this.shader = new Shader();
            this.shader.addVertexShader("/model.vert");
            this.shader.addFragmentShader("/ambient.frag");
            this.shader.compile();

            this.camera = new Camera(new Vector3(0, 3, 8), new Vector3(0, -3, -8), Vector3.UP,
                    (float)Math.toRadians(70f), (float)this.width/(float)this.height, 1, 1000);

            this.spritebatch = new Spritebatch(1024, this.width, this.height);

            glEnable(GL_DEPTH_TEST);
            glEnable(GL_CULL_FACE);
        }

        @Override
        public void update(Time time) {
            this.rotation += 1/(2*Math.PI)*time.getElapsedTime();
            this.world.setRotation(Vector3.UP, this.rotation);
            Matrix4.mul(this.camera.getViewProj(), this.world, this.wvp);
        }

        @Override
        public void render() {

            this.mesh.getVertices().bind();
            this.mesh.getIndices().bind();

            this.renderColors();

            IndexBuffer.unbind();
            VertexBuffer.unbind();
            Texture.unbind();

            this.spritebatch.start();
            this.spritebatch.draw(this.frameBuffer.getColorTexture(), new Vector2(this.paddingV, this.paddingH),
                    this.width - 2*this.paddingV, this.height - 2*this.paddingH);
            this.spritebatch.end();
        }

        private void renderColors() {
            this.shader.bind();
            this.shader.setUniform("uMatrices.world", this.world);
            this.shader.setUniform("uMatrices.wvp", this.wvp);
            this.shader.setUniform("uBaseLight.color", Color.WHITE);
            this.shader.setUniform("uBaseLight.intensity", 1.f);
            this.mesh.getMaterial().applyTo(this.shader);
            this.frameBuffer.bind();

            FrameBuffer.clear(BLUEISH);
            glDrawElements(GL_TRIANGLES, this.mesh.getIndices().getSize(), GL_UNSIGNED_INT, 0);

            FrameBuffer.unbind();
            Shader.unbind();
        }

        @Override
        public void destroy() {
            this.shader.destroy();
            this.mesh.destroy();
            this.frameBuffer.destroy();
        }

    }

    public static void main(String [] args) {
        new Engine(new TestGame(), TestGame.TITLE).start();
    }

}
