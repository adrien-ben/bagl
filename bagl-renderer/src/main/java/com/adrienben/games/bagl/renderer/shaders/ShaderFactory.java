package com.adrienben.games.bagl.renderer.shaders;

import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.opengl.shader.Shader;

public final class ShaderFactory {

    private ShaderFactory() {
    }

    public static Shader createSkyboxShader() {
        return Shader.builder()
                .vertexPath(ResourcePath.get("classpath:/shaders/environment/environment.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/environment/environment_cubemap_sample.frag"))
                .build();
    }

    public static Shader createShadowShader() {
        return Shader.builder()
                .vertexPath(ResourcePath.get("classpath:/shaders/shadow/shadow_map.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/shadow/shadow_map.frag"))
                .build();
    }

    public static Shader createGBufferShader() {
        return Shader.builder()
                .vertexPath(ResourcePath.get("classpath:/shaders/deferred/gbuffer.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/deferred/gbuffer.frag"))
                .build();
    }

    public static Shader createDeferredShader() {
        return Shader.builder()
                .vertexPath(ResourcePath.get("classpath:/shaders/deferred/deferred.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/deferred/deferred.frag"))
                .build();
    }

    public static Shader createForwardShader() {
        return Shader.builder()
                .vertexPath(ResourcePath.get("classpath:/shaders/forward/forward.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/forward/forward.frag"))
                .build();
    }

    public static Shader createBrdfShader() {
        return Shader.builder()
                .vertexPath(ResourcePath.get("classpath:/shaders/post/post_process.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/environment/brdf_integration.frag"))
                .build();
    }
}
