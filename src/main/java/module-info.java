module com.adrien.games.bagl {
	requires java.base;
	requires org.joml;
	requires org.apache.logging.log4j;
	requires org.lwjgl;
	requires org.lwjgl.natives;
	requires org.lwjgl.glfw;
	requires org.lwjgl.glfw.natives;
	requires org.lwjgl.jemalloc;
	requires org.lwjgl.jemalloc.natives;
	requires org.lwjgl.opengl;
	requires org.lwjgl.opengl.natives;
	requires org.lwjgl.stb;
	requires org.lwjgl.stb.natives;
	requires gltf.loader;

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
}
