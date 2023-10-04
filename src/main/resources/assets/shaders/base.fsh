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
	if ((flags & 1) == 0) discard;

	// TODO: per pixel fog
//	float fc = gl_FragCoord.w / gl_DepthRange.diff - gl_DepthRange.near;
////	fc += gl_Fog.start;
////	fc *= gl_Fog.end - gl_Fog.start;
//	fc = clamp(fc, 0, 1);

	gl_FragColor = texture2D(colortex0, TexCoord.xy).rgba * Color;
}
