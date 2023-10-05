#version 120
// required for bitwise operations
#extension GL_EXT_gpu_shader4 : enable

varying vec2 TexCoord;
varying vec4 Color;

void main() {
	// gives more control than ftransform()
	vec4 estimated = vec4(gl_Vertex.xyz, 1.0) * gl_ModelViewMatrixTranspose * gl_ProjectionMatrixTranspose;
	gl_Position = estimated;

	Color = gl_Color;
	TexCoord = gl_MultiTexCoord0.xy;
}
