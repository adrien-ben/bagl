package com.adrien.games.bagl.sample;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Input;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.math.Quaternion;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.particles.ParticleRenderer;
import com.adrien.games.bagl.rendering.particles.Particle;
import com.adrien.games.bagl.rendering.particles.ParticleEmitter;

public class ParticlesSample implements Game {
	
	private static final String TITLE = "Particles";
	private static final Color PARTICLE_COLOR = new Color(1, 1, 1, 0.9f);
	
	private int xRes;
	private int yRes;
	private Camera camera;
	private Time timer;
	
	private ParticleEmitter emitter;
	private ParticleRenderer renderer;
	
	@Override
	public void init() {
		this.xRes = Configuration.getInstance().getXResolution();
		this.yRes = Configuration.getInstance().getYResolution();
		
		this.camera = new Camera(new Vector3(0, 0, 100), new Vector3(1, 0, -1), Vector3.UP, 
				(float)Math.toRadians(60), (float)xRes/yRes, 0.1f, 100f);
		this.timer = new Time();
		
		this.emitter = new ParticleEmitter(0.1f, 500, this::resetParticle);
		this.renderer = new ParticleRenderer();
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
	}
	
	private void resetParticle(Particle p) {
		p.reset(
				new Vector3(0, 0, 0), 
				new Vector3((float)Math.random()*2 - 1, (float)Math.random()*2 - 1, (float)Math.random()*2 - 1),
				30f,
				PARTICLE_COLOR,
				4f);
	}

	@Override
	public void update(Time time) {
		final float elapsedTime = time.getElapsedTime();
		
		this.timer.update();
		this.emitter.update(time);
		System.out.println("Updating particles: " + this.timer.getElapsedTime());
		
		if(Input.isKeyPressed(GLFW.GLFW_KEY_LEFT)) {
			this.camera.rotate(Quaternion.fromAngleAndVector((float)Math.toRadians(10*elapsedTime), Vector3.UP));
		} else if(Input.isKeyPressed(GLFW.GLFW_KEY_RIGHT)) {
			this.camera.rotate(Quaternion.fromAngleAndVector((float)Math.toRadians(-10*elapsedTime), Vector3.UP));
		}

	}

	@Override
	public void render() {
		this.timer.update();
		this.renderer.render(this.emitter, this.camera);
		System.out.println("Rendering particles: " + this.timer.getElapsedTime());
	}

	@Override
	public void destroy() {
		this.renderer.destroy();
	}
	
	public static void main(String[] args) {
		new Engine(new ParticlesSample(), TITLE).start();
	}
	
}
