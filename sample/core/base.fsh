#version 130
// required for bitwise operations
#extension GL_EXT_gpu_shader4: enable
// matrix math functions
#include <matrices.glsl>

uniform sampler2D colortex0;
varying vec2 TexCoord;
varying vec4 Color;
varying vec4 WorldCoord;
varying vec4 SunCoord;

uniform int flags;

void main() {
    // flag: discard, used for fancy graphics for transparent blocks
    // this allows for ignoring per-chunk quad sorting
    if ((flags & 1) == 1) discard;
    // flag: shadow, no point in processing shadows if you're in the process of drawing the shadow map(s)
    // fog should also be disabled in the shadow pass due to colored shadows
    if ((flags & 2) == 2) {
        gl_FragColor = texture2D(colortex0, TexCoord.xy).rgba * Color;
        return;
    }

    gl_FragColor = texture2D(colortex0, TexCoord.xy).rgba * Color;

    // apply fog
    gl_FragColor = vec4(
            gl_FragColor.xyz * (1 - gl_FogFragCoord) + gl_Fog.color.xyz * gl_FogFragCoord,
            gl_FragColor.a
    );
}
