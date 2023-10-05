#version 120
// required for bitwise operations
#extension GL_EXT_gpu_shader4: enable
// required for matrix math functions
#extension GL_ARB_gpu_shader5: enable

uniform sampler2D colortex0;
varying vec2 TexCoord;
varying vec4 Color;
varying vec4 WorldCoord;
varying vec4 SunCoord;

uniform int flags;

// -- CONFIG -- //
#config
    #define SOFT_PENUMUBRA
    #define SHADOW_BRIGHTNESSS 0.35
    #define PENUMBRA_STEPS 8
    #define PENUMBRA_ROUNDNESS 8
#endconfig

// -- CONSTANTS -- //
#define shadow_bias 0.00005

#include <shadow.glsl>

// https://learnopengl.com/Advanced-Lighting/Shadows/Shadow-Mapping
vec4 hardPenumbra(vec4 fragPosLightSpace) {
    float shadow = checkShadow(fragPosLightSpace, shadow_bias) * (1.0 - SHADOW_BRIGHTNESSS) + SHADOW_BRIGHTNESSS;
    return vec4(shadow, shadow, shadow, 1.0);
}

vec4 softPenumbra(vec4 fragPosLightSpace) {
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;
    float texel = 1.0 / float(shadowResolution);
    float currentDepth = projCoords.z;

    float averageShadow = 0.0;

    float penumbraBias = projCoords.z;
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
            averageShadow += currentDepth - shadow_bias > closestDepth ? 0.0 : 1.0;
        }
    }

    averageShadow /= (s0 * r0);
    averageShadow = clamp(averageShadow, 0.0, 1.0) * (1.0 - SHADOW_BRIGHTNESSS) + SHADOW_BRIGHTNESSS;

    return vec4(averageShadow, averageShadow, averageShadow, 1.0);
}

void main() {
    // flag: discard, used for fancy graphics for transparent blocks
    // this allows for ignoring per-chunk quad sorting
    if ((flags & 1) == 1) discard;
    // flag: shadow, no point in processing shadows if you're in the process of drawing the shadow map(s)
    if ((flags & 2) == 2) {
        gl_FragColor = texture2D(colortex0, TexCoord.xy).rgba * Color;
        return;
    }

    vec4 shadow;
    #ifdef SOFT_PENUMUBRA
        vec3 projCoords = SunCoord.xyz / SunCoord.w;
        shadow = softPenumbra(SunCoord);
    #else
        shadow = hardPenumbra(SunCoord);
    #endif

    gl_FragColor = texture2D(colortex0, TexCoord.xy).rgba * Color * shadow;
}
