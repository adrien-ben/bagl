package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.*;
import com.adrien.games.bagl.core.camera.CameraController;
import com.adrien.games.bagl.core.camera.FPSCameraController;
import com.adrien.games.bagl.rendering.Material;
import com.adrien.games.bagl.rendering.Renderer;
import com.adrien.games.bagl.rendering.Spritebatch;
import com.adrien.games.bagl.rendering.model.Mesh;
import com.adrien.games.bagl.rendering.model.MeshFactory;
import com.adrien.games.bagl.rendering.model.Model;
import com.adrien.games.bagl.rendering.text.Font;
import com.adrien.games.bagl.rendering.text.TextRenderer;
import com.adrien.games.bagl.resource.scene.SceneLoader;
import com.adrien.games.bagl.scene.GameObject;
import com.adrien.games.bagl.scene.Scene;
import com.adrien.games.bagl.scene.components.CameraComponent;
import com.adrien.games.bagl.scene.components.ModelComponent;
import com.adrien.games.bagl.scene.components.PointLightComponent;
import com.adrien.games.bagl.scene.components.SpotLightComponent;
import com.adrien.games.bagl.utils.FileUtils;
import com.adrien.games.bagl.utils.MathUtils;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public class DeferredRenderingSample {

    private static final class TestGame implements Game {

        private static final String TITLE = "Deferred Rendering";

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

        private Font font;

        private CameraController cameraController;

        private Scene scene;
        private Mesh pointBulb;
        private Mesh spotBulb;

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

            this.font = new Font(FileUtils.getResourceAbsolutePath("/fonts/segoe/segoe.fnt"));


            this.scene = new SceneLoader().load(FileUtils.getResourceAbsolutePath("/scenes/demo_scene.json"));
            this.loadMeshes();
            this.initScene();

            this.scene.getObjectById("camera").ifPresent(cam ->
                    cam.getComponentOfType(CameraComponent.class).ifPresentOrElse(comp ->
                                    cameraController = new FPSCameraController(comp.getCamera()),
                            () -> {
                                throw new IllegalStateException("A scene should a at least a camera");
                            }));

            this.spritebatch = new Spritebatch(1024, this.width, this.height);
        }

        @Override
        public void destroy() {
            this.textRenderer.destroy();
            this.renderer.destroy();
            this.font.destroy();
            this.scene.destroy();
            this.pointBulb.destroy();
            this.spotBulb.destroy();
        }

        private void loadMeshes() {
            this.pointBulb = MeshFactory.createSphere(0.1f, 8, 8);
            this.spotBulb = MeshFactory.createCylinder(0.1f, 0.065f, 0.2f, 12);
        }

        private void initScene() {
            this.scene.getObjectsByTag("point_lights").forEach(parent ->
                    parent.getComponentOfType(PointLightComponent.class).ifPresent(point ->
                            createBulb(parent, point.getLight().getColor(), pointBulb)));

            this.scene.getObjectsByTag("spot_lights").forEach(parent ->
                    parent.getComponentOfType(SpotLightComponent.class).ifPresent(spot ->
                            createBulb(parent, spot.getLight().getColor(), spotBulb)));
        }

        private GameObject createBulb(final GameObject parent, final Color color, final Mesh bulbModel) {
            final var modelObject = parent.createChild("bulb_" + parent.getId());
            modelObject.getLocalTransform().setRotation(new Quaternionf().rotationX((float) Math.toRadians(-90f)));

            final var material = Material.builder().emissive(color).emissiveIntensity(10f).build();
            final var model = new Model();
            model.addNode().addMesh(bulbModel, material);
            final var modelComponent = new ModelComponent(model);
            modelObject.addComponent(modelComponent);
            return modelObject;
        }

        @Override
        public void update(final Time time) {
            this.scene.update(time);

            if (Input.isKeyPressed(GLFW.GLFW_KEY_1) || Input.isKeyPressed(GLFW.GLFW_KEY_2)) {
                final var speed = Input.isKeyPressed(GLFW.GLFW_KEY_1) ? 20 : -20;
                this.scene.getObjectById("sun").ifPresent(sunObj -> {
                    final var transform = new Transform()
                            .setRotation(new Quaternionf().setAngleAxis((float) Math.toRadians(speed * time.getElapsedTime()), 1f, 1f, 0f));
                    sunObj.getLocalTransform().transform(transform);
                });
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
                this.spritebatch.draw(this.renderer.getGBuffer().getColorTexture(0), new Vector2f());
            } else if (this.displayMode == DisplayMode.NORMALS) {
                this.spritebatch.draw(this.renderer.getGBuffer().getColorTexture(1), new Vector2f());
            } else if (this.displayMode == DisplayMode.DEPTH) {
                this.spritebatch.draw(this.renderer.getGBuffer().getDepthTexture(), new Vector2f());
            } else if (this.displayMode == DisplayMode.EMISSIVE) {
                this.spritebatch.draw(this.renderer.getGBuffer().getColorTexture(2), new Vector2f());
            } else if (this.displayMode == DisplayMode.SHADOW) {
                this.spritebatch.draw(this.renderer.getShadowBuffer().getDepthTexture(), new Vector2f(), MathUtils.min(this.width, this.height), MathUtils.min(this.width, this.height));
            } else if (this.displayMode == DisplayMode.UNPROCESSED) {
                this.spritebatch.draw(this.renderer.getFinalBuffer().getColorTexture(0), new Vector2f());
            }
            this.spritebatch.end();

            this.textRenderer.render("Toggle instructions : F1", this.font, new Vector2f(0.01f, 0.97f), 0.03f, Color.BLACK);
            if (this.displayInstructions) {
                this.textRenderer.render(INSTRUCTIONS, this.font, new Vector2f(0.01f, 0.94f), 0.03f, Color.BLACK);
            }

            final var lightCount = this.scene.getObjectsByTag("light").count();
            this.textRenderer.render("Lights: " + lightCount, this.font, new Vector2f(0.01f, 0.01f), 0.03f, Color.BLACK);
        }
    }

    private enum DisplayMode {
        SCENE, ALBEDO, NORMALS, DEPTH, EMISSIVE, SHADOW, UNPROCESSED
    }

    public static void main(String[] args) {
        new Engine(new TestGame(), TestGame.TITLE).start();
    }
}
