const int MAX_DIR_LIGHTS = 50;
const int MAX_POINT_LIGHTS = 50;
const int MAX_SPOT_LIGHTS = 50;

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
    samplerCube irradiance;
    samplerCube preFilteredMap;
    sampler2D brdf;
    DirectionalLight directionals[MAX_DIR_LIGHTS];
    int directionalCount;
    PointLight points[MAX_POINT_LIGHTS];
    int pointCount;
    SpotLight spots[MAX_SPOT_LIGHTS];
    int spotCount;
};

float computeFalloff(float distance, float radius) {
    float distanceFactor = distance/radius;
    float distanceFactor4 = distanceFactor*distanceFactor*distanceFactor*distanceFactor;
    float nominatorSqrt = clamp(1 - distanceFactor4, 0.0, 1.0);
    float nominator = nominatorSqrt*nominatorSqrt;

    float distance2 = distance*distance;
    float denominator = distance2 + 1;

    return nominator/denominator;
}
