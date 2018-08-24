#version 330

#import "classpath:/shaders/common/lights.glsl"
#import "classpath:/shaders/common/camera.glsl"

in GeomOut {
    vec4 position;
	vec4 color;
	vec2 coords;
} geomOut;

out vec4 color;

uniform sampler2D uTexture;
uniform bool hasTexture;
uniform Camera uCamera;
uniform Lights uLights;

float computeLightAmountForFace(vec3 N, vec3 L, float opacity) {
    float NdotL = dot(N, L);
    if(NdotL >= 0.0) {
        return NdotL;
    }
    return -NdotL*(1.0 - opacity + 0.15);
}

void main() {

    vec3 position = geomOut.position.xyz;

    color = geomOut.color;
    if(hasTexture) {
        color *= texture2D(uTexture, geomOut.coords);
    }

    vec3 N = vec3(uCamera.view[0][2], uCamera.view[1][2], uCamera.view[2][2]);

    vec3 L0 = vec3(0.0);

    //directional lights
    int directionalCount = min(uLights.directionalCount, MAX_DIR_LIGHTS);
    for(int i = 0; i < directionalCount; i++) {
        vec3 L = normalize(-uLights.directionals[i].direction);
        float amount = computeLightAmountForFace(N, L, color.a);

        L0 += uLights.directionals[i].base.color.rgb*uLights.directionals[i].base.intensity*amount;
    }

    //point lights
    int pointCount = min(uLights.pointCount, MAX_POINT_LIGHTS);
    for(int i = 0; i < pointCount; i++) {
        vec3 lightDirection = uLights.points[i].position - position.xyz;
        float distance = length(lightDirection);
        if(distance > uLights.points[i].radius) {
            continue;
        }

        vec3 L = normalize(lightDirection);
        float amount = computeLightAmountForFace(N, L, color.a);
        float attenuation = computeFalloff(distance, uLights.points[i].radius);

        L0 += uLights.points[i].base.color.rgb*uLights.points[i].base.intensity*attenuation*amount;
    }

    //spot lights
    int spotCount = min(uLights.spotCount, MAX_SPOT_LIGHTS);
    for(int i = 0; i < spotCount; i++) {
        vec3 lightDirection = uLights.spots[i].point.position - position.xyz;
        float distance = length(lightDirection);
        vec3 L = normalize(lightDirection);
        float theta = dot(-normalize(uLights.spots[i].direction), L);

        if(theta <= uLights.spots[i].outerCutOff || distance > uLights.spots[i].point.radius) {
            continue;
        }

        float amount = computeLightAmountForFace(N, L, color.a);
        float attenuation = computeFalloff(distance, uLights.spots[i].point.radius);
        float epsilon = uLights.spots[i].cutOff - uLights.spots[i].outerCutOff;
        float falloff = clamp((theta - uLights.spots[i].outerCutOff)/epsilon, 0, 1);

        L0 += uLights.spots[i].point.base.color.rgb*uLights.spots[i].point.base.intensity*attenuation*falloff*amount;
    }

	color = 0.2*color + color*vec4(L0, 1.0);
}
