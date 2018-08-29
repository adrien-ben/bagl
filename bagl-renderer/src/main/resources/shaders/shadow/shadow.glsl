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

float linearizeDepth(float depth, float zNear, float zFar) {
    return (2 * zNear) / (zFar + zNear - depth * (zFar - zNear));
}

int selectShadowMapIndexFromDepth(Shadow shadow, float depth) {
    for(int i = 0; i < CSM_SPLIT_COUNT; i++) {
        if(depth < shadow.shadowCascades[i].splitValue) {
            return i;
        }
    }
    return CSM_SPLIT_COUNT - 1;
}

float computeShadow(Shadow shadow, float linearDepth, vec4 worldSpacePosition) {
    int cascadeIndex = selectShadowMapIndexFromDepth(shadow, linearDepth);
    vec4 lightSpacePosition = shadow.shadowCascades[cascadeIndex].lightViewProj*worldSpacePosition;
    vec3 shadowMapCoords = (lightSpacePosition.xyz / lightSpacePosition.w)*0.5 + 0.5;
    if(shadowMapCoords.z > 1.0) {
        return 1.0;
    }
    return texture(shadow.shadowCascades[cascadeIndex].shadowMap, shadowMapCoords);
}