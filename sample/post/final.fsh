#version 130

#include <matrices.glsl>
#include <post_utils.glsl>

uniform sampler2D colortex0;
uniform sampler2D depthtex0;

uniform float width;
uniform float height;
uniform float intensity;

in vec2 texcoord;

uniform vec3 skyColor;
uniform vec3 sunDir;

#config
	// cel shading
	#define CEL_SHADING
	#define CEL_SIZE 1.0
	#define CEL_INTENSITY 0.666666667

	// fancy effects
	#define GOD_RAYS

	// debug
	#define DISPLAY_DEPTH
	#define DISPLAY_NORMALS

	// shadow variables
    #define USE_SHADOWS
    #define SOFT_PENUMUBRA
    #define SHADOW_BRIGHTNESSS 0.65
    #define PENUMBRA_STEPS 16
    #define PENUMBRA_ROUNDNESS 8
#endconfig

float getDepth(vec2 coord){
	coord = clamp(coord, 0, 1);
	return pow(
		100, texture2D(depthtex0, coord).r
	);
}

#define shadow_bias 0.00005
#include <cel.glsl>
#include <shadow.glsl>
#include <shadow1.glsl>

out vec4 fragColor;

uniform mat4 camMatrix;
uniform mat4 projectionMatrix;

uniform mat4 sunCameraMatrix;
uniform mat4 sunProjectionMatrix;

#line 59
void main(){
	vec3 color = texture2D(colortex0, texcoord).rgb;

	#ifdef DISPLAY_DEPTH
		color = vec3(sqrt((1 / texture2D(depthtex0, texcoord).r) - 1.0) * 2.0);
	#endif

	#ifdef CEL_SHADING
		float outline = 0.0;
		float size = CEL_SIZE * intensity;
		outline = 1 - clamp(oline(size, texcoord), 0, 1);
		color = color * vec3(outline);
	#endif

	#ifdef GOD_RAYS
//		vec4 crd = vec4(normalize(vec3(1, 0.5, 1)) * 10000.0, 1) * transpose(projectionMatrix) * transpose(camMatrix);
		vec4 crd = vec4(normalize(sunDir), 1) * transpose(camMatrix);
		crd *= transpose(projectionMatrix);
		vec2 sun = ((crd.xy / 2)) / crd.w + 0.5;

		// https://github.com/Erkaman/glsl-godrays/blob/master/index.glsl
		float density = 0.5;

		vec2 sunCoord = sun;

		float d = distance(sunCoord, vec2(0.5));

//		if (distance(sunCoord, texcoord.xy) < 0.1) {
//			fragColor = vec4(1, 0, 0, 1);
//			return;
//		}

		if (crd.w > 0 && d < 1.5) {
			d = 1.0;
			float sunDepth = texture2D(depthtex0, sunCoord).r;

			int samples = 200;

			vec2 delta = (texcoord - sunCoord) / samples;

			vec2 searchCoord = texcoord;
			float weight = 1.0;
			float illuminationDecay = 1.0;
			float decay = 0.9;

			float fragcol = 0;

			for (int i = 0; i < samples; i++) {
				searchCoord -= delta;
				float samp = texture2D(depthtex0, searchCoord).r;
				samp *= samp;
				if (samp == 1) {
					samp *= illuminationDecay;
					fragcol += samp;
					illuminationDecay *= decay;
				}
			}

			fragcol /= 20;
			fragcol = fragcol * length(delta) * 200 * weight / max(300.0 / samples, 1.0) * (1 - distance(sunCoord, texcoord));
//			color += vec3(1.5, 0.9, 0.2) * fragcol;
			color += clamp((skyColor * fragcol) / d, 0, 1);
//			color += clamp((vec3(0.8, 0.9, 1.3) * fragcol) / d, 0, 1);
//			color += vec3(0.0, 1.0, 0.0) * fragcol;
			color = clamp(color, 0, 1);
		}
	#endif

	#if defined(DISPLAY_NORMALS) || defined(USE_SHADOWS)
		float texelX = 1 / width;
		float texelY = 1 / height;
		vec3 norm = calculateNormal(camMatrix, projectionMatrix, texcoord, vec2(texelX, texelY), depthtex0);

		#ifdef DISPLAY_NORMALS
			#ifdef DISPLAY_DEPTH
				color *= norm;
			#else
				color *= norm;
			#endif
		#endif
	#endif

	#ifdef USE_SHADOWS
		float depth = texture2D(depthtex0, texcoord).r;
		vec4 worldCoord = screenToWorld(camMatrix, projectionMatrix, depth, texcoord);

		vec4 SunCoord = vec4(worldCoord.xyz, 1.0) *
			transpose(sunCameraMatrix) * transpose(sunProjectionMatrix); // to sun-space

		vec4 shadow;
        #ifdef SOFT_PENUMUBRA
            shadow = softPenumbra(norm, sunDir, SunCoord);
        #else
            shadow = hardPenumbra(norm, sunDir, SunCoord);
        #endif

		color *= shadow.xyz;
    #endif

	fragColor = vec4(color, 1.0);
}
