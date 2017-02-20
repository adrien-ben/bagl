#version 330

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
const int MAX_POINT_LIGHTS = 5;
const int MAX_SPOT_LIGHTS = 3;
const float DEFAULT_SHININESS = 32;

in vec2 passCoords;

out vec4 finalColor;

uniform Camera uCamera;
uniform Light uAmbient;
uniform DirectionalLight uDirectionals[MAX_DIR_LIGHTS];
uniform PointLight uPoints[MAX_POINT_LIGHTS];
uniform SpotLight uSpots[MAX_SPOT_LIGHTS];

uniform sampler2D colors;
uniform sampler2D normals;
uniform sampler2D depth;

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

vec4 computeSpecular(Light light, float shininess, vec4 position, vec3 unitNormal, vec3 unitLightDirection) {
	vec3 viewDir = normalize(uCamera.position - position.xyz);
	vec3 refectDir = reflect(unitLightDirection, unitNormal);
	float specular = pow(max(dot(viewDir, refectDir), 0), shininess);
	float specularIntensity = 0.5;
	return vec4(light.color.xyz*specular*light.intensity*specularIntensity, 1);
}

void main() {
	vec3 normal = texture2D(normals, passCoords).xyz;
	if(normal.x == 0 && normal.y == 0 && normal.z == 0) {
		finalColor = vec4(0, 0, 0, 1);
	} else {
		//retrive data from gbuffer
		vec3 normal = texture2D(normals, passCoords).xyz*2 - 1;
		vec4 color = texture2D(colors, passCoords);
		float depthValue = texture2D(depth, passCoords).r;
		vec4 position = positionFromDepth(depthValue); 
		
		//compute lights
		vec4 ambient = vec4(uAmbient.color.xyz*uAmbient.intensity, 1);
		vec4 diffuse = vec4(0, 0, 0, 1);
		vec4 specular = vec4(0, 0, 0, 1);
	
		vec3 lightDirection;
			
		//directional lights
		for(int i = 0; i < MAX_DIR_LIGHTS; i++) {
			DirectionalLight light = uDirectionals[i];
			lightDirection = normalize(light.direction);
			diffuse += computeDiffuse(light.base, normal, lightDirection);
			specular += computeSpecular(light.base, DEFAULT_SHININESS, position, normal, lightDirection);
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
				specular += computeSpecular(light.base, DEFAULT_SHININESS, position, normal, lightDirection)*attenuation;
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
				specular += computeSpecular(light.point.base, DEFAULT_SHININESS, position, normal, lightDirection)*attenuation*intensity;
			}
		}

		finalColor = (ambient + diffuse)*color + specular;
	} 
}
