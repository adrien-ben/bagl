#version 330

#import "classpath:/shaders/common/maths.glsl"
#import "classpath:/shaders/environment/environment.glsl"

in vec3 passPosition;

out vec4 finalColor;

uniform samplerCube cubemap;
uniform float roughness;

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

void main() {
    vec3 N = normalize(passPosition);
    vec3 R = N;
    vec3 V = R;

    uint SAMPLE_COUNT = 1024u;
    float totalWeight = 0.0;
    vec3 prefilteredColor = vec3(0.0);
    for(uint i = 0u; i < SAMPLE_COUNT; ++i) {
        vec2 Xi = hammersley(i, SAMPLE_COUNT);
        vec3 H  = importanceSampleGGX(Xi, N, roughness);
        vec3 L  = normalize(2.0 * dot(V, H) * H - V);

        float NdotL = max(dot(N, L), 0.0);
        if(NdotL > 0.0) {
            float D = distribution(N, H, roughness);
            float pdf = (D * max(dot(N, H), 0.0) / (4.0 * max(dot(H, V), 0.0))) + 0.0001;

            float resolution = 1024.0;
            float saTexel = 4.0*PI/(6.0*resolution*resolution);
            float saSample = 1.0/(float(SAMPLE_COUNT) * pdf + 0.0001);

            float mipLevel = roughness == 0.0 ? 0.0 : 0.5 * log2(saSample / saTexel);

            prefilteredColor += textureLod(cubemap, L, mipLevel).rgb * NdotL;
            totalWeight      += NdotL;
        }
    }
    prefilteredColor = prefilteredColor / totalWeight;
	finalColor = vec4(prefilteredColor, 1.0);
}
