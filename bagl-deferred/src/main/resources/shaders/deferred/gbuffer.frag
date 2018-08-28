#version 330

#import "classpath:/shaders/common/material.glsl"

in vec2 passCoords;
in vec3 passNormal;
in mat3 passTBN;

layout (location = 0) out vec4 colors;
layout (location = 1) out vec4 normals;
layout (location = 2) out vec3 emissive;
layout (location = 3) out vec2 occlusion;

uniform Material uMaterial;

void main() {
    // discard transparent pixels
    if(isTransparent(uMaterial, passCoords)) {
        discard;
    }

    // diffuse color
    colors.rgb = getDiffuseColor(uMaterial, passCoords).rgb;

    // roughness
    colors.a = getRoughness(uMaterial, passCoords);

    // normals
    normals.rgb = getNormal(uMaterial, passNormal, passTBN, passCoords)*0.5 + 0.5;

    // metallic
    normals.a = getMetallic(uMaterial, passCoords);

    // emissive color
    emissive = getEmissiveColor(uMaterial, passCoords);

    // occlusion
    occlusion.r = getOcclusionStrength(uMaterial);
    occlusion.g = getOcclusionValue(uMaterial, passCoords);
}
