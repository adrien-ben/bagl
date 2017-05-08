package com.adrien.games.bagl.sample;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.BufferUsage;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.vertex.Vertex;
import com.adrien.games.bagl.rendering.vertex.VertexDescription;
import com.adrien.games.bagl.rendering.vertex.VertexElement;

public class GeometryShaderParticlesSample implements Game {

	private static final String TITLE = "Geometry Shader Particles";
	private static final int MAX_PARTICLES_COUNT = 10000;
	
	/**
	 * Particle vertex class.
	 *
	 */
	public static class ParticleVertex implements Vertex {

		private Vector3 position;
		private Color color;
		private float size;
		
		public ParticleVertex(Vector3 position, Color color, float size) {
			this.position = position;
			this.color = color;
			this.size = size;
		}
		
		@Override
		public float[] getData() {
			return new float[] {this.position.getX(), this.position.getY(), this.position.getZ(), this.color.getRed(), 
					this.color.getGreen(), this.color.getBlue(), this.size};
		}
		
		public static VertexDescription getDescription() {
			return new VertexDescription(new VertexElement[]{ new VertexElement(0, 3, 0), new VertexElement(1, 3, 3), new VertexElement(2, 1, 6)});
		}
		
	}
	
	/**
	 * Particle class.
	 *
	 */
	public static class Particle {
		
		public Vector3 position;
		public Vector3 direction;
		public Color color;
		public float size;
		public float ttl;
		public boolean alive;
		
		public Particle() {
			this.position = new Vector3();
			this.direction = new Vector3();
			this.color = Color.WHITE;
			this.size = 1f;
			this.alive = false;
		}
		
		public void activate(Vector3 position, Color color, float size, float ttl) {
			this.position = position;
			this.direction = new Vector3((float)Math.random()*2 - 1, (float)Math.random()*2 - 1, (float)Math.random()*2 - 1);
			this.direction.normalise();
			this.color = color;
			this.size = size;
			this.ttl = ttl;
			this.alive = true;
		}
		
		public void update(Time t) {
			final Vector3 v = new Vector3(this.direction);
			v.scale(t.getElapsedTime());
			this.position.add(v);
			this.size *= 1 - 0.8*t.getElapsedTime();
		}
		
	}
	
	private int xRes;
	private int yRes;
	private Shader particleShader;
	private VertexBuffer vbuffer;
	private ParticleVertex[] vertices;
	private Camera camera;
	private List<Particle> particles;
	private int liveParticles;
	private float popRate = 0.2f;
	private float timeToNextPop = 0;
	private int popSize = 200;
	
	@Override
	public void init() {
		this.xRes = Configuration.getInstance().getXResolution();
		this.yRes = Configuration.getInstance().getYResolution();
		this.particleShader = new Shader()
				.addVertexShader("particles.vert")
				.addFragmentShader("particles.frag")
				.addGeometryShader("particles.geom")
				.compile();
		
		this.vertices = new ParticleVertex[MAX_PARTICLES_COUNT];
		for(int i = 0; i < MAX_PARTICLES_COUNT; i++) {
			this.vertices[i] = new ParticleVertex(new Vector3(), Color.WHITE, 1f);
		}
		this.vbuffer = new VertexBuffer(ParticleVertex.getDescription(), BufferUsage.STREAM_DRAW, this.vertices);
		
		this.camera = new Camera(new Vector3(5, 0, 5), new Vector3(-1, 0, -1), Vector3.UP, 
				(float)Math.toRadians(60), (float)xRes/yRes, 0.1f, 100f);
		this.particles = new ArrayList<>();
		for(int i = 0; i < MAX_PARTICLES_COUNT; i++) {
			this.particles.add(new Particle());
		}
		this.liveParticles = 0;
	}

	@Override
	public void destroy() {
		this.particleShader.destroy();
		this.vbuffer.destroy();
	}

	@Override
	public void update(Time time) {
		final float elapsedTime = time.getElapsedTime();
		this.timeToNextPop -= elapsedTime;
		if(this.timeToNextPop <= 0) {
			this.timeToNextPop = this.popRate;
			int poped = 0;
			for(Particle p : particles) {
				if(poped == this.popSize) {
					return;
				}
				if(!p.alive) {
					p.activate(new Vector3(0, 0, 0), Color.RED, 0.1f, 3);
					poped++;
				}
			}
		}
		
		this.liveParticles = 0;
		for(Particle p : this.particles) {
			if(p.alive) {
				p.ttl -= elapsedTime;
				if(p.ttl < 0) {
					p.alive = false;
				} else {
					p.update(time);
					final ParticleVertex particleVertex = this.vertices[this.liveParticles];
					particleVertex.position = p.position;
					particleVertex.color = p.color;
					particleVertex.size = p.size;
					this.liveParticles++;
				}
			}
		}
	}

	@Override
	public void render() {
		this.particleShader.bind();
		this.particleShader.setUniform("camera.view", this.camera.getView());
		this.particleShader.setUniform("camera.viewProj", this.camera.getViewProj());
		this.vbuffer.bind();
		
		this.vbuffer.setData(this.vertices, this.liveParticles);
		GL11.glDrawArrays(GL11.GL_POINTS, 0, this.liveParticles);
		
		VertexBuffer.unbind();
		Shader.unbind();
	}

	public static void main(String[] args) {
		new Engine(new GeometryShaderParticlesSample(), TITLE).start();
	}
	
}
