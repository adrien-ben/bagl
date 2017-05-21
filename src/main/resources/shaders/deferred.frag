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
const float MAX_GLOSSINESS = 512;
const float SHADOW_BIAS = 0.005;

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
	depth = depth*2 - 1;
	vec4 screenSpace = vec4(passCoords*2 - 1, depth, 1);
	vec4 position = inverse(uCamera.vp)*screenSpace;
	position.xyz /= position.w;
	return vec4(position.xyz, 1);
}

float computeAttenuation(float distance, Attenuation attenuation) {
	return 1/(attenuation.constant + attenuation.linear*distance + attenuation.quadratic*distance*distance);
}

vec4 computeAmbient(Light light) {
	return vec4(light.color.xyz*light.intensity, 1);
}

vec4 computeDiffuse(Light light, vec3 unitNormal, vec3 unitLightDirection) {
	float diffuse = max(dot(unitNormal, -unitLightDirection), 0);
	return vec4(light.color.xyz*diffuse*light.intensity, 1);
}

vec4 computeSpecular(Light light, float shininess, float glossiness, vec4 position, vec3 unitNormal, vec3 unitLightDirection) {
	vec3 viewDir = normalize(uCamera.position - position.xyz);
	vec3 halfway = normalize(viewDir - unitLightDirection);
	float specular = pow(max(dot(halfway, unitNormal), 0), glossiness);
	return vec4(light.color.xyz*specular*light.intensity*shininess, 1);
}

void main() {
	vec4 normalGloss = texture2D(uGBuffer.normals, passCoords);
	vec3 normal = normalGloss.xyz;
	if(normal.x == 0 && normal.y == 0 && normal.z == 0) {
		gl_FragDepth = 1.0;
		finalColor = vec4(0, 0, 0, 1);
	} else {
		//retrive data from gbuffer
		normal = normalize(normal*2 - 1);
		vec4 colorShininess = texture2D(uGBuffer.colors, passCoords);
		vec4 color = vec4(colorShininess.rgb, 1);
		float depthValue = texture2D(uGBuffer.depth, passCoords).r;
		vec4 position = positionFromDepth(depthValue);
		float shininess = colorShininess.a;
		float glossiness = max(normalGloss.a*MAX_GLOSSINESS, 0.000000001);
		
		//compute lights
		vec4 ambient = vec4(uAmbient.color.xyz*uAmbient.intensity, 1);
		vec4 diffuse = vec4(0, 0, 0, 1);
		vec4 specular = vec4(0, 0, 0, 1);
	
		vec3 lightDirection;


		//directional lights
		for(int i = 0; i < MAX_DIR_LIGHTS; i++) {
		    float shadow = 0;
		    if(i == 0 && uShadow.hasShadow) {
                vec4 lightSpacePosition = uShadow.lightViewProj*position;
                lightSpacePosition.xyz /= lightSpacePosition.w;
                float shadowMapDepth = texture2D(uShadow.shadowMap, lightSpacePosition.xy*0.5 + 0.5).r;
                if(shadowMapDepth + SHADOW_BIAS < lightSpacePosition.z*0.5 + 0.5) {
                    shadow = 1;
                }
		    }
			DirectionalLight light = uDirectionals[i];
			lightDirection = normalize(light.direction);
			diffuse += computeDiffuse(light.base, normal, lightDirection)*(1 - shadow);
			specular += computeSpecular(light.base, shininess, glossiness, position, normal, lightDirection)*(1 - shadow);
		}

		//point lights
		for(int i = 0; i < MAX_POINT_LIGHTS; i++) {
			PointLight light = uPoints[i];
			lightDirection = position.xyz - light.position;
			float distance = length(lightDirection);
			if(distance <= light.radius) {
				lightDirection = normalize(lightDirection);
				float attenuation = computeAttenuation(distance, light.attenuation);
				diffuse += computeDiffuse(light.base, normal, lightDirection)*attenuation;
				specular += computeSpecular(light.base, shininess, glossiness, position, normal, lightDirection)*attenuation;
			}
		}

		//spot lights
		for(int i = 0; i < MAX_SPOT_LIGHTS; i++) {
			SpotLight light = uSpots[i];
			lightDirection = position.xyz - light.point.position;
			float distance = length(lightDirection);
			lightDirection = normalize(lightDirection);
			float theta = dot(-light.direction, -lightDirection);
			if(theta > light.outerCutOff && distance <= light.point.radius) {
				float epsilon = light.cutOff - light.outerCutOff;
				float intensity = clamp((theta - light.outerCutOff)/epsilon, 0, 1);
				float attenuation = computeAttenuation(distance, light.point.attenuation);
				diffuse += computeDiffuse(light.point.base, normal, lightDirection)*attenuation*intensity;
				specular += computeSpecular(light.point.base, shininess, glossiness, position, normal, lightDirection)*attenuation*intensity;
			}
		}

		gl_FragDepth = depthValue;
		finalColor = (ambient + diffuse)*color + specular;
	} 
}
