package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.*;
import com.adrien.games.bagl.core.math.Quaternion;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.particles.Particle;
import com.adrien.games.bagl.rendering.particles.ParticleEmitter;
import com.adrien.games.bagl.rendering.particles.ParticleRenderer;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureParameters;
import com.adrien.games.bagl.utils.FileUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

public class ParticlesSample implements Game {

    private static final String TITLE = "Particles";

    private int xRes;
    private int yRes;
    private Camera camera;
    private Time timer;

    private ParticleEmitter snowEmitter;
    private ParticleEmitter explosionEmitter;
    private ParticleRenderer renderer;
    private Texture texture;

    private final Consumer<Particle> exploder = p -> p.reset(new Vector3(0, 0, 0),
            new Vector3((float)Math.random()*2 - 1, (float)Math.random()*2 - 1, (float)Math.random()*2 - 1),
            0.1f,10f, Color.WHITE,3f);

    private final Consumer<Particle> snow = p -> p.reset(
            new Vector3((float)Math.random()*20 - 10, 10, (float)Math.random()*20 - 10),
            new Vector3(0, -1, 0),0.4f, 1f, Color.WHITE,10f);

    @Override
    public void init() {
        this.xRes = Configuration.getInstance().getXResolution();
        this.yRes = Configuration.getInstance().getYResolution();

        this.camera = new Camera(new Vector3(0, 0, 20), new Vector3(0, 0, -1), Vector3.UP,
                (float)Math.toRadians(60), (float)xRes/yRes, 0.1f, 100f);
        this.timer = new Time();

        this.texture = new Texture(FileUtils.getResourceAbsolutePath("/snowflake.png"), new TextureParameters());
        this.snowEmitter = new ParticleEmitter(this.texture, 0.5f, 10, this.snow);
        this.explosionEmitter = new ParticleEmitter(0.1f, 100, this.exploder);
        this.renderer = new ParticleRenderer();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    @Override
    public void update(Time time) {
        final float elapsedTime = time.getElapsedTime();

        this.timer.update();
        this.snowEmitter.update(time);
        System.out.println("Updating snow particles: " + this.timer.getElapsedTime());

        this.timer.update();
        this.explosionEmitter.update(time);
        System.out.println("Updating explotion particles: " + this.timer.getElapsedTime());

        if(Input.isKeyPressed(GLFW.GLFW_KEY_LEFT)) {
            this.camera.rotate(Quaternion.fromAngleAndVector((float)Math.toRadians(10*elapsedTime), Vector3.UP));
        } else if(Input.isKeyPressed(GLFW.GLFW_KEY_RIGHT)) {
            this.camera.rotate(Quaternion.fromAngleAndVector((float)Math.toRadians(-10*elapsedTime), Vector3.UP));
        }

    }

    @Override
    public void render() {
        this.timer.update();
        this.renderer.render(this.snowEmitter, this.camera);
        System.out.println("Rendering snow particles: " + this.timer.getElapsedTime());

        this.timer.update();
        this.renderer.render(this.explosionEmitter, this.camera);
        System.out.println("Rendering explosion particles: " + this.timer.getElapsedTime());
    }

    @Override
    public void destroy() {
        this.renderer.destroy();
        this.texture.destroy();
    }

    public static void main(String[] args) {
        new Engine(new ParticlesSample(), TITLE).start();
    }

}
