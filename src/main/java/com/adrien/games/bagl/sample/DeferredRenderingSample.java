package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.*;
import com.adrien.games.bagl.core.math.Matrix4;
import com.adrien.games.bagl.core.math.Quaternion;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.Model;
import com.adrien.games.bagl.rendering.Renderer;
import com.adrien.games.bagl.rendering.Skybox;
import com.adrien.games.bagl.rendering.Spritebatch;
import com.adrien.games.bagl.rendering.light.*;
import com.adrien.games.bagl.rendering.scene.Scene;
import com.adrien.games.bagl.rendering.scene.SceneNode;
import com.adrien.games.bagl.rendering.text.Font;
import com.adrien.games.bagl.rendering.text.TextRenderer;
import com.adrien.games.bagl.utils.FileUtils;
import com.adrien.games.bagl.utils.MeshFactory;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

import static org.lwjgl.opengl.GL11.*;

public class DeferredRenderingSample {

    private static final class TestGame implements Game {

        private static final String TITLE = "Deferred Rendering";

        private static final String INSTRUCTIONS = "Display debug infos : G\n" +
                "Move camera : Z, Q, S, D, LCTRL, SPACE\nAdvance time: 1, 2";

        private int width;
        private int height;

        private TextRenderer textRenderer;
        private Renderer renderer;

        private Font font;

        private Scene scene;
        private Skybox skybox;
        private Model floor;
        private Model cube;
        private Model tree;

        private Camera camera;
        private CameraController cameraController;

        private Spritebatch spritebatch;

        private boolean isKeyPressed = false;
        private boolean displayGbuffer = false;

        @Override
        public void init() {
            this.width = Configuration.getInstance().getXResolution();
            this.height = Configuration.getInstance().getYResolution();

            this.textRenderer = new TextRenderer();
            this.renderer = new Renderer();

            this.font = new Font(FileUtils.getResourceAbsolutePath("/fonts/segoe/segoe.fnt"));

            this.scene = new Scene();
            this.loadMeshes();
            this.initSceneGraph();
            this.setUpLights();

            this.camera = new Camera(new Vector3(5f, 4f, 6f), new Vector3(-5f, -4f, -6f), new Vector3(Vector3.UP),
                    (float)Math.toRadians(60f), (float)this.width/(float)this.height, 1, 1000);
            this.cameraController = new CameraController(this.camera);

            this.spritebatch = new Spritebatch(1024, this.width, this.height);

            glEnable(GL_DEPTH_TEST);
            glEnable(GL_CULL_FACE);
        }

        @Override
        public void destroy() {
            this.textRenderer.destroy();
            this.renderer.destroy();
            this.font.destroy();
            this.skybox.destroy();
            this.floor.destroy();
            this.cube.destroy();
            this.tree.destroy();
        }

        private void loadMeshes() {
            this.skybox = new Skybox(FileUtils.getResourceAbsolutePath("/skybox/left.png"),
                    FileUtils.getResourceAbsolutePath("/skybox/right.png"),
                    FileUtils.getResourceAbsolutePath("/skybox/bottom.png"),
                    FileUtils.getResourceAbsolutePath("/skybox/top.png"),
                    FileUtils.getResourceAbsolutePath("/skybox/back.png"),
                    FileUtils.getResourceAbsolutePath("/skybox/front.png"));
            this.scene.setSkybox(this.skybox);

            this.floor = MeshFactory.fromResourceFile("/models/floor/floor.obj");
            this.cube = MeshFactory.fromResourceFile("/models/cube/cube.obj");
            this.tree = MeshFactory.fromResourceFile("/models/tree/tree.obj");
        }

        private void initSceneGraph() {
            this.scene.getRoot().set(this.floor);
            final SceneNode<Model> cubeNode = new SceneNode<>(this.cube);
            cubeNode.getLocalTransform().setTranslation(new Vector3(0, 1f, 0)).setScale(new Vector3(0.5f, 0.5f, 0.5f));
            final SceneNode<Model> treeNode = new SceneNode<>(this.tree);
            treeNode.getLocalTransform().setTranslation(new Vector3(4f, 0f, 1.5f));
            this.scene.getRoot().addChild(treeNode);
            this.scene.getRoot().addChild(cubeNode);
        }

        private void setUpLights() {
            this.scene.setAmbient(new Light(0.1f));
            this.scene.getDirectionals().add(new DirectionalLight(0.5f, Color.WHITE, new Vector3(3f, -2, 4)));
            this.scene.getDirectionals().add(new DirectionalLight(0.3f, Color.ORANGE, new Vector3(0.5f, -2, 4)));
            this.scene.getPoints().add(new PointLight(1f, Color.GREEN, new Vector3(4f, 0.5f, 2f), 7f));
            this.scene.getPoints().add(new PointLight(1f, Color.YELLOW, new Vector3(-4f, 0.2f, 2f), 7f));
            this.scene.getPoints().add(new PointLight(1f, Color.BLUE, new Vector3(0f, 0.5f, 3f), 7f));
            this.scene.getPoints().add(new PointLight(1f, Color.PURPLE, new Vector3(0f, 3f, 0f), 7f));
            this.scene.getPoints().add(new PointLight(2f, Color.TURQUOISE, new Vector3(-1f, 0.1f, 1f), 7f));
            this.scene.getPoints().add(new PointLight(1f, Color.CYAN, new Vector3(3f, 0.6f, -3f), 7f));
//            this.scene.getSpots().add(new SpotLight(10f, Color.RED, new Vector3(-2f, 0.5f, -3f), 7f, Attenuation.CLOSE,
//                    new Vector3(0f, -1f, 0.8f), 20f, 5f));
//            this.scene.getSpots().add(new SpotLight(2f, Color.WHITE, new Vector3(2f, 2f, 2f), 7f, Attenuation.CLOSE,
//                    new Vector3(0f, -1f, -0f), 10f, 5f));
//            this.scene.getSpots().add(new SpotLight(1f, Color.ORANGE, new Vector3(-0.5f, 0.5f, 0.5f), 7f, Attenuation.CLOSE,
//                    new Vector3(2f, 0.7f, -1f), 20f, 5f));
        }

        @Override
        public void update(Time time) {
            final Optional<SceneNode<Model>> node = this.scene.getRoot().getChildren().stream().findFirst();
            node.ifPresent(meshSceneNode -> meshSceneNode.getLocalTransform().getRotation().mul(Quaternion.fromAngleAndVector(
                    (float) Math.toRadians(10 * time.getElapsedTime()), Vector3.UP)));

            if(Input.isKeyPressed(GLFW.GLFW_KEY_1) || Input.isKeyPressed(GLFW.GLFW_KEY_2)) {
                float speed = Input.isKeyPressed(GLFW.GLFW_KEY_1) ? 20 : -20;
                this.scene.getDirectionals().get(0).getDirection().transform(Matrix4.createRotation(Quaternion.fromAngleAndVector(
                        (float)Math.toRadians(speed*time.getElapsedTime()), new Vector3(1f, 1f, 0f).normalise())), 0);
            }

            if(Input.isKeyPressed(GLFW.GLFW_KEY_G) && !this.isKeyPressed) {
                this.displayGbuffer = !this.displayGbuffer;
                this.isKeyPressed = true;
            }
            if(!Input.isKeyPressed(GLFW.GLFW_KEY_G) && this.isKeyPressed) {
                this.isKeyPressed = false;
            }

            this.cameraController.update(time);
        }

        @Override
        public void render() {
            this.renderer.render(this.scene, this.camera);
            if(this.displayGbuffer) {
                this.spritebatch.start();
                this.spritebatch.draw(this.renderer.getGBuffer().getDepthTexture(),
                        new Vector2(4*this.width/5, 2*this.height/5), this.width/5, this.height/5);
                this.spritebatch.draw(this.renderer.getGBuffer().getColorTexture(0),
                        new Vector2(4*this.width/5, 0), this.width/5, this.height/5);
                this.spritebatch.draw(this.renderer.getGBuffer().getColorTexture(1),
                        new Vector2(4*this.width/5, this.height/5), this.width/5, this.height/5);
                this.spritebatch.draw(this.renderer.getShadowBuffer().getDepthTexture(),
                        new Vector2(4*this.width/5, 3*this.height/5), this.width/5, this.width/5);
                this.spritebatch.end();
            }
            this.textRenderer.render(INSTRUCTIONS, this.font, new Vector2(0.01f, 0.97f), 0.03f, Color.RED);
        }

    }

    public static void main(String [] args) {
        new Engine(new TestGame(), TestGame.TITLE).start();
    }

}
