package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.*;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.Renderer;
import com.adrien.games.bagl.rendering.Spritebatch;
import com.adrien.games.bagl.rendering.environment.EnvironmentMapGenerator;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.rendering.model.Model;
import com.adrien.games.bagl.rendering.model.ModelFactory;
import com.adrien.games.bagl.rendering.scene.Component;
import com.adrien.games.bagl.rendering.scene.Scene;
import com.adrien.games.bagl.rendering.scene.components.*;
import com.adrien.games.bagl.rendering.text.Font;
import com.adrien.games.bagl.rendering.text.TextRenderer;
import com.adrien.games.bagl.rendering.texture.Cubemap;
import com.adrien.games.bagl.utils.FileUtils;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.opengl.GL11.*;

public class DeferredRenderingSample {

    private static final class TestGame implements Game {

        private static final String TITLE = "Deferred Rendering";

        private static final String INSTRUCTIONS = "Display scene : F2\n"
                + "Display Albedo : F3\n"
                + "Display Normals : F4\n"
                + "Display Depth : F5\n"
                + "Display Shadow Map : F6\n"
                + "Display Scene before post process : F7\n"
                + "Switch Camera Mode : TAB\n"
                + "Move camera : Z, Q, S, D, LCTRL, SPACE\n"
                + "Advance time: 1, 2";

        private int width;
        private int height;

        private TextRenderer textRenderer;
        private Renderer renderer;
        private EnvironmentMapGenerator environmentMapGenerator;

        private Font font;

        private Camera camera;
        private CameraController cameraController;

        private Scene scene;
        private Cubemap environmentMap;
        private Cubemap irradianceMap;
        private Cubemap preFilteredMap;
        private Model floor;
        private Model cube;
        private Model sphere;

        private Spritebatch spritebatch;

        private DisplayMode displayMode = DisplayMode.SCENE;

        private boolean displayInstructions = false;
        private boolean fpsCamera = false;

        @Override
        public void init() {
            this.width = Configuration.getInstance().getXResolution();
            this.height = Configuration.getInstance().getYResolution();

            this.textRenderer = new TextRenderer();
            this.renderer = new Renderer();
            this.environmentMapGenerator = new EnvironmentMapGenerator();

            this.font = new Font(FileUtils.getResourceAbsolutePath("/fonts/segoe/segoe.fnt"));

            this.camera = new Camera(new Vector3(4f, 2.5f, 3f), new Vector3(-5f, -2.5f, -6f), new Vector3(Vector3.UP),
                    (float) Math.toRadians(60f), (float) this.width / (float) this.height, 0.1f, 1000);
            this.cameraController = new CameraController(this.camera);

            this.scene = new Scene();
            this.loadMeshes();
            this.initSceneGraph();

            this.spritebatch = new Spritebatch(1024, this.width, this.height);

            glEnable(GL_DEPTH_TEST);
            glEnable(GL_CULL_FACE);
        }

        @Override
        public void destroy() {
            this.textRenderer.destroy();
            this.renderer.destroy();
            this.environmentMapGenerator.destroy();
            this.font.destroy();
            this.environmentMap.destroy();
            this.irradianceMap.destroy();
            this.preFilteredMap.destroy();
            this.floor.destroy();
            this.cube.destroy();
            this.sphere.destroy();
        }

        private void loadMeshes() {
            this.environmentMap = this.environmentMapGenerator.generateEnvironmentMap(FileUtils.getResourceAbsolutePath("/envmaps/flat.hdr"));
//            this.environmentMap = this.environmentMapGenerator.generateEnvironmentMap("D:/Images/HDRI/lookout.hdr");
            this.irradianceMap = this.environmentMapGenerator.generateIrradianceMap(this.environmentMap);
            this.preFilteredMap = this.environmentMapGenerator.generatePreFilteredMap(this.environmentMap);

            this.scene.setEnvironmentMap(this.environmentMap);
            this.scene.setIrradianceMap(this.irradianceMap);
            this.scene.setPreFilteredMap(this.preFilteredMap);

            this.floor = ModelFactory.fromFile(FileUtils.getResourceAbsolutePath("/models/floor/floor.obj"));
            this.cube = ModelFactory.fromFile(FileUtils.getResourceAbsolutePath("/models/cube/cube.obj"));
//            this.cube = ModelFactory.fromFile("D:/Documents/3D Models/sphere/sphere.obj");
            this.sphere = ModelFactory.createSphere(0.5f, 25, 25, Color.YELLOW, true, 0.15f);
        }

        private void initSceneGraph() {
            this.scene.getRoot().addChild(new CameraComponent(this.camera));

            final ModelComponent floorComponent = new ModelComponent(this.floor);
            floorComponent.getLocalTransform().setScale(new Vector3(2f, 2f, 2f));
            this.scene.getRoot().addChild(floorComponent);

            final ModelComponent cubeComponent = new ModelComponent(this.cube);
            cubeComponent.getLocalTransform().setTranslation(new Vector3(0f, 0.5f, 0f));
            floorComponent.addChild(cubeComponent);

            final ModelComponent sphereComponent = new ModelComponent(this.sphere);
            sphereComponent.getLocalTransform().setTranslation(new Vector3(1.5f, 0.6f, 0f));
            floorComponent.addChild(sphereComponent);

            this.setUpLights(floorComponent);
        }

        private void setUpLights(final Component parent) {
            parent.addChild(new DirectionalLightComponent(new DirectionalLight(0.8f, Color.WHITE, new Vector3(3f, -2, 4))));

            final PointLightComponent pointLight0 = new PointLightComponent(new PointLight(10f, Color.GREEN, Vector3.ZERO, 2f));
            pointLight0.getLocalTransform().setTranslation(new Vector3(4f, 0.5f, 2f));
            parent.addChild(pointLight0);

            final PointLightComponent pointLight1 = new PointLightComponent(new PointLight(10f, Color.YELLOW, Vector3.ZERO, 3f));
            pointLight1.getLocalTransform().setTranslation(new Vector3(-4f, 0.2f, 2f));
            parent.addChild(pointLight1);

            final PointLightComponent pointLight2 = new PointLightComponent(new PointLight(10f, Color.BLUE, Vector3.ZERO, 2f));
            pointLight2.getLocalTransform().setTranslation(new Vector3(0f, 0.5f, 3f));
            parent.addChild(pointLight2);

            final PointLightComponent pointLight3 = new PointLightComponent(new PointLight(10f, Color.TURQUOISE, Vector3.ZERO, 2f));
            pointLight3.getLocalTransform().setTranslation(new Vector3(-1f, 0.1f, 1f));
            parent.addChild(pointLight3);

            final PointLightComponent pointLight4 = new PointLightComponent(new PointLight(10f, Color.CYAN, Vector3.ZERO, 2f));
            pointLight4.getLocalTransform().setTranslation(new Vector3(3f, 0.6f, -3f));
            parent.addChild(pointLight4);

            final SpotLightComponent spotLight0 = new SpotLightComponent(new SpotLight(10f, Color.RED, Vector3.ZERO, 20f,
                    new Vector3(0f, -1f, 1.2f), 20f, 5f));
            spotLight0.getLocalTransform().setTranslation(new Vector3(-2f, 0.5f, -3f));
            parent.addChild(spotLight0);

            final SpotLightComponent spotLight1 = new SpotLightComponent(new SpotLight(2f, Color.WHITE, Vector3.ZERO, 7f,
                    new Vector3(0f, -1f, -0f), 10f, 5f));
            spotLight1.getLocalTransform().setTranslation(new Vector3(2f, 2f, 2f));
            parent.addChild(spotLight1);
        }

        @Override
        public void update(final Time time) {
            if (Input.isKeyPressed(GLFW.GLFW_KEY_1) || Input.isKeyPressed(GLFW.GLFW_KEY_2)) {
                float speed = Input.isKeyPressed(GLFW.GLFW_KEY_1) ? 20 : -20;
//                if (!this.scene.getDirectionals().isEmpty()) {
//                    this.scene.getDirectionals().get(0).getDirection().transform(Matrix4.createRotation(Quaternion.fromAngleAndVector(
//                            (float) Math.toRadians(speed * time.getElapsedTime()), new Vector3(1f, 1f, 0f).normalise())), 0);
//                }
            }

            if (Input.wasKeyPressed(GLFW.GLFW_KEY_F1)) {
                this.displayInstructions = !this.displayInstructions;
            }

            if (Input.wasKeyPressed(GLFW.GLFW_KEY_F2)) {
                displayMode = DisplayMode.SCENE;
            } else if (Input.wasKeyPressed(GLFW.GLFW_KEY_F3)) {
                this.displayMode = DisplayMode.ALBEDO;
            } else if (Input.wasKeyPressed(GLFW.GLFW_KEY_F4)) {
                this.displayMode = DisplayMode.NORMALS;
            } else if (Input.wasKeyPressed(GLFW.GLFW_KEY_F5)) {
                this.displayMode = DisplayMode.DEPTH;
            } else if (Input.wasKeyPressed(GLFW.GLFW_KEY_F6)) {
                this.displayMode = DisplayMode.SHADOW;
            } else if (Input.wasKeyPressed(GLFW.GLFW_KEY_F7)) {
                this.displayMode = DisplayMode.UNPROCESSED;
            }

            if (Input.wasKeyPressed(GLFW.GLFW_KEY_TAB)) {
                this.fpsCamera = !this.fpsCamera;
                if (this.fpsCamera) {
                    Input.setMouseMode(MouseMode.DISABLED);
                } else {
                    Input.setMouseMode(MouseMode.NORMAL);
                }
            }

            if (this.fpsCamera) {
                this.cameraController.update(time);
            }
        }

        @Override
        public void render() {
            this.renderer.render(this.scene);

            this.spritebatch.start();
            if (this.displayMode == DisplayMode.ALBEDO) {
                this.spritebatch.draw(this.renderer.getGBuffer().getColorTexture(0), Vector2.ZERO);
            } else if (this.displayMode == DisplayMode.NORMALS) {
                this.spritebatch.draw(this.renderer.getGBuffer().getColorTexture(1), Vector2.ZERO);
            } else if (this.displayMode == DisplayMode.DEPTH) {
                this.spritebatch.draw(this.renderer.getGBuffer().getDepthTexture(), Vector2.ZERO);
            } else if (this.displayMode == DisplayMode.SHADOW) {
                this.spritebatch.draw(this.renderer.getShadowBuffer().getDepthTexture(), Vector2.ZERO);
            } else if (this.displayMode == DisplayMode.UNPROCESSED) {
                this.spritebatch.draw(this.renderer.getFinalBuffer().getColorTexture(0), Vector2.ZERO);
            }
            this.spritebatch.end();

            this.textRenderer.render("Toggle instructions : F1", this.font, new Vector2(0.01f, 0.97f), 0.03f, Color.BLACK);
            if (this.displayInstructions) {
                this.textRenderer.render(INSTRUCTIONS, this.font, new Vector2(0.01f, 0.94f), 0.03f, Color.BLACK);
            }
        }
    }

    private enum DisplayMode {
        SCENE, ALBEDO, NORMALS, DEPTH, SHADOW, UNPROCESSED
    }

    public static void main(String[] args) {
        new Engine(new TestGame(), TestGame.TITLE).start();
    }
}
