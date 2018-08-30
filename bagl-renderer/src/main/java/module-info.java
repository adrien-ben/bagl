module com.adrienben.games.bagl.renderer {
    // REQUIRES
    requires java.base;
    requires transitive com.adrienben.games.bagl.engine;

    // EXPORTS
    exports com.adrienben.games.bagl.renderer;
    exports com.adrienben.games.bagl.renderer.gbuffer;

    // OPENS
    opens shaders.deferred;
    opens shaders.forward;
    opens shaders.pbr;
    opens shaders.shadow;
}
