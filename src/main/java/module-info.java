module com.adrienben.games.bagl {
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
    exports com.adrienben.games.bagl.core;
    exports com.adrienben.games.bagl.core.math;
    exports com.adrienben.games.bagl.core.exception;
    exports com.adrienben.games.bagl.core.io;
    exports com.adrienben.games.bagl.core.utils;
    exports com.adrienben.games.bagl.core.validation;

    exports com.adrienben.games.bagl.opengl;
    exports com.adrienben.games.bagl.opengl.shader;
    exports com.adrienben.games.bagl.opengl.texture;
    exports com.adrienben.games.bagl.opengl.vertex;

    exports com.adrienben.games.bagl.engine;
    exports com.adrienben.games.bagl.engine.camera;
    exports com.adrienben.games.bagl.engine.game;
    exports com.adrienben.games.bagl.engine.scene;
    exports com.adrienben.games.bagl.engine.scene.components;
    exports com.adrienben.games.bagl.engine.rendering;
    exports com.adrienben.games.bagl.engine.rendering.environment;
    exports com.adrienben.games.bagl.engine.rendering.light;
    exports com.adrienben.games.bagl.engine.rendering.model;
    exports com.adrienben.games.bagl.engine.rendering.particles;
    exports com.adrienben.games.bagl.engine.rendering.postprocess;
    exports com.adrienben.games.bagl.engine.rendering.postprocess.fxaa;
    exports com.adrienben.games.bagl.engine.rendering.postprocess.steps;
    exports com.adrienben.games.bagl.engine.rendering.renderer;
    exports com.adrienben.games.bagl.engine.rendering.shape;
    exports com.adrienben.games.bagl.engine.rendering.sprite;
    exports com.adrienben.games.bagl.engine.rendering.text;
    exports com.adrienben.games.bagl.engine.assets;
    exports com.adrienben.games.bagl.engine.resource;
    exports com.adrienben.games.bagl.engine.resource.asset;
    exports com.adrienben.games.bagl.engine.resource.asset.json;
    exports com.adrienben.games.bagl.engine.resource.scene;
    exports com.adrienben.games.bagl.engine.resource.scene.json;


    exports com.adrienben.games.bagl.deferred;

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
