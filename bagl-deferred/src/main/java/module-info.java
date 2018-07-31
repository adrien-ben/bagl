module com.adrienben.games.bagl.deferred {
    // REQUIRES
    requires java.base;
    requires com.adrienben.games.bagl.engine;

    // EXPORTS
    exports com.adrienben.games.bagl.deferred;

    // OPENS
    opens shaders.deferred;
    opens shaders.shadow;
}
