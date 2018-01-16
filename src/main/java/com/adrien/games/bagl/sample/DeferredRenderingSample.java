package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.*;
import com.adrien.games.bagl.core.math.Quaternion;
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
        private static final String LIGHT_TAG = "light";

        private static final String INSTRUCTIONS = "Display scene : F2\n"
                + "Display Albedo : F3\n"
                + "Display Normals : F4\n"
                + "Display Depth : F5\n"
                + "Display Emissive : F6\n"
                + "Display Shadow Map : F7\n"
                + "Display Scene before post process : F8\n"
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
            this.initScene();

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

        private void initScene() {
            this.scene.getRoot().addChild(new CameraComponent(this.camera, "camera"));

            final ModelComponent floorComponent = new ModelComponent(this.floor, "floor");
            floorComponent.getLocalTransform().setScale(new Vector3(2f, 2f, 2f));
            this.scene.getRoot().addChild(floorComponent);

            final ModelComponent cubeComponent = new ModelComponent(this.cube, "cube");
            cubeComponent.getLocalTransform().setTranslation(new Vector3(0f, 0.5f, 0f));
            floorComponent.addChild(cubeComponent);

            final ModelComponent sphereComponent = new ModelComponent(this.sphere, "sphere");
            sphereComponent.getLocalTransform().setTranslation(new Vector3(1.5f, 0.6f, 0f));
            floorComponent.addChild(sphereComponent);

            this.setUpLights();
        }

        private void setUpLights() {
            final DirectionalLight sinLight = new DirectionalLight(0.8f, Color.WHITE, Vector3.ZERO);
            final DirectionalLightComponent directionalLight0 = new DirectionalLightComponent(sinLight, "sun", LIGHT_TAG);
            directionalLight0.getLocalTransform().setRotation(Quaternion.fromEuler((float) Math.toRadians(45.f), 0, (float) Math.toRadians(45.f)));
            this.scene.getRoot().addChild(directionalLight0);

            final Component floor = this.scene.getComponentById("floor").orElseThrow(() -> new EngineException("No component 'floor' in the scene"));

            this.addPointLight(floor, new Vector3(4f, 0.5f, 2f), 8f, Color.GREEN, 3f, "point_light_0");
            this.addPointLight(floor, new Vector3(-4f, 0.2f, 2f), 10f, Color.YELLOW, 2f, "point_light_1");
            this.addPointLight(floor, new Vector3(0f, 0.5f, 3f), 10f, Color.BLUE, 2f, "point_light_2");
            this.addPointLight(floor, new Vector3(-1f, 0.1f, 1f), 10f, Color.TURQUOISE, 2f, "point_light3");
            this.addPointLight(floor, new Vector3(3f, 0.6f, -3f), 10f, Color.CYAN, 2f, "point_light_4");
            this.addSpotLight(floor, new Vector3(-2f, 0.5f, -3f), Quaternion.fromEuler((float) Math.toRadians(45f), 0, 0), 10f, Color.RED,
                    20f, 20f, 5f, "spot_light_0");
            this.addSpotLight(floor, new Vector3(2f, 2f, 2f), Quaternion.fromEuler((float) Math.toRadians(90f), 0, 0), 2f, Color.WHITE,
                    7f, 10f, 4f, "spot_light_1");
        }

        private void addPointLight(final Component parent, final Vector3 position, final float intensity, final Color color, final float radius,
                                   final String id) {
            final PointLight light = new PointLight(intensity, color, Vector3.ZERO, radius);
            final PointLightComponent component = new PointLightComponent(light, id, LIGHT_TAG);
            component.getLocalTransform().setTranslation(position);
            parent.addChild(component);
        }

        private void addSpotLight(final Component parent, final Vector3 position, final Quaternion rotation, final float intensity, final Color color,
                                  final float radius, final float angle, final float edge, final String id) {
            final SpotLight light = new SpotLight(intensity, color, Vector3.ZERO, radius, Vector3.ZERO, angle, edge);
            final SpotLightComponent component = new SpotLightComponent(light, id, LIGHT_TAG);
            component.getLocalTransform().setTranslation(position).setRotation(rotation);
            parent.addChild(component);
        }

        @Override
        public void update(final Time time) {
            if (Input.isKeyPressed(GLFW.GLFW_KEY_1) || Input.isKeyPressed(GLFW.GLFW_KEY_2)) {
                float speed = Input.isKeyPressed(GLFW.GLFW_KEY_1) ? 20 : -20;
                this.scene.getComponentById("sun").ifPresent(component -> {
                    final Transform transform = new Transform()
                            .setRotation(Quaternion.fromAngleAndVector((float) Math.toRadians(speed * time.getElapsedTime()),
                                    new Vector3(1f, 1f, 0f).normalise()));
                    component.getLocalTransform().transform(transform);
                });
            }

            this.scene.getComponentsByType(PointLightComponent.class)
                    .map(PointLightComponent::getLight)
                    .forEach(light -> {
                        final float intensity = (float) Math.sin(time.getTotalTime() * 3f) * 40f + 60f;
                        light.setIntensity(intensity);
                    });

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
                this.displayMode = DisplayMode.EMISSIVE;
            } else if (Input.wasKeyPressed(GLFW.GLFW_KEY_F7)) {
                this.displayMode = DisplayMode.SHADOW;
            } else if (Input.wasKeyPressed(GLFW.GLFW_KEY_F8)) {
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
            } else if (this.displayMode == DisplayMode.EMISSIVE) {
                this.spritebatch.draw(this.renderer.getGBuffer().getColorTexture(2), Vector2.ZERO);
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
            final long lightCount = this.scene.getComponentsByTag(LIGHT_TAG).count();
            this.textRenderer.render("Lights: " + lightCount, this.font, new Vector2(0.01f, 0f), 0.03f, Color.BLACK);
        }
    }

    private enum DisplayMode {
        SCENE, ALBEDO, NORMALS, DEPTH, EMISSIVE, SHADOW, UNPROCESSED
    }

    public static void main(String[] args) {
        new Engine(new TestGame(), TestGame.TITLE).start();
    }
}
