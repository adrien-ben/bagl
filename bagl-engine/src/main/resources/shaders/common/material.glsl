struct Material {
	vec4 diffuseColor;
	vec4 emissiveColor;
    float emissiveIntensity;
	float roughness;
	float metallic;

	bool hasDiffuseMap;
    sampler2D diffuseMap;
    bool hasEmissiveMap;
    sampler2D emissiveMap;
    bool hasOrmMap;
    sampler2D ormMap;
	bool hasNormalMap;
    sampler2D normalMap;

    bool isOpaque;
    float alphaCutoff;
};

bool isTransparent(Material material, vec2 coords) {
    if(!material.isOpaque) {
        float alpha = material.diffuseColor.a;
        if(material.hasDiffuseMap) {
            alpha = texture2D(material.diffuseMap, coords).a;
        }

        if(alpha < material.alphaCutoff) {
            return true;
        }
    }
    return false;
}