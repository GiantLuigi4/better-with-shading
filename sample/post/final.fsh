#version 120

uniform sampler2D colortex0;
uniform sampler2D depthtex0;

uniform float width;
uniform float height;
uniform float intensity;

varying vec2 texcoord;

#config
	#define CEL_SHADING
	#define CEL_SIZE 1.0
	#define CEL_INTENSITY 0.666666667

	#define GOD_RAYS
#endconfig

float getDepth(vec2 coord){
	coord = clamp(coord, 0, 1);
	return pow(
		100, texture2D(depthtex0, coord).r
	);
}

#include <lighting.glsl>
#include <cel.glsl>

void main(){
	vec3 color = texture2D(colortex0, texcoord).rgb;

	#ifdef CEL_SHADING
		float outline = 0.0;
		float size = CEL_SIZE * intensity;
		outline = 1 - clamp(oline(size, texcoord), 0, 1);
		color = color * vec3(outline);
	#endif

	#ifdef GOD_RAYS
		// https://github.com/Erkaman/glsl-godrays/blob/master/index.glsl
		float density = 0.5;

		vec2 sunCoord = vec2(0.5);
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
//		color += vec3(1.5, 0.9, 0.2) * fragcol;
		color += vec3(0.8, 0.9, 1.3) * fragcol;
//		color += vec3(0.0, 1.0, 0.0) * fragcol;
		color = clamp(color, 0, 1);
	#endif

	gl_FragColor = vec4(color, 1.0);
}
