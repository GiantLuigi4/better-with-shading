uniform int shadowResolution;
uniform sampler2D shadowMap0;
uniform sampler2D shadowMap1;

float sampleShadow(vec2 crd) {
    if (crd.x < 0 || crd.x > 1 || crd.y < 0 || crd.y > 1) {
        crd = (crd - 0.5) / 4 + 0.5;
        if (crd.x < 0 || crd.x > 1 || crd.y < 0 || crd.y > 1) {
            return 1.0;
        }
        return texture2D(shadowMap1, crd).r;
    }
    return texture2D(shadowMap0, crd).r;
}

// https://learnopengl.com/Advanced-Lighting/Shadows/Shadow-Mapping
float checkShadow(vec4 fragPosLightSpace, float bias) {
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;
    float closestDepth = sampleShadow(projCoords.xy);
    float currentDepth = projCoords.z;
    float shadow = currentDepth - bias > closestDepth ? 0.0 : 1.0;

    return shadow;
}
