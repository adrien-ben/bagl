module com.adrien.games.bagl {
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

	exports com.adrien.games.bagl.core;
	exports com.adrien.games.bagl.core.camera;
	exports com.adrien.games.bagl.core.math;
	exports com.adrien.games.bagl.rendering;
	exports com.adrien.games.bagl.rendering.light;
	exports com.adrien.games.bagl.rendering.vertex;
	exports com.adrien.games.bagl.rendering.texture;
	exports com.adrien.games.bagl.resource;
	exports com.adrien.games.bagl.scene;
	exports com.adrien.games.bagl.utils;

    // Open resource folders
    opens shaders.ui;
    opens shaders.deferred;
    opens shaders.environment;
    opens shaders.particles;
    opens shaders.post;
    opens shaders.shadow;
    opens shaders.sprite;
    opens envmaps;
    opens fonts.arial;
    opens fonts.segoe;
    opens models.floor;
    opens models.helmet;
    opens scenes;
}
