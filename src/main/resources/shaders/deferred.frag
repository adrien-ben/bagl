#version 330

const int MAX_DIR_LIGHTS = 50;
const int MAX_POINT_LIGHTS = 50;
const int MAX_SPOT_LIGHTS = 50;
const float SHADOW_BIAS = 0.005;
const float PI = 3.14159265359;

struct Shadow {
    bool hasShadow;
    sampler2D shadowMap;
    mat4 lightViewProj;
};

struct GBuffer {
	sampler2D colors;
	sampler2D normals;
	sampler2D depth;
};

struct Camera {
	mat4 vp;
	vec3 position;
};

struct Light {
	float intensity;
	vec4 color;
};

struct DirectionalLight {
	Light base;
	vec3 direction;
};

struct PointLight {
	Light base;
	vec3 position;
	float radius;
};

struct SpotLight {
	PointLight point;
	vec3 direction;
	float cutOff;
	float outerCutOff;
};


struct Lights {
    Light ambient;
    DirectionalLight directionals[MAX_DIR_LIGHTS];
    int directionalCount;
    PointLight points[MAX_POINT_LIGHTS];
    int pointCount;
    SpotLight spots[MAX_SPOT_LIGHTS];
    int spotCount;
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
	vec4 position = inverse(uCamera.vp)*screenSpace;
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

vec3 computeLight(Light light, float attenuation, vec3 L, vec3 V, vec3 N, float NdotV, vec3 F0, vec3 color, float roughness, float metallic) {

    //N.L
    float NdotL = dot(N, L);
    if(NdotL <= 0) {
        return vec3(0);
    }

    //half vector
    vec3 H = normalize(L + V);

    //radiance
    vec3 Li = light.color.rgb*light.intensity*attenuation;

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

    return (kD*color/PI + specular)*Li*NdotL;
}

void main() {
	vec4 normalMetallic = texture2D(uGBuffer.normals, passCoords);
	vec3 N = normalMetallic.xyz;
	if(N == vec3(0.0)) {
		gl_FragDepth = 1.0;
		finalColor = vec4(0.0, 0.0, 0.0, 1.0);
	} else {
		//retrive data from gbuffer
		N = normalize(N*2 - 1);
		vec4 colorRoughness = texture2D(uGBuffer.colors, passCoords);
		vec3 color = colorRoughness.rgb;
		float depthValue = texture2D(uGBuffer.depth, passCoords).r;
		vec4 position = positionFromDepth(depthValue);
		float roughness = colorRoughness.a;
		float metallic = normalMetallic.a;

        vec3 L0 = uLights.ambient.color.rgb*uLights.ambient.intensity*color;

        //View vector
        vec3 V = normalize(uCamera.position - position.xyz);

        //N.V
        float NdotV = max(dot(N, V), 0.0);

        //base reflexivity
        vec3 F0 = mix(vec3(0.04), color, metallic);

		//directional lights
		int directionalCount = min(uLights.directionalCount, MAX_DIR_LIGHTS);
		for(int i = 0; i < directionalCount; i++) {
		    if(i == 0 && uShadow.hasShadow) {
                vec4 lightSpacePosition = uShadow.lightViewProj*position;
                lightSpacePosition.xyz /= lightSpacePosition.w;
                float shadowMapDepth = texture2D(uShadow.shadowMap, lightSpacePosition.xy*0.5 + 0.5).r;
                if(shadowMapDepth + SHADOW_BIAS < lightSpacePosition.z*0.5 + 0.5) {
                    continue;
                }
		    }

			DirectionalLight light = uLights.directionals[i];

            //light direction
            vec3 L = normalize(-light.direction);

            L0 += computeLight(light.base, 1.0, L, V, N, NdotV, F0, color, roughness, metallic);
		}

		//point lights
		int pointCount = min(uLights.pointCount, MAX_POINT_LIGHTS);
		for(int i = 0; i < pointCount; i++) {
			PointLight light = uLights.points[i];

            vec3 lightDirection = light.position - position.xyz;
            float distance = length(lightDirection);
            if(distance > light.radius) {
                continue;
            }

			vec3 L = normalize(lightDirection);
			float attenuation = computeFalloff(distance, light.radius);

            L0 += computeLight(light.base, attenuation, L, V, N, NdotV, F0, color, roughness, metallic);
		}

		//spot lights
		int spotCount = min(uLights.spotCount, MAX_SPOT_LIGHTS);
		for(int i = 0; i < spotCount; i++) {
			SpotLight light = uLights.spots[i];

			vec3 lightDirection = light.point.position - position.xyz;
            float distance = length(lightDirection);
            vec3 L = normalize(lightDirection);
            float theta = dot(-normalize(light.direction), L);

            if(theta <= light.outerCutOff || distance > light.point.radius) {
                continue;
            }

            float attenuation = computeFalloff(distance, light.point.radius);

            float epsilon = light.cutOff - light.outerCutOff;
            float falloff = clamp((theta - light.outerCutOff)/epsilon, 0, 1);

            L0 += computeLight(light.point.base, attenuation*falloff, L, V, N, NdotV, F0, color, roughness, metallic);
		}

		//HDR
		L0 = L0/(L0 + 1.0);

		//Gamma correction
		L0 = pow(L0, vec3(1.0/2.2));

		gl_FragDepth = depthValue;
		finalColor = vec4(L0, 1.0);
	} 
}
