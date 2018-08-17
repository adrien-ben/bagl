#version 330

#import "classpath:/shaders/common/lights.glsl"
#import "classpath:/shaders/common/maths.glsl"
#import "classpath:/shaders/common/camera.glsl"

const float MIN_SHADOW_BIAS = 0.0005;
const float MAX_SHADOW_BIAS = 0.0025;
const float MAX_REFLECTION_LOD = 4.0;
const int CSM_SPLIT_COUNT = 4;

struct ShadowCascade {
    sampler2DShadow shadowMap;
    mat4 lightViewProj;
    float splitValue;
};

struct Shadow {
    bool hasShadow;
    float zNear;
    float zFar;
    ShadowCascade shadowCascades[CSM_SPLIT_COUNT];
};

struct GBuffer {
	sampler2D colors;
	sampler2D normals;
	sampler2D depth;
	sampler2D emissive;
};

in vec2 passCoords;

out vec4 finalColor;

uniform Shadow uShadow;
uniform GBuffer uGBuffer;
uniform Camera uCamera;
uniform Lights uLights;

vec4 positionFromDepth(float depth) {
	depth = depth*2.0 - 1.0;
	vec4 screenSpace = vec4(passCoords*2.0 - 1.0, depth, 1.0);
	vec4 position = uCamera.invertedViewProj*screenSpace;
	position.xyz /= position.w;
	return vec4(position.xyz, 1.0);
}

/**
 * Computes the fresnel factor of the BRDF. This is Epic's version of the Schlick implementation.
 * @param cosTheta The angle between the half vector and the view vector.
 * @param F0 Base reflexivity of the material.
 * @return Reflected light factor.
 */
vec3 fresnel(float cosTheta, vec3 F0) {
    float powValue = (-5.55473*cosTheta - 6.98316)*cosTheta;
    return F0 + (1.0 - F0)*pow(2, powValue);
}

/**
 * Computes the fresnel factor of the BRDF. This is Epic's version of the Schlick implementation.
 * This is a hacked version of the fresnel function taking into account the roughness of the surface.
 * It is used when computing the diffuse part of the ambient light since their is no halfway vector.
 *
 * @param cosTheta The angle between the half vector and the view vector.
 * @param F0 Base reflexivity of the material.
 * @param roughness Roughness of the surface
 * @return Reflected light factor.
 */
vec3 fresnel(float cosTheta, vec3 F0, float roughness) {
    float powValue = (-5.55473*cosTheta - 6.98316)*cosTheta;
    return F0 + (max(vec3(1.0 - roughness), F0) - F0) * pow(2, powValue);
}

/**
 * Computes the normal distribution of the surface. This is the GGX implementation.
 * @param N The normal to the surface. Must be normalized.
 * @param H The half vector.
 * @param roughness Roughness of the surface.
 * @return The normal ditribution of the surface.
 */
float distribution(vec3 N, vec3 H, float roughness) {
    float a = roughness*roughness;
    float a2 = a*a;
    float NdotH = max(dot(N, H), 0.0);
    float NdotH2 = NdotH*NdotH;

    float nominator = a2;
    float denominator = NdotH2*(a2 - 1.0) + 1.0;
    denominator = PI*denominator*denominator;

    return nominator/denominator;
}

/**
 * Computes the normal distribution of the surface. This is the Schlick/GGX implementation.
 * This method must be called twice. Once passing the dot between normal and view vectors.
 * The second passing the dot between normal and light vector. Then multiplying the two result
 * will give you the geometric term.
 * @param NdotV Dot between normal and view vector ou normal and light vector.
 * @param roughness Roughness of the surface.
 * @return Part of the normal ditribution of the surface.
 */
float geometrySchlickGGX(float NdotV, float roughness) {
    float r = roughness + 1.0;
    float k = (r*r)/8.0;
    float nominator = NdotV;
    float denominator = NdotV*(1.0 - k) + k;

    return nominator/denominator;
}

float computeFalloff(float distance, float radius) {
    float distanceFactor = distance/radius;
    float distanceFactor2 = distanceFactor*distanceFactor;
    float distanceFactor4 = distanceFactor2*distanceFactor2;

    float nominator = pow(clamp(1 - distanceFactor4, 0.0, 1.0), 2);
    float denominator = distanceFactor2 + 1;

    return nominator/denominator;
}

vec3 computeLight(vec3 color, float intensity, float attenuation, vec3 L, vec3 V, vec3 N, float NdotV, vec3 F0, vec3 albedo, float roughness, float metallic) {

    //N.L
    float NdotL = dot(N, L);
    if(NdotL <= 0) {
        return vec3(0);
    }

    //half vector
    vec3 H = normalize(L + V);

    //radiance
    vec3 Li = color*intensity*attenuation;

    //fresnel factor
    vec3 F = fresnel(max(dot(H, V), 0.0), F0);

    //normal distribution
    float ND = distribution(N, H, roughness);

    //geometry factor
    float G = geometrySchlickGGX(NdotV, roughness)*geometrySchlickGGX(NdotL, roughness);

    vec3 nominator = ND*G*F;
    float denominator = 4*NdotV*NdotL + 0.001;
    vec3 specular = nominator/denominator;

    //diffuse factor
    vec3 kD = (1.0 - F)*(1.0 - metallic);

    return (kD*albedo/PI + specular)*Li*NdotL;
}

float linearizeDepth(float depth) {
    float f = uShadow.zFar;
    float n = uShadow.zNear;
    return (2 * n) / (f + n - depth * (f - n));
}

int selectShadowMapIndexFromDepth(float depth) {
    for(int i = 0; i < CSM_SPLIT_COUNT; i++) {
        if(depth < uShadow.shadowCascades[i].splitValue) {
            return i;
        }
    }
    return CSM_SPLIT_COUNT - 1;
}

float computeShadow(float linearDepth, vec4 worldSpacePosition, float bias) {
    int cascadeIndex = selectShadowMapIndexFromDepth(linearDepth);
    vec4 lightSpacePosition = uShadow.shadowCascades[cascadeIndex].lightViewProj*worldSpacePosition;
    vec3 shadowMapCoords = (lightSpacePosition.xyz / lightSpacePosition.w)*0.5 + 0.5;
    if(shadowMapCoords.z > 1.0) {
        return 1.0;
    }
    shadowMapCoords.z -= bias;
    return texture(uShadow.shadowCascades[cascadeIndex].shadowMap, shadowMapCoords);
}

void main() {
    //retrive data from gbuffer
	vec4 normalMetallic = texture2D(uGBuffer.normals, passCoords);
	vec3 N = normalize(normalMetallic.xyz*2 - 1);
    vec4 colorRoughness = texture2D(uGBuffer.colors, passCoords);
    vec3 color = colorRoughness.rgb;
    float depthValue = texture2D(uGBuffer.depth, passCoords).r;
    float linearDepth = linearizeDepth(depthValue);
    vec4 position = positionFromDepth(depthValue);
    float roughness = colorRoughness.a;
    float metallic = normalMetallic.a;
    vec3 emissive = texture2D(uGBuffer.emissive, passCoords).rgb;

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
    vec3 F = fresnel(NdotV, F0, roughness);
    vec3 kS = F;
    vec3 kD = 1.0 - kS;
    kD *= 1.0 - metallic;

    vec3 irradiance = texture(uLights.irradiance, N).rgb;
    vec3 diffuse = irradiance * color;

    vec3 prefilteredSample = textureLod(uLights.preFilteredMap, R, roughness * MAX_REFLECTION_LOD).rgb;
    vec2 envBRDF = texture2D(uLights.brdf, vec2(NdotV, roughness)).rg;
    vec3 specular = prefilteredSample * (F * envBRDF.x + envBRDF.y);

    vec3 ambient = kD * diffuse + specular;

    //directional lights
    int directionalCount = min(uLights.directionalCount, MAX_DIR_LIGHTS);
    for(int i = 0; i < directionalCount; i++) {
        //light direction
        vec3 L = normalize(-uLights.directionals[i].direction);
        float lightIntensity = uLights.directionals[i].base.intensity;
        if(i == 0 && uShadow.hasShadow) {
            float bias = max(MAX_SHADOW_BIAS * (1.0 - dot(N, L)), MIN_SHADOW_BIAS);
            float shadowMapDepthTest = computeShadow(linearDepth, position, bias);
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
