#version 120
// matrix math functions
#include <matrices.glsl>

varying vec2 TexCoord;
varying vec4 Color;

varying vec4 SunCoord;

uniform mat4 camMatrix;
uniform mat4 sunCameraMatrix;
uniform mat4 sunProjectionMatrix;

void main() {
	// gives more control than ftransform()
	vec4 estimated = vec4(gl_Vertex.xyz, 1.0) * gl_ModelViewMatrixTranspose;
	gl_Position = estimated * gl_ProjectionMatrixTranspose;
	SunCoord = vec4(gl_Vertex.xyz, 1.0) * gl_ModelViewMatrixTranspose * // to local space
			inverse(transpose(camMatrix)) * // to camera space
			transpose(sunCameraMatrix) * transpose(sunProjectionMatrix); // to sun-space

	Color = gl_Color;
	TexCoord = gl_MultiTexCoord0.xy;

	gl_FogFragCoord = clamp(
		(length(estimated) - gl_Fog.start) * gl_Fog.scale * gl_Fog.density,
		0, 1
	);
}
