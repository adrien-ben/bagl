module com.adrienben.games.bagl.core {
    // REQUIRES
    requires java.base;
    requires transitive org.joml;
    requires transitive org.apache.logging.log4j;
    requires org.lwjgl;
    requires org.lwjgl.natives;
    requires org.lwjgl.jemalloc;
    requires org.lwjgl.jemalloc.natives;
    requires org.lwjgl.stb;
    requires org.lwjgl.stb.natives;

    // EXPORTS
    exports com.adrienben.games.bagl.core;
    exports com.adrienben.games.bagl.core.math;
    exports com.adrienben.games.bagl.core.exception;
    exports com.adrienben.games.bagl.core.io;
    exports com.adrienben.games.bagl.core.utils;
    exports com.adrienben.games.bagl.core.utils.repository;
    exports com.adrienben.games.bagl.core.validation;

    // OPENS
    opens com.adrienben.games.bagl.core.math to com.google.gson;
}
