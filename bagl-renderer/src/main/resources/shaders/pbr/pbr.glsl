const float MAX_REFLECTION_LOD = 4.0;

struct Environment {
    samplerCube irradiance;
    samplerCube preFilteredMap;
    sampler2D brdf;
};

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

vec3 computeIBL(Environment environment, vec3 color, vec3 F0, float NdotV, vec3 N, vec3 R, float roughness, float metallic) {
    vec3 F = fresnel(NdotV, F0, roughness);
    vec3 kS = F;
    vec3 kD = 1.0 - kS;
    kD *= 1.0 - metallic;

    vec3 irradiance = texture(environment.irradiance, N).rgb;
    vec3 diffuse = irradiance * color.rgb;

    vec3 prefilteredSample = textureLod(environment.preFilteredMap, R, roughness * MAX_REFLECTION_LOD).rgb;
    vec2 envBRDF = texture2D(environment.brdf, vec2(NdotV, roughness)).rg;
    vec3 specular = prefilteredSample * (F * envBRDF.x + envBRDF.y);

    return kD * diffuse + specular;
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
