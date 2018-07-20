#version 330

#import "classpath:/shaders/environment/environment.glsl"

in vec2 passCoords;

out vec2 finalColor;

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
    float k = (roughness*roughness)/2.0;
    float nominator = NdotV;
    float denominator = NdotV*(1.0 - k) + k;

    return nominator/denominator;
}

vec2 integrateBRDF(float NdotV, float roughness) {
    vec3 V;
    V.x = sqrt(1.0 - NdotV*NdotV);
    V.y = 0.0;
    V.z = NdotV;

    float A = 0.0;
    float B = 0.0;

    vec3 N = vec3(0.0, 0.0, 1.0);

    uint SAMPLE_COUNT = 1024u;
    for(uint i = 0u; i < SAMPLE_COUNT; ++i)
    {
        vec2 Xi = hammersley(i, SAMPLE_COUNT);
        vec3 H  = importanceSampleGGX(Xi, N, roughness);
        vec3 L  = normalize(2.0 * dot(V, H) * H - V);

        float NdotL = max(L.z, 0.0);
        float NdotH = max(H.z, 0.0);
        float VdotH = max(dot(V, H), 0.0);

        if(NdotL > 0.0)
        {
            float G = geometrySchlickGGX(NdotV, roughness)*geometrySchlickGGX(NdotL, roughness);//GeometrySmith(N, V, L, roughness);
            float G_Vis = (G * VdotH) / (NdotH * NdotV);
            float Fc = pow(1.0 - VdotH, 5.0);

            A += (1.0 - Fc) * G_Vis;
            B += Fc * G_Vis;
        }
    }
    A /= float(SAMPLE_COUNT);
    B /= float(SAMPLE_COUNT);
    return vec2(A, B);
}

void main() {
    finalColor = integrateBRDF(passCoords.x, passCoords.y);
}
