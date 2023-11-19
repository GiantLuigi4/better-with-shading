float calcBias(vec3 normal, vec3 sunDir) {
    float bias = 0.00001;

    bool axil =
        abs(normal.x) > 0.9 ||
        abs(normal.y) > 0.9 ||
        abs(normal.z) > 0.9;

    if (axil) {
        bias -= 0.001 * (1.0 - dot(normal, sunDir));
        bias += 0.00025; // 0.00001
    } else {
        bias -= 0.001 * (1.0 - dot(normal, sunDir));
        bias += 0.001;
    }

    return bias;
}

// https://learnopengl.com/Advanced-Lighting/Shadows/Shadow-Mapping
vec4 hardPenumbra(vec3 normal, vec3 sunDir, vec4 fragPosLightSpace) {
    float bias = calcBias(normal, sunDir);
    vec4 shadow = checkShadow(-bias, normal, sunDir, fragPosLightSpace, shadow_bias) * (1.0 - SHADOW_BRIGHTNESSS) + SHADOW_BRIGHTNESSS;
    return shadow;
}

vec4 softPenumbra(vec3 normal, vec3 sunDir, vec4 fragPosLightSpace) {
    float bias = calcBias(normal, sunDir);
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;
    float texel = 1.0 / float(shadowResolution);
    float currentDepth = projCoords.z;

    float averageShadow = 0.0;

    float penumbraBias = projCoords.z;
    penumbraBias = 1 - penumbraBias;
    penumbraBias *= penumbraBias;

    float s0 = float(int(PENUMBRA_STEPS * penumbraBias));
    float r0 = float(int(PENUMBRA_ROUNDNESS * penumbraBias));
    s0 = clamp(s0, 1, PENUMBRA_STEPS);
    r0 = clamp(r0, 1, PENUMBRA_ROUNDNESS);

    for (int r = 0; r < r0; r++) {
        float s = sin((r / r0) * 6.28319);
        float c = cos((r / r0) * 6.28319);

        for (int i = 0; i < s0; i++) {
            float closestDepth = sampleShadow(
                projCoords.xy + vec2(c * texel, s * texel) * (i / max(1.0, s0 - 1.0))
            );
            averageShadow += currentDepth - bias > closestDepth ? 0.0 : 1.0;
        }
    }

    averageShadow /= (s0 * r0);
    averageShadow = clamp(averageShadow, 0.0, 1.0) * (1.0 - SHADOW_BRIGHTNESSS) + SHADOW_BRIGHTNESSS;

    return vec4(averageShadow, averageShadow, averageShadow, 1.0);
}
