#version 330

#import "classpath:/shaders/common/material.glsl"

in vec2 passCoords;
in vec3 passNormal;
in mat3 passTBN;

layout (location = 0) out vec4 colors;
layout (location = 1) out vec4 normals;
layout (location = 2) out vec3 emissive;

uniform Material uMaterial;

void main() {
    // discard transparent pixels
    if(isTransparent(uMaterial, passCoords)) {
        discard;
    }

    // diffuse color
    colors.rgb = uMaterial.diffuseColor.rgb;
    if(uMaterial.hasDiffuseMap) {
        colors.rgb *= pow(texture2D(uMaterial.diffuseMap, passCoords).rgb, vec3(2.2));
    }

    // roughness
    float roughness = uMaterial.roughness;
    if(uMaterial.hasRoughnessMetallicMap) {
        roughness *= texture2D(uMaterial.roughnessMetallicMap, passCoords).g;
    }
    colors.a = clamp(roughness, 0.03, 1.0);

    // normals
	vec3 normal;
	if(uMaterial.hasNormalMap) {
		normal = normalize(texture2D(uMaterial.normalMap, passCoords).rgb*2 - 1);
		normal = normalize(passTBN*normal);
	} else {
		normal = normalize(passNormal);
	}
	if(!gl_FrontFacing) {
	    normal *= -1;
    }

	normals.rgb = normal*0.5 + 0.5;

    // metallic
	normals.a = uMaterial.metallic;
	if(uMaterial.hasRoughnessMetallicMap) {
	    normals.a *= texture2D(uMaterial.roughnessMetallicMap, passCoords).b;
	}

    // emissive color
    emissive = uMaterial.emissiveColor.rgb*uMaterial.emissiveIntensity;
    if(uMaterial.hasEmissiveMap) {
        emissive *= pow(texture2D(uMaterial.emissiveMap, passCoords).rgb, vec3(2.2));
    }
}
