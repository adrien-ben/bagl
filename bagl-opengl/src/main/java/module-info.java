module com.adrienben.games.bagl.opengl {
    // REQUIRES
    requires java.base;
    requires org.lwjgl;
    requires org.lwjgl.natives;
    requires org.lwjgl.jemalloc;
    requires org.lwjgl.jemalloc.natives;
    requires transitive org.lwjgl.opengl;
    requires transitive org.lwjgl.opengl.natives;
    requires org.lwjgl.stb;
    requires org.lwjgl.stb.natives;
    requires java.sql;

    requires transitive com.adrienben.games.bagl.core;

    // EXPORTS
    exports com.adrienben.games.bagl.opengl;
    exports com.adrienben.games.bagl.opengl.shader;
    exports com.adrienben.games.bagl.opengl.texture;
    exports com.adrienben.games.bagl.opengl.vertex;
}
