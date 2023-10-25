#version 120
// required for bitwise operations
#extension GL_EXT_gpu_shader4 : enable

varying vec2 TexCoord;
varying vec4 Color;

// define input variables
// will be used later for VOB compat, mostly for instanced rendering
//#inputs
	#define position gl_Vertex
	#define color gl_Color
	#define texture gl_MultiTexCoord0
//#endinputs

void main() {
	// gives more control than ftransform()
	// coordinate
	vec4 estimated = vec4(position.xyz, 1.0) * gl_ModelViewMatrixTranspose;
	gl_Position = estimated * gl_ProjectionMatrixTranspose;

	// color&tex
	Color = color;
	TexCoord = texture.xy;

	// fog
	gl_FogFragCoord = clamp(
		(length(estimated) - gl_Fog.start) * gl_Fog.scale * gl_Fog.density,
		0, 1
	);
}
