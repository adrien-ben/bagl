module com.adrienben.games.bagl.engine {
    // REQUIRES
	requires java.base;
	requires org.lwjgl;
	requires org.lwjgl.natives;
	requires transitive org.lwjgl.glfw;
	requires transitive org.lwjgl.glfw.natives;
	requires org.lwjgl.jemalloc;
	requires org.lwjgl.jemalloc.natives;
	requires gltf.loader;
	requires gson;
	requires java.sql;

    requires transitive com.adrienben.games.bagl.opengl;

    // EXPORTS
    exports com.adrienben.games.bagl.engine;
    exports com.adrienben.games.bagl.engine.animation;
    exports com.adrienben.games.bagl.engine.camera;
    exports com.adrienben.games.bagl.engine.game;
    exports com.adrienben.games.bagl.engine.scene;
    exports com.adrienben.games.bagl.engine.scene.components;
    exports com.adrienben.games.bagl.engine.rendering;
    exports com.adrienben.games.bagl.engine.rendering.environment;
    exports com.adrienben.games.bagl.engine.rendering.light;
    exports com.adrienben.games.bagl.engine.rendering.material;
    exports com.adrienben.games.bagl.engine.rendering.model;
    exports com.adrienben.games.bagl.engine.rendering.particles;
    exports com.adrienben.games.bagl.engine.rendering.postprocess;
    exports com.adrienben.games.bagl.engine.rendering.postprocess.fxaa;
    exports com.adrienben.games.bagl.engine.rendering.postprocess.steps;
    exports com.adrienben.games.bagl.engine.rendering.renderer;
    exports com.adrienben.games.bagl.engine.rendering.shaders;
    exports com.adrienben.games.bagl.engine.rendering.shape;
    exports com.adrienben.games.bagl.engine.rendering.sprite;
    exports com.adrienben.games.bagl.engine.rendering.text;
    exports com.adrienben.games.bagl.engine.assets;
    exports com.adrienben.games.bagl.engine.resource.asset;
    exports com.adrienben.games.bagl.engine.resource.asset.json;
    exports com.adrienben.games.bagl.engine.resource.gltf;
    exports com.adrienben.games.bagl.engine.resource.scene;
    exports com.adrienben.games.bagl.engine.resource.scene.json;

    // OPENS
	opens shaders.common;
    opens shaders.ui;
    opens shaders.environment;
    opens shaders.particles;
    opens shaders.post;
    opens shaders.sprite;

    opens com.adrienben.games.bagl.engine.resource.asset.json to gson;
    opens com.adrienben.games.bagl.engine.resource.scene.json to gson;
}
