package com.adrienben.games.bagl.engine;

import com.adrienben.games.bagl.engine.game.Game;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;

public final class Engine {

    private static final Logger log = LogManager.getLogger(Engine.class);

    private final Game game;
    private final Window window;
    private final Time time;
    private boolean isRunning;

    public Engine(final Game game, final String title) {
        log.info("Initializing engine");
        if (Objects.isNull(game)) {
            throw new IllegalArgumentException("The argument game cannot be null.");
        }
        final var configuration = Configuration.getInstance();
        this.game = game;
        this.window = new Window(title,
                configuration.getXResolution(),
                configuration.getYResolution(),
                configuration.getVsync(),
                configuration.getFullscreen());
        this.initGlState();
        this.time = new Time();
        this.isRunning = false;
        this.game.init();
    }

    private void initGlState() {
        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }

    public void start() {
        try {
            log.info("Starting engine");
            this.isRunning = true;
            this.time.update();
            while (this.isRunning) {
                if (this.window.isCloseRequested()) {
                    this.stop();
                }
                this.update();
                this.render();
                Input.update();
                this.window.update();
            }
            this.destroy();
        } catch (final RuntimeException exception) {
            log.error("An unexpected fatal error occurred during the execution of the app", exception);
            System.exit(1);
        }
    }

    private void update() {
        this.time.update();
        this.game.update(this.time);
    }

    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        this.game.render();
    }

    public void stop() {
        log.info("Stopping engine");
        this.isRunning = false;
    }

    private void destroy() {
        log.info("Destroying engine");
        this.game.destroy();
        this.window.destroy();
    }

}
