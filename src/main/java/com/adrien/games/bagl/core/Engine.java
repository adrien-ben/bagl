package com.adrien.games.bagl.core;

import com.adrien.games.bagl.rendering.BlendMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public final class Engine {

    private static final Logger log = LogManager.getLogger(Engine.class);

    private Configuration configuration;
    private Game game;
    private Window window;
    private Time time;
    private boolean isRunning;

    public Engine(Game game, String title) {
        log.info("Initializing engine");
        if (game == null) {
            throw new IllegalArgumentException("The argument game cannot be null.");
        }
        this.configuration = Configuration.getInstance();
        this.game = game;
        this.window = new Window(title,
                this.configuration.getXResolution(),
                this.configuration.getYResolution(),
                this.configuration.getVsync(),
                this.configuration.getFullscreen());
        this.time = new Time();
        this.isRunning = false;
        this.game.init();
    }

    public void start() {
        log.info("Starting engine");
        this.isRunning = true;
        while (this.isRunning) {
            if (this.window.isCloseRequested()) {
                stop();
            }
            this.update();
            this.render();
            this.window.update();
        }
        this.destroy();
    }

    private void update() {
        this.time.update();
        this.game.update(this.time);
    }

    private void render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        this.game.render();
    }

    public void stop() {
        log.info("Stopping engine");
        this.isRunning = false;
    }

    /**
     * Sets the color to une when clear the color buffer. This color is only applied to the currently
     * bound framebuffer. The engine must have been started first.
     * @param color The clear color.
     */
    public static void setClearColor(Color color) {
        GL11.glClearColor(color.getRed(), color.getGreen(), color.getBlue(), 1);
    }

    /**
     * Sets the blend mode for the current rendering context.
     * @param blendMode The blend mode to apply.
     */
    public static void setBlendMode(BlendMode blendMode) {
        if(blendMode == BlendMode.NONE) {
            GL11.glDisable(GL11.GL_BLEND);
        } else {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(blendMode.getGlSource(), blendMode.getGlDestination());
        }
    }

    private void destroy() {
        log.info("Destroying engine");
        game.destroy();
        window.destroy();
    }

}
