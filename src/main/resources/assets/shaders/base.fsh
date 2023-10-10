#version 120
// required for bitwise operations
#extension GL_EXT_gpu_shader4 : enable

uniform sampler2D colortex0;
varying vec2 TexCoord;
varying vec4 Color;
varying vec4 WorldCoord;

uniform int flags;

void main() {
	// flag: discard, used for fancy graphics for transparent blocks
	// this allows for ignoring per-chunk quad sorting
	if ((flags & 1) == 1) discard;
	// flag: shadow, no point in processing shadows if you're in the process of drawing them
	if ((flags & 2) == 2) {
		gl_FragColor = texture2D(colortex0, TexCoord.xy).rgba * Color;
		return;
	}

	// deal with fog
    float fc = gl_FogFragCoord;
    fc = min(fc, 1);

	// color&tex
	gl_FragColor = texture2D(colortex0, TexCoord.xy).rgba * Color;
	// fog
	gl_FragColor = vec4(
		gl_FragColor.xyz * (1 - fc) + gl_Fog.color.xyz * fc,
		gl_FragColor.a
	);
}
