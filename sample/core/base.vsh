#version 120
// matrix math functions
#include <matrices.glsl>

varying vec2 TexCoord;
varying vec4 Color;

uniform mat4 camMatrix;

// -- CONFIG -- //
#config
	#define PLANAR_FOG
#endconfig

// define input variables
// will be used later for VOB compat, mostly for instanced rendering
//#inputs
	#define position gl_Vertex
	#define color gl_Color
	#define texture gl_MultiTexCoord0
//#endinputs

void main() {
	// gives more control than ftransform()
	vec4 estimated = vec4(gl_Vertex.xyz, 1.0) * gl_ModelViewMatrixTranspose;
	gl_Position = estimated * gl_ProjectionMatrixTranspose;

	Color = gl_Color;
	TexCoord = gl_MultiTexCoord0.xy;

	#ifdef PLANAR_FOG
		gl_FogFragCoord = clamp(
			(length(estimated.z) - gl_Fog.start) * gl_Fog.scale * gl_Fog.density,
			0, 1
		);
	#else
		gl_FogFragCoord = clamp(
			(length(estimated) - gl_Fog.start) * gl_Fog.scale * gl_Fog.density,
			0, 1
		);
	#endif
}
