package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.*;
import com.adrien.games.bagl.core.camera.Camera;
import com.adrien.games.bagl.core.camera.CameraController;
import com.adrien.games.bagl.core.camera.FPSCameraController;
import com.adrien.games.bagl.rendering.Material;
import com.adrien.games.bagl.rendering.Renderer;
import com.adrien.games.bagl.rendering.Spritebatch;
import com.adrien.games.bagl.rendering.environment.EnvironmentMapGenerator;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.rendering.model.Mesh;
import com.adrien.games.bagl.rendering.model.MeshFactory;
import com.adrien.games.bagl.rendering.model.Model;
import com.adrien.games.bagl.rendering.model.ModelFactory;
import com.adrien.games.bagl.rendering.text.Font;
import com.adrien.games.bagl.rendering.text.TextRenderer;
import com.adrien.games.bagl.rendering.texture.Cubemap;
import com.adrien.games.bagl.resource.GltfLoader;
import com.adrien.games.bagl.scene.GameObject;
import com.adrien.games.bagl.scene.Scene;
import com.adrien.games.bagl.scene.components.*;
import com.adrien.games.bagl.utils.FileUtils;
import com.adrien.games.bagl.utils.MathUtils;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
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
        private Mesh pointBulb;
        private Mesh spotBulb;

        private Model gltf;

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

            this.camera = new Camera(new Vector3f(4f, 2.5f, 3f), new Vector3f(-4f, -2.5f, -3f), new Vector3f(0, 1, 0),
                    (float) Math.toRadians(60f), (float) this.width / (float) this.height, 0.1f, 1000);
            this.cameraController = new FPSCameraController(this.camera);

            this.scene = new Scene();
            this.loadMeshes();
            this.initScene();

            this.spritebatch = new Spritebatch(1024, this.width, this.height);
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
            this.pointBulb.destroy();
            this.spotBulb.destroy();
            this.gltf.destroy();
        }

        private void loadMeshes() {
            this.environmentMap = this.environmentMapGenerator.generateEnvironmentMap(FileUtils.getResourceAbsolutePath("/envmaps/flat.hdr"));
//            this.environmentMap = this.environmentMapGenerator.generateEnvironmentMap("D:/Images/HDRI/lookout.hdr");
            this.irradianceMap = this.environmentMapGenerator.generateIrradianceMap(this.environmentMap);
            this.preFilteredMap = this.environmentMapGenerator.generatePreFilteredMap(this.environmentMap);

            this.floor = ModelFactory.fromFile(FileUtils.getResourceAbsolutePath("/models/floor/floor.obj"));
            this.cube = ModelFactory.fromFile(FileUtils.getResourceAbsolutePath("/models/cube/cube.obj"));
//            this.cube = ModelFactory.fromFile("D:/Documents/3D Models/gun/gun.obj");
            final Material gold = Material.builder().diffuse(Color.YELLOW).metallic(1f).roughness(0.1f).build();
            this.sphere = ModelFactory.createSphere(0.5f, 25, 25, gold);
            this.pointBulb = MeshFactory.createSphere(0.1f, 8, 8);
            this.spotBulb = MeshFactory.createCylinder(0.1f, 0.065f, 0.2f, 12);
            this.gltf = new GltfLoader().load(FileUtils.getResourceAbsolutePath("/models/box/Box.gltf"));
        }

        private void initScene() {
            this.scene.getRoot().addComponent(new EnvironmentComponent(this.environmentMap, this.irradianceMap, this.preFilteredMap));

            final GameObject cameraObj = this.scene.getRoot().createChild("camera");
            cameraObj.addComponent(new CameraComponent(this.camera));

            final GameObject floorObj = this.scene.getRoot().createChild("floor");
            floorObj.addComponent(new ModelComponent(this.floor));
            floorObj.getLocalTransform().setScale(new Vector3f(2f, 2f, 2f));

            final GameObject cubeObj = floorObj.createChild("cube");
            cubeObj.addComponent(new ModelComponent(this.cube));
            cubeObj.getLocalTransform().setTranslation(new Vector3f(0f, 0.5f, 0f));

            final GameObject sphereObj = floorObj.createChild("sphere");
            sphereObj.addComponent(new ModelComponent(this.sphere));
            sphereObj.getLocalTransform().setTranslation(new Vector3f(1.5f, 0.6f, 0f));

            final GameObject gltfObj = floorObj.createChild("gltf");
            gltfObj.addComponent(new ModelComponent(this.gltf));
            gltfObj.getLocalTransform().setTranslation(new Vector3f(-1.5f, 0.5f, 0f));

            this.setUpLights();
        }

        private void setUpLights() {
            final GameObject sunObj = this.scene.getRoot().createChild("sun");
            sunObj.getLocalTransform().setRotation(new Quaternionf().rotation((float) Math.toRadians(45f), (float) Math.toRadians(45f), 0));
            sunObj.addComponent(new DirectionalLightComponent(new DirectionalLight(0.8f, Color.WHITE, new Vector3f())));

            final GameObject parent = this.scene.getObjectById("floor").orElseThrow(() -> new EngineException("No floor found in the scene"));

            final PointLight pointLight0 = new PointLight(8f, Color.GREEN, new Vector3f(), 3f);
            this.addPointLight(parent, new Vector3f(4f, 0.5f, 2f), pointLight0, 0);
            final PointLight pointLight1 = new PointLight(10f, Color.YELLOW, new Vector3f(), 2f);
            this.addPointLight(parent, new Vector3f(-4f, 0.2f, 2f), pointLight1, 1);
            final PointLight pointLight2 = new PointLight(10f, Color.BLUE, new Vector3f(), 2f);
            this.addPointLight(parent, new Vector3f(0f, 0.5f, 3f), pointLight2, 2);
            final PointLight pointLight3 = new PointLight(10f, Color.TURQUOISE, new Vector3f(), 2f);
            this.addPointLight(parent, new Vector3f(-1f, 0.1f, 1f), pointLight3, 3);
            final PointLight pointLight4 = new PointLight(10f, Color.CYAN, new Vector3f(), 2f);
            this.addPointLight(parent, new Vector3f(3f, 0.6f, -3f), pointLight4, 4);

            final SpotLight spotLight0 = new SpotLight(10f, Color.RED, new Vector3f(), 20f, new Vector3f(), 20f, 5f);
            this.addSpotLight(parent, new Vector3f(-3, 1f, -2), new Quaternionf().rotationX((float) Math.toRadians(45f)), spotLight0, 0);
            final SpotLight spotLight1 = new SpotLight(2f, Color.WHITE, new Vector3f(), 7f, new Vector3f(), 10f, 4f);
            this.addSpotLight(parent, new Vector3f(2f, 2f, 2f), new Quaternionf().rotationX((float) Math.toRadians(90f)), spotLight1, 1);
        }

        private void addPointLight(final GameObject parent, final Vector3f position, final PointLight light, final int id) {
            final String lightId = "point_light_" + id;
            final GameObject lightObject = this.createLightObject(parent, position, new Quaternionf(), light.getColor(), this.pointBulb, lightId);
            lightObject.addComponent(new PointLightComponent(light));
        }

        private void addSpotLight(final GameObject parent, final Vector3f position, final Quaternionf rotation, final SpotLight light, final int id) {
            final String lightId = "spot_light_" + id;
            final GameObject lightObject = this.createLightObject(parent, position, rotation, light.getColor(), this.spotBulb, lightId);
            lightObject.addComponent(new SpotLightComponent(light));
        }

        private GameObject createLightObject(final GameObject parent, final Vector3f position, final Quaternionf rotation, final Color color,
                                             final Mesh mesh, final String id) {
            final GameObject lightObject = parent.createChild("object_" + id, "light");
            lightObject.getLocalTransform().setTranslation(position).setRotation(rotation);

            final GameObject modelObject = lightObject.createChild("bulb_" + id);
            modelObject.getLocalTransform().setRotation(new Quaternionf().rotationX((float) Math.toRadians(-90f)));

            final Material material = Material.builder().emissive(color).emissiveIntensity(10f).build();
            modelObject.addComponent(new ModelComponent(new Model().addMesh(mesh, material)));

            return lightObject;
        }

        @Override
        public void update(final Time time) {
            this.scene.update(time);

            if (Input.isKeyPressed(GLFW.GLFW_KEY_1) || Input.isKeyPressed(GLFW.GLFW_KEY_2)) {
                float speed = Input.isKeyPressed(GLFW.GLFW_KEY_1) ? 20 : -20;
                this.scene.getObjectById("sun").ifPresent(sunObj -> {
                    final Transform transform = new Transform()
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

            final long lightCount = this.scene.getObjectsByTag("light").count();
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
