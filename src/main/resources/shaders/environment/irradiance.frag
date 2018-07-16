#version 330

in vec3 passPosition;

out vec4 finalColor;

uniform samplerCube cubemap;

const float PI = 3.14159265359;

void main() {
    vec3 irradiance = vec3(0.0);

    vec3 normal = normalize(passPosition);
    vec3 up = vec3(0.0, 1.0, 0.0);
    vec3 right = cross(normal, up);
    up = cross(normal, right);

    float step = 0.025;
    int sampleCount = 0;

    for(float phi = 0.0; phi < 2.0 * PI; phi += step) {
        for(float theta = 0.0; theta < 0.5 * PI; theta += step) {
            // spherical coordinates (phi, theta) to cartesian (in tangent space)
            vec3 tangentSample = vec3(sin(theta) * cos(phi), sin(theta) * sin(phi), cos(theta));
            // tangent space to world
            vec3 sampleVec = tangentSample.x * right + tangentSample.y  * up + tangentSample.z * normal;

            irradiance += texture(cubemap, sampleVec).rgb * cos(theta) * sin(theta);
            sampleCount++;
        }
    }
    irradiance = PI * irradiance * (1.0/ float(sampleCount));
	finalColor = vec4(irradiance, 1.0);
}
