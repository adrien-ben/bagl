module com.adrienben.games.bagl.deferred {
    // REQUIRES
    requires java.base;
    requires transitive com.adrienben.games.bagl.engine;

    // EXPORTS
    exports com.adrienben.games.bagl.deferred;
    exports com.adrienben.games.bagl.deferred.gbuffer;

    // OPENS
    opens shaders.deferred;
    opens shaders.forward;
    opens shaders.pbr;
    opens shaders.shadow;
}
