package com.adrien.games.bagl.sample;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPointSize;

import java.util.Optional;

import org.lwjgl.glfw.GLFW;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Input;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.math.Quaternion;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.Mesh;
import com.adrien.games.bagl.rendering.Renderer;
import com.adrien.games.bagl.rendering.Skybox;
import com.adrien.games.bagl.rendering.Spritebatch;
import com.adrien.games.bagl.rendering.light.Attenuation;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.light.Light;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.rendering.scene.Scene;
import com.adrien.games.bagl.rendering.scene.SceneNode;
import com.adrien.games.bagl.utils.FileUtils;
import com.adrien.games.bagl.utils.MeshFactory;

public class DeferredRenderingSample {

	private static final class TestGame implements Game {
		
		private static final String TITLE = "Deferred Rendering";
				
		private int width;
		private int height;
		
		private Renderer renderer;
		
		private Scene scene;
		private Skybox skybox;
		private Mesh floor;
		private Mesh sphere;
		
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
			this.sphere = MeshFactory.fromResourceFile("/models/sphere/sphere.obj");
		}
		
		private void initSceneGraph() {
			this.scene.getRoot().set(this.floor);
			final SceneNode<Mesh> sphereNode = new SceneNode<Mesh>(this.sphere);
			sphereNode.getLocalTransform().setTranslation(new Vector3(0, 0.5f, 0));
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
			final Optional<SceneNode<Mesh>> node = this.scene.getRoot().getChildren().stream().findFirst();
			if(node.isPresent()) {
				node.get().getLocalTransform().getRotation().mul(Quaternion.fromAngleAndVector(
						(float)Math.toRadians(10*time.getElapsedTime()), Vector3.UP));	
			}
			
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
