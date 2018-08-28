#version 330

#import "classpath:/shaders/common/lights.glsl"
#import "classpath:/shaders/common/maths.glsl"
#import "classpath:/shaders/common/camera.glsl"
#import "classpath:/shaders/pbr/pbr.glsl"
#import "classpath:/shaders/shadow/shadow.glsl"

struct GBuffer {
	sampler2D colors;
	sampler2D normals;
	sampler2D depth;
	sampler2D emissive;
	sampler2D occlusion;
};

in vec2 passCoords;

out vec4 finalColor;

uniform Shadow uShadow;
uniform GBuffer uGBuffer;
uniform Camera uCamera;
uniform Lights uLights;
uniform Environment uEnvironment;

vec4 positionFromDepth(float depth) {
	depth = depth*2.0 - 1.0;
	vec4 screenSpace = vec4(passCoords*2.0 - 1.0, depth, 1.0);
	vec4 position = uCamera.invertedViewProj*screenSpace;
	position.xyz /= position.w;
	return vec4(position.xyz, 1.0);
}

void main() {
    //retrive data from gbuffer
	vec4 normalMetallic = texture2D(uGBuffer.normals, passCoords);
    vec4 colorRoughness = texture2D(uGBuffer.colors, passCoords);
    float depthValue = texture2D(uGBuffer.depth, passCoords).r;
    vec3 emissive = texture2D(uGBuffer.emissive, passCoords).rgb;
    vec2 occlusion = texture(uGBuffer.occlusion, passCoords).rg;

	// separate the data
    vec4 position = positionFromDepth(depthValue);
    float linearDepth = linearizeDepth(depthValue, uShadow.zNear, uShadow.zFar);
	vec3 N = normalize(normalMetallic.xyz*2 - 1);

    vec3 color = colorRoughness.rgb;
    float roughness = colorRoughness.a;
    float metallic = normalMetallic.a;
    float occlusionStrength = occlusion.r;
    float occlusionValue = occlusion.g;

    vec3 L0 = vec3(0.0);

    //View vector
    vec3 V = normalize(uCamera.position - position.xyz);

    //Reflected view vector
    vec3 R = reflect(-V, N);

    //N.V
    float NdotV = max(dot(N, V), 0.0);

    //base reflexivity
    vec3 F0 = mix(vec3(0.04), color, metallic);

    //Ambient term
    vec3 ambient = computeIBL(uEnvironment, color, F0, NdotV, N, R, roughness, metallic);
    ambient = mix(ambient, ambient*occlusionValue, occlusionStrength);

    //directional lights
    int directionalCount = min(uLights.directionalCount, MAX_DIR_LIGHTS);
    for(int i = 0; i < directionalCount; i++) {
        //light direction
        vec3 L = normalize(-uLights.directionals[i].direction);
        float lightIntensity = uLights.directionals[i].base.intensity;
        if(i == 0 && uShadow.hasShadow) {
            float shadowMapDepthTest = computeShadow(uShadow, linearDepth, position);
            lightIntensity *= shadowMapDepthTest;
        }

        L0 += computeLight(uLights.directionals[i].base.color.rgb, lightIntensity, 1.0, L, V, N, NdotV, F0, color, roughness, metallic);
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
        float attenuation = computeFalloff(distance, uLights.points[i].radius);

        L0 += computeLight(uLights.points[i].base.color.rgb, uLights.points[i].base.intensity, attenuation, L, V, N, NdotV, F0, color, roughness, metallic);
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

        float attenuation = computeFalloff(distance, uLights.spots[i].point.radius);

        float epsilon = uLights.spots[i].cutOff - uLights.spots[i].outerCutOff;
        float falloff = clamp((theta - uLights.spots[i].outerCutOff)/epsilon, 0, 1);

        L0 += computeLight(uLights.spots[i].point.base.color.rgb, uLights.spots[i].point.base.intensity, attenuation*falloff, L, V, N, NdotV, F0, color, roughness, metallic);
    }

    finalColor = vec4(ambient + L0 + emissive, 1.0);
}
