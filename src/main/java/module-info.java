module com.adrien.games.bagl {
    // REQUIRES
	requires java.base;
	requires transitive org.joml;
	requires transitive org.apache.logging.log4j;
	requires org.lwjgl;
	requires org.lwjgl.natives;
	requires transitive org.lwjgl.glfw;
	requires transitive org.lwjgl.glfw.natives;
	requires org.lwjgl.jemalloc;
	requires org.lwjgl.jemalloc.natives;
	requires transitive org.lwjgl.opengl;
	requires transitive org.lwjgl.opengl.natives;
	requires org.lwjgl.stb;
	requires org.lwjgl.stb.natives;
	requires gltf.loader;
	requires gson;
	requires java.sql;

    // EXPORTS
	exports com.adrien.games.bagl.core;
	exports com.adrien.games.bagl.core.math;
    exports com.adrien.games.bagl.core.exception;
    exports com.adrien.games.bagl.core.io;
    exports com.adrien.games.bagl.core.utils;
    exports com.adrien.games.bagl.core.validation;

    exports com.adrien.games.bagl.opengl;
    exports com.adrien.games.bagl.opengl.shader;
    exports com.adrien.games.bagl.opengl.texture;
    exports com.adrien.games.bagl.opengl.vertex;

    exports com.adrien.games.bagl.engine;
    exports com.adrien.games.bagl.engine.camera;
    exports com.adrien.games.bagl.engine.game;
    exports com.adrien.games.bagl.engine.scene;
    exports com.adrien.games.bagl.engine.scene.components;
    exports com.adrien.games.bagl.engine.rendering;
    exports com.adrien.games.bagl.engine.rendering.environment;
    exports com.adrien.games.bagl.engine.rendering.light;
    exports com.adrien.games.bagl.engine.rendering.model;
    exports com.adrien.games.bagl.engine.rendering.particles;
    exports com.adrien.games.bagl.engine.rendering.postprocess;
    exports com.adrien.games.bagl.engine.rendering.postprocess.fxaa;
    exports com.adrien.games.bagl.engine.rendering.postprocess.steps;
    exports com.adrien.games.bagl.engine.rendering.renderer;
    exports com.adrien.games.bagl.engine.rendering.shape;
    exports com.adrien.games.bagl.engine.rendering.sprite;
    exports com.adrien.games.bagl.engine.rendering.text;
    exports com.adrien.games.bagl.engine.assets;
    exports com.adrien.games.bagl.engine.resource;
    exports com.adrien.games.bagl.engine.resource.asset;
    exports com.adrien.games.bagl.engine.resource.asset.json;
    exports com.adrien.games.bagl.engine.resource.scene;
    exports com.adrien.games.bagl.engine.resource.scene.json;


    exports com.adrien.games.bagl.deferred;

    // OPENS
	opens shaders.common;
    opens shaders.ui;
    opens shaders.deferred;
    opens shaders.environment;
    opens shaders.particles;
    opens shaders.post;
    opens shaders.shadow;
    opens shaders.sprite;
}
