#version 330

#import "classpath:/shaders/common/material.glsl"
#import "classpath:/shaders/common/lights.glsl"
#import "classpath:/shaders/common/maths.glsl"
#import "classpath:/shaders/common/camera.glsl"
#import "classpath:/shaders/pbr/pbr.glsl"
#import "classpath:/shaders/shadow/shadow.glsl"

in vec4 passPosition;
in vec2 passCoords;
in vec3 passNormal;
in mat3 passTBN;

out vec4 finalColor;

uniform Material uMaterial;
uniform Camera uCamera;
uniform Lights uLights;
uniform Environment uEnvironment;
uniform Shadow uShadow;

void main() {
    // discard transparent pixels
    if(isTransparent(uMaterial, passCoords)) {
        discard;
    }

    // Material properties
    vec4 color = getDiffuseColor(uMaterial, passCoords);
    float roughness = getRoughness(uMaterial, passCoords);
    float metallic = getMetallic(uMaterial, passCoords);
    vec3 emissive = getEmissiveColor(uMaterial, passCoords);
    float occlusionStrength = getOcclusionStrength(uMaterial);
    float occlusionValue = getOcclusionValue(uMaterial, passCoords);

    float linearDepth = linearizeDepth(gl_FragCoord.z, uShadow.zNear, uShadow.zFar);

    // Surface normal
    vec3 N = getNormal(uMaterial, passNormal, passTBN, passCoords);

    //View vector
    vec3 V = normalize(uCamera.position - passPosition.xyz);

    //Reflected view vector
    vec3 R = reflect(-V, N);

    //N.V
    float NdotV = max(dot(N, V), 0.0);

    //base reflexivity
    vec3 F0 = mix(vec3(0.04), color.rgb, metallic);

    //Ambient term
    vec3 ambient = computeIBL(uEnvironment, color.rgb, F0, NdotV, N, R, roughness, metallic);
    ambient = mix(ambient, ambient*occlusionValue, occlusionStrength);

    vec3 L0 = vec3(0.0);

    //directional lights
    int directionalCount = min(uLights.directionalCount, MAX_DIR_LIGHTS);
    for(int i = 0; i < directionalCount; i++) {
        //light direction
        vec3 L = normalize(-uLights.directionals[i].direction);
        float lightIntensity = uLights.directionals[i].base.intensity;
        if(i == 0 && uShadow.hasShadow) {
            float shadowMapDepthTest = computeShadow(uShadow, linearDepth, passPosition);
            lightIntensity *= shadowMapDepthTest;
        }

        L0 += computeLight(uLights.directionals[i].base.color.rgb, lightIntensity, 1.0, L, V, N, NdotV, F0, color.rgb, roughness, metallic);
    }

    //point lights
    int pointCount = min(uLights.pointCount, MAX_POINT_LIGHTS);
    for(int i = 0; i < pointCount; i++) {
        vec3 lightDirection = uLights.points[i].position - passPosition.xyz;
        float distance = length(lightDirection);
        if(distance > uLights.points[i].radius) {
            continue;
        }

        vec3 L = normalize(lightDirection);
        float attenuation = computeFalloff(distance, uLights.points[i].radius);

        L0 += computeLight(uLights.points[i].base.color.rgb, uLights.points[i].base.intensity, attenuation, L, V, N, NdotV, F0, color.rgb, roughness, metallic);
    }

    //spot lights
    int spotCount = min(uLights.spotCount, MAX_SPOT_LIGHTS);
    for(int i = 0; i < spotCount; i++) {
        vec3 lightDirection = uLights.spots[i].point.position - passPosition.xyz;
        float distance = length(lightDirection);
        vec3 L = normalize(lightDirection);
        float theta = dot(-normalize(uLights.spots[i].direction), L);

        if(theta <= uLights.spots[i].outerCutOff || distance > uLights.spots[i].point.radius) {
            continue;
        }

        float attenuation = computeFalloff(distance, uLights.spots[i].point.radius);

        float epsilon = uLights.spots[i].cutOff - uLights.spots[i].outerCutOff;
        float falloff = clamp((theta - uLights.spots[i].outerCutOff)/epsilon, 0, 1);

        L0 += computeLight(uLights.spots[i].point.base.color.rgb, uLights.spots[i].point.base.intensity, attenuation*falloff, L, V, N, NdotV, F0, color.rgb, roughness, metallic);
    }

    finalColor = vec4((ambient + L0 + emissive), color.a);
}
