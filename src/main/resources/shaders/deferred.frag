#version 330

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

struct Attenuation {
	float constant;
	float linear;
	float quadratic;
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
	Attenuation attenuation;
};

struct SpotLight {
	PointLight point;
	vec3 direction;
	float cutOff;
	float outerCutOff;
};

const int MAX_DIR_LIGHTS = 2;
const int MAX_POINT_LIGHTS = 6;
const int MAX_SPOT_LIGHTS = 3;
const float SHADOW_BIAS = 0.005;
const float PI = 3.14159265359;

in vec2 passCoords;

out vec4 finalColor;

uniform Shadow uShadow;
uniform GBuffer uGBuffer;
uniform Camera uCamera;
uniform Light uAmbient;
uniform DirectionalLight uDirectionals[MAX_DIR_LIGHTS];
uniform PointLight uPoints[MAX_POINT_LIGHTS];
uniform SpotLight uSpots[MAX_SPOT_LIGHTS];

vec4 positionFromDepth(float depth) {
	depth = depth*2.0 - 1.0;
	vec4 screenSpace = vec4(passCoords*2.0 - 1.0, depth, 1.0);
	vec4 position = inverse(uCamera.vp)*screenSpace;
	position.xyz /= position.w;
	return vec4(position.xyz, 1.0);
}

/**
 * Computes the fresnel factor of the BRDF. This is the Schlick implementation.
 * @param cosTheta The angle between the half vector and the view vector.
 * @param F0 Base reflexivity of the material.
 * @return Reflected light factor.
 */
vec3 fresnel(float cosTheta, vec3 F0) {
    return F0 + (1.0 - F0)*pow(1.0 - cosTheta, 5.0);
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

float geometrySchlickGGX(float NdotV, float roughness) {
    float r = roughness + 1.0;
    float k = r*r/8.0;
    float nominator = NdotV;
    float denominator = NdotV*(1.0 - k) + k;

    return nominator/denominator;
}

/**
 * Computes the geometry factor of the BRDF. This is the Smith implementation.
 * @param N The normal to the surface. Must be normalized.
 * @param V The view vector.
 * @param L The light vector.
 * @param roughness Roughness of the surface.
 * @return The geometry factor of the surface.
 */
float geometry(vec3 N, vec3 V, vec3 L, float roughness) {
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float ggx1 = geometrySchlickGGX(NdotV, roughness);
    float ggx2 = geometrySchlickGGX(NdotL, roughness);

    return ggx1*ggx2;
}



void main() {
	vec4 normalMetallic = texture2D(uGBuffer.normals, passCoords);
	vec3 N = normalMetallic.xyz;
	if(N.x == 0.0 && N.y == 0.0 && N.z == 0.0) {
		gl_FragDepth = 1.0;
		finalColor = vec4(0.0, 0.0, 0.0, 1.0);
	} else {
		//retrive data from gbuffer
		N = normalize(N*2 - 1);
		vec4 colorRoughness = texture2D(uGBuffer.colors, passCoords);
		vec3 color = pow(colorRoughness.rgb, vec3(2.2));
		float depthValue = texture2D(uGBuffer.depth, passCoords).r;
		vec4 position = positionFromDepth(depthValue);
		float roughness = colorRoughness.a;
		float metallic = normalMetallic.a;

        vec3 L0 = vec3(0.0);

        //View vector
        vec3 V = normalize(uCamera.position - position.xyz);

        //N.V
        float NdotV = max(dot(N, V), 0.0);

        //base reflexivity
        vec3 F0 = mix(vec3(0.04), color, metallic);

		//directional lights
		for(int i = 0; i < MAX_DIR_LIGHTS; i++) {
//		    float shadow = 0;
//		    if(i == 0 && uShadow.hasShadow) {
//                vec4 lightSpacePosition = uShadow.lightViewProj*position;
//                lightSpacePosition.xyz /= lightSpacePosition.w;
//                float shadowMapDepth = texture2D(uShadow.shadowMap, lightSpacePosition.xy*0.5 + 0.5).r;
//                if(shadowMapDepth + SHADOW_BIAS < lightSpacePosition.z*0.5 + 0.5) {
//                    shadow = 1;
//                }
//		    }
			DirectionalLight light = uDirectionals[i];

            //light direction
            vec3 L = normalize(-light.direction);

            //N.L
            float NdotL = max(dot(N, L), 0.0);

            //half vector
            vec3 H = normalize(L + V);

            //radiance
            vec3 Li = light.base.color.rgb*light.base.intensity;

            //fresnel factor
            vec3 F = fresnel(max(dot(H, V), 0.0), F0);

            //normal distribution
            float ND = distribution(N, H, roughness);

            //geometry factor
            float G = geometry(N, V, L, roughness);

            vec3 nominator = ND*G*F;
            float denominator = 4*NdotV*NdotL + 0.001;
            vec3 specular = nominator/denominator;

            //diffuse factor
            vec3 kD = (1.0 - F)*(1.0 - metallic);

            L0 += (kD*color/PI + specular)*Li*NdotL;

		}

		//point lights
//		for(int i = 0; i < MAX_POINT_LIGHTS; i++) {
//			PointLight light = uPoints[i];
//			lightDirection = position.xyz - light.position;
//			float distance = length(lightDirection);
//			if(distance <= light.radius) {
//				lightDirection = normalize(lightDirection);
//				float attenuation = computeAttenuation(distance, light.attenuation);
//				diffuse += computeDiffuse(light.base, normal, lightDirection)*attenuation;
//				specular += computeSpecular(light.base, shininess, glossiness, position, normal, lightDirection)*attenuation;
//			}
//		}

		//spot lights
//		for(int i = 0; i < MAX_SPOT_LIGHTS; i++) {
//			SpotLight light = uSpots[i];
//			lightDirection = position.xyz - light.point.position;
//			float distance = length(lightDirection);
//			lightDirection = normalize(lightDirection);
//			float theta = dot(-light.direction, -lightDirection);
//			if(theta > light.outerCutOff && distance <= light.point.radius) {
//				float epsilon = light.cutOff - light.outerCutOff;
//				float intensity = clamp((theta - light.outerCutOff)/epsilon, 0, 1);
//				float attenuation = computeAttenuation(distance, light.point.attenuation);
//				diffuse += computeDiffuse(light.point.base, normal, lightDirection)*attenuation*intensity;
//				specular += computeSpecular(light.point.base, shininess, glossiness, position, normal, lightDirection)*attenuation*intensity;
//			}
//		}


//		L0 += uAmbient.color.rgb*uAmbient.intensity*color;

		//HDR
		L0 = L0/(L0 + 1.0);

		//Gamma correction
		L0 = pow(L0, vec3(1.0/2.2));

		gl_FragDepth = depthValue;
		finalColor = vec4(L0, 1.0);
	} 
}
