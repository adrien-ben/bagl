package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.*;
import com.adrien.games.bagl.core.math.Quaternion;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.*;
import com.adrien.games.bagl.rendering.light.*;
import com.adrien.games.bagl.rendering.scene.Scene;
import com.adrien.games.bagl.rendering.scene.SceneNode;
import com.adrien.games.bagl.utils.FileUtils;
import com.adrien.games.bagl.utils.MeshFactory;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

import static org.lwjgl.opengl.GL11.*;

public class DeferredRenderingSample {

    private static final class TestGame implements Game {

        private static final String TITLE = "Deferred Rendering";

        private int width;
        private int height;

        private Renderer renderer;

        private Scene scene;
        private Skybox skybox;
        private Model floor;
        private Model sphere;

        private Camera camera;

        private Spritebatch spritebatch;

        private boolean isKeyPressed = false;
        private boolean displayGbuffer = false;

        @Override
        public void init() {
            this.width = Configuration.getInstance().getXResolution();
            this.height = Configuration.getInstance().getYResolution();

            this.renderer = new Renderer();

            this.scene = new Scene();
            this.loadMeshes();
            this.initSceneGraph();
            this.setUpLights();

            this.camera = new Camera(new Vector3(0f, 2f, 6f), new Vector3(0f, -2f, -6f), Vector3.UP,
                    (float)Math.toRadians(60f), (float)this.width/(float)this.height, 1, 1000);

            this.spritebatch = new Spritebatch(1024, this.width, this.height);

            glEnable(GL_DEPTH_TEST);
            glEnable(GL_CULL_FACE);
            glPointSize(6);
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
            this.sphere = MeshFactory.fromResourceFile("/models/tree/tree.obj");
        }

        private void initSceneGraph() {
            this.scene.getRoot().set(this.floor);
            final SceneNode<Model> sphereNode = new SceneNode<>(this.sphere);
            sphereNode.getLocalTransform().setTranslation(new Vector3(4f, 0f, 1.5f));
            this.scene.getRoot().addChild(sphereNode);
        }

        private void setUpLights() {
            this.scene.setAmbient(new Light(0.1f));
            this.scene.getDirectionals().add(new DirectionalLight(0.2f, Color.WHITE, new Vector3(0.5f, -2, 4)));
            this.scene.getDirectionals().add(new DirectionalLight(0.2f, Color.WHITE, new Vector3(0.5f, -2, 4)));
            this.scene.getPoints().add(new PointLight(1f, Color.GREEN, new Vector3(4f, 0.5f, 2f), 7f, Attenuation.CLOSE));
            this.scene.getPoints().add(new PointLight(1f, Color.YELLOW, new Vector3(-4f, 0.2f, 2f), 7f, Attenuation.CLOSE));
            this.scene.getPoints().add(new PointLight(1f, Color.BLUE, new Vector3(0f, 0.5f, 3f), 7f, Attenuation.CLOSE));
            this.scene.getPoints().add(new PointLight(1f, Color.PURPLE, new Vector3(0f, 3f, 0f), 7f, Attenuation.CLOSE));
            this.scene.getPoints().add(new PointLight(2f, Color.TURQUOISE, new Vector3(-1f, 0.1f, 1f), 7f, Attenuation.CLOSE));
            this.scene.getPoints().add(new PointLight(1f, Color.CYAN, new Vector3(3f, 0.6f, -3f), 7f, Attenuation.CLOSE));
            this.scene.getSpots().add(new SpotLight(10f, Color.RED, new Vector3(-2f, 0.5f, -3f), 7f, Attenuation.CLOSE,
                    new Vector3(0f, -1f, 0.8f), 20f, 5f));
            this.scene.getSpots().add(new SpotLight(2f, Color.WHITE, new Vector3(2f, 2f, 2f), 7f, Attenuation.CLOSE,
                    new Vector3(0f, -1f, -0f), 10f, 5f));
            this.scene.getSpots().add(new SpotLight(1f, Color.ORANGE, new Vector3(-0.5f, 0.5f, 0.5f), 7f, Attenuation.CLOSE,
                    new Vector3(2f, 0.7f, -1f), 20f, 5f));
        }

        @Override
        public void update(Time time) {
            //rotating the first child if any
            final Optional<SceneNode<Model>> node = this.scene.getRoot().getChildren().stream().findFirst();
            node.ifPresent(meshSceneNode -> meshSceneNode.getLocalTransform().getRotation().mul(Quaternion.fromAngleAndVector(
                    (float) Math.toRadians(10 * time.getElapsedTime()), Vector3.UP)));

            if(Input.isKeyPressed(GLFW.GLFW_KEY_SPACE) && !this.isKeyPressed) {
                this.displayGbuffer = !this.displayGbuffer;
                this.isKeyPressed = true;
            }
            if(!Input.isKeyPressed(GLFW.GLFW_KEY_SPACE) && this.isKeyPressed) {
                this.isKeyPressed = false;
            }
        }

        @Override
        public void render() {

            this.renderer.render(this.scene, this.camera);

            if(this.displayGbuffer) {
                this.spritebatch.start();
                this.spritebatch.draw(this.renderer.getGBuffer().getDepthTexture(), new Vector2(0, 2*this.height/3), this.width/3, this.height/3);
                this.spritebatch.draw(this.renderer.getGBuffer().getColorTexture(0), Vector2.ZERO, this.width/3, this.height/3);
                this.spritebatch.draw(this.renderer.getGBuffer().getColorTexture(1), new Vector2(0, this.height/3), this.width/3, this.height/3);
                this.spritebatch.end();
            }
        }


        @Override
        public void destroy() {
            this.renderer.destroy();
            this.skybox.destroy();
            this.floor.destroy();
            this.sphere.destroy();
        }

    }

    public static void main(String [] args) {
        new Engine(new TestGame(), TestGame.TITLE).start();
    }

}
