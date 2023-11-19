#line 1
uniform int shadowResolution;
uniform sampler2D shadowMap0;
uniform sampler2D shadowMap1;

float sampleShadow(vec2 crd) {
    if (crd.x < 0 || crd.x > 1 || crd.y < 0 || crd.y > 1) {
        crd = (crd - 0.5) / 8 + 0.5;

        if (crd.x < 0 || crd.x > 1 || crd.y < 0 || crd.y > 1)
            return 1.0;

        return texture2D(shadowMap1, crd).r;
    }

    return texelFetch(shadowMap0, ivec2(crd * textureSize(shadowMap0, 0)), 0).r;
}

// https://learnopengl.com/Advanced-Lighting/Shadows/Shadow-Mapping
float checkShadow(vec4 fragPosLightSpace, float bias) {
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;
    float closestDepth = sampleShadow(projCoords.xy);
    float currentDepth = projCoords.z;
    float shadow = currentDepth + shadow_bias > closestDepth ? 0.0 : 1.0;

    return shadow;
}

vec4 checkShadow(float shadowBias, vec3 normal, vec3 sunDir, vec4 fragPosLightSpace, float bias) {
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;
    float closestDepth = sampleShadow(projCoords.xy);
    float currentDepth = projCoords.z;
    float shadow = currentDepth + shadowBias > closestDepth ? 0.0 : 1.0;

//    vec2 shadowTex = projCoords.xy * textureSize(shadowMap0, 0);
//    vec2 texOffset = shadowTex - ivec2(shadowTex);

    return vec4(vec3(shadow), 1.0);
//    return vec4(texOffset, 1 - shadow, 0.5);
}
