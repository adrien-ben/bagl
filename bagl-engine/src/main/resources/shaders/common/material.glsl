struct Material {
	vec4 diffuseColor;
	vec4 emissiveColor;
    float emissiveIntensity;
	float roughness;
	float metallic;
	float occlusionStrength;

	bool hasDiffuseMap;
    sampler2D diffuseMap;
    bool hasEmissiveMap;
    sampler2D emissiveMap;
    bool hasRoughnessMetallicMap;
    sampler2D roughnessMetallicMap;
	bool hasNormalMap;
    sampler2D normalMap;
    bool hasOcclusionMap;
    sampler2D occlusionMap;

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

vec4 getDiffuseColor(Material material, vec2 texCoords) {
    vec4 diffuseColor = material.diffuseColor;
    if(material.hasDiffuseMap) {
        vec4 diffuseTextureColor = texture2D(material.diffuseMap, texCoords);
        diffuseColor.rgb *= pow(diffuseTextureColor.rgb, vec3(2.2));
        diffuseColor.a *= diffuseTextureColor.a;
    }
    return diffuseColor;
}

vec3 getEmissiveColor(Material material, vec2 texCoords) {
    vec3 emissive = material.emissiveColor.rgb*material.emissiveIntensity;
    if(material.hasEmissiveMap) {
        emissive *= pow(texture2D(material.emissiveMap, texCoords).rgb, vec3(2.2));
    }
    return emissive;
}

float getRoughness(Material material, vec2 texCoords) {
    float roughness = material.roughness;
    if(material.hasRoughnessMetallicMap) {
        roughness *= texture2D(material.roughnessMetallicMap, texCoords).g;
    }
    return clamp(roughness, 0.03, 1.0);
}

float getMetallic(Material material, vec2 texCoords) {
    float metallic = material.metallic;
	if(material.hasRoughnessMetallicMap) {
	    metallic *= texture2D(material.roughnessMetallicMap, texCoords).b;
	}
    return metallic;
}

float getOcclusionStrength(Material material) {
    return material.occlusionStrength;
}

float getOcclusionValue(Material material, vec2 texCoords) {
    float occlusionValue = 1.0;
    if(material.hasOcclusionMap) {
        occlusionValue = texture(material.occlusionMap, texCoords).r;
    }
    return occlusionValue;
}

vec3 getNormal(Material material, vec3 nonMappedNormal, mat3 tbnMatrix, vec2 texCoords) {
    vec3 normal;
	if(material.hasNormalMap) {
		normal = normalize(texture2D(material.normalMap, texCoords).rgb*2 - 1);
		normal = normalize(tbnMatrix*normal);
	} else {
		normal = normalize(nonMappedNormal);
	}
	if(!gl_FrontFacing) {
	    normal *= -1;
    }
    return normal;
}
