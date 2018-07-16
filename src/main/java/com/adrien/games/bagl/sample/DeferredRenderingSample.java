package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.*;
import com.adrien.games.bagl.rendering.Material;
import com.adrien.games.bagl.rendering.Spritebatch;
import com.adrien.games.bagl.rendering.model.Mesh;
import com.adrien.games.bagl.rendering.model.MeshFactory;
import com.adrien.games.bagl.rendering.model.Model;
import com.adrien.games.bagl.rendering.renderer.PBRDeferredSceneRenderer;
import com.adrien.games.bagl.rendering.text.Font;
import com.adrien.games.bagl.rendering.text.Text;
import com.adrien.games.bagl.rendering.text.TextRenderer;
import com.adrien.games.bagl.resource.scene.SceneLoader;
import com.adrien.games.bagl.scene.GameObject;
import com.adrien.games.bagl.scene.Scene;
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
                + "Move camera : Z, Q, S, D, LCTRL, SPACE\n"
                + "Advance time: 1, 2\n"
                + "Toggle debug info: A";

        private int width;
        private int height;

        private TextRenderer textRenderer;
        private PBRDeferredSceneRenderer renderer;

        private Font font;

        private Scene scene;
        private Mesh pointBulb;
        private Mesh spotBulb;

        private Text toggleInstructionsText;
        private Text instructionsText;

        private Spritebatch spritebatch;

        private DisplayMode displayMode = DisplayMode.SCENE;

        private boolean displayInstructions = false;
        private boolean fpsCamera = false;

        @Override
        public void init() {
            width = Configuration.getInstance().getXResolution();
            height = Configuration.getInstance().getYResolution();

            textRenderer = new TextRenderer();
            renderer = new PBRDeferredSceneRenderer();

            font = new Font(FileUtils.getResourceAbsolutePath("/fonts/segoe/segoe.fnt"));

            scene = new SceneLoader().load(FileUtils.getResourceAbsolutePath("/scenes/demo_scene.json"));
            loadMeshes();
            initScene();

            toggleInstructionsText = Text.create("Toggle instructions : F1", font, 0.01f, 0.97f, 0.03f, Color.BLACK);
            instructionsText = Text.create(INSTRUCTIONS, font, 0.01f, 0.94f, 0.03f, Color.BLACK);

            spritebatch = new Spritebatch(1024, width, height);
        }

        @Override
        public void destroy() {
            textRenderer.destroy();
            renderer.destroy();
            font.destroy();
            scene.destroy();
            pointBulb.destroy();
            spotBulb.destroy();
        }

        private void loadMeshes() {
            pointBulb = MeshFactory.createSphere(0.1f, 8, 8);
            spotBulb = MeshFactory.createCylinder(0.1f, 0.065f, 0.2f, 12);
        }

        private void initScene() {
            // Add debug models for lights
            scene.getObjectsByTag("point_lights").forEach(parent ->
                    parent.getComponentOfType(PointLightComponent.class).ifPresent(point ->
                            createBulb(parent, point.getLight().getColor(), pointBulb)));

            scene.getObjectsByTag("spot_lights").forEach(parent ->
                    parent.getComponentOfType(SpotLightComponent.class).ifPresent(spot ->
                            createBulb(parent, spot.getLight().getColor(), spotBulb)));
        }

        private GameObject createBulb(final GameObject parent, final Color color, final Mesh bulbModel) {
            final var modelObject = parent.createChild("bulb_" + parent.getId(), "debug");
            modelObject.getLocalTransform().setRotation(new Quaternionf().rotationX(MathUtils.toRadians(-90f)));

            final var material = Material.builder().emissive(color).emissiveIntensity(10f).build();
            final var model = new Model();
            model.addNode().addMesh(bulbModel, material);
            final var modelComponent = new ModelComponent(model);
            modelObject.addComponent(modelComponent);
            return modelObject;
        }

        @Override
        public void update(final Time time) {
            scene.update(time);
            rotateSun(time);
            toggleInstructions();
            toggleDebug();
            selectDisplayMode();
            selectCameraMode();
        }

        private void rotateSun(final Time time) {
            if (Input.isKeyPressed(GLFW.GLFW_KEY_1) || Input.isKeyPressed(GLFW.GLFW_KEY_2)) {
                final var speed = Input.isKeyPressed(GLFW.GLFW_KEY_1) ? 20 : -20;
                scene.getObjectById("sun").ifPresent(sunObj -> {
                    final var transform = new Transform()
                            .setRotation(new Quaternionf().setAngleAxis(MathUtils.toRadians(speed * time.getElapsedTime()), 1f, 1f, 0f));
                    sunObj.getLocalTransform().transform(transform);
                });
            }
        }

        private void toggleInstructions() {
            if (Input.wasKeyPressed(GLFW.GLFW_KEY_F1)) {
                displayInstructions = !displayInstructions;
            }
        }

        private void toggleDebug() {
            if (Input.wasKeyPressed(GLFW.GLFW_KEY_Q)) {
                scene.getObjectsByTag("debug").forEach(obj -> obj.setEnabled(!obj.isEnabled()));
            }
        }

        private void selectDisplayMode() {
            if (Input.wasKeyPressed(GLFW.GLFW_KEY_F2)) {
                displayMode = DisplayMode.SCENE;
            } else if (Input.wasKeyPressed(GLFW.GLFW_KEY_F3)) {
                displayMode = DisplayMode.ALBEDO;
            } else if (Input.wasKeyPressed(GLFW.GLFW_KEY_F4)) {
                displayMode = DisplayMode.NORMALS;
            } else if (Input.wasKeyPressed(GLFW.GLFW_KEY_F5)) {
                displayMode = DisplayMode.DEPTH;
            } else if (Input.wasKeyPressed(GLFW.GLFW_KEY_F6)) {
                displayMode = DisplayMode.EMISSIVE;
            } else if (Input.wasKeyPressed(GLFW.GLFW_KEY_F7)) {
                displayMode = DisplayMode.SHADOW;
            } else if (Input.wasKeyPressed(GLFW.GLFW_KEY_F8)) {
                displayMode = DisplayMode.UNPROCESSED;
            }
        }

        private void selectCameraMode() {
            if (Input.wasKeyPressed(GLFW.GLFW_KEY_TAB)) {
                fpsCamera = !fpsCamera;
                if (fpsCamera) {
                    Input.setMouseMode(MouseMode.DISABLED);
                } else {
                    Input.setMouseMode(MouseMode.NORMAL);
                }
            }
        }

        @Override
        public void render() {
            renderer.render(scene);
            renderDisplayMode();
            renderInstructions();
        }

        private void renderDisplayMode() {
            spritebatch.start();
            if (displayMode == DisplayMode.ALBEDO) {
                spritebatch.draw(renderer.getGBuffer().getColorTexture(0), new Vector2f());
            } else if (displayMode == DisplayMode.NORMALS) {
                spritebatch.draw(renderer.getGBuffer().getColorTexture(1), new Vector2f());
            } else if (displayMode == DisplayMode.DEPTH) {
                spritebatch.draw(renderer.getGBuffer().getDepthTexture(), new Vector2f());
            } else if (displayMode == DisplayMode.EMISSIVE) {
                spritebatch.draw(renderer.getGBuffer().getColorTexture(2), new Vector2f());
            } else if (displayMode == DisplayMode.SHADOW) {
                spritebatch.draw(renderer.getShadowBuffer().getDepthTexture(), new Vector2f(), MathUtils.min(width, height), MathUtils.min(width, height));
            } else if (displayMode == DisplayMode.UNPROCESSED) {
                spritebatch.draw(renderer.getFinalBuffer().getColorTexture(0), new Vector2f());
            }
            spritebatch.end();
        }

        private void renderInstructions() {
            textRenderer.render(toggleInstructionsText);
            if (displayInstructions) {
                textRenderer.render(instructionsText);
            }
        }
    }

    private enum DisplayMode {
        SCENE, ALBEDO, NORMALS, DEPTH, EMISSIVE, SHADOW, UNPROCESSED
    }

    public static void main(String[] args) {
        new Engine(new TestGame(), TestGame.TITLE).start();
    }
}
