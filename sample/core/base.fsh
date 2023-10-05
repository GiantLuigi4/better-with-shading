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
#include <shadow1.glsl>

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
