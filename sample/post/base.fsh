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
#endconfig

float getDepth(vec2 coord){
	coord = clamp(coord, 0, 1);
	return pow(
		100, texture2D(depthtex0, coord).r
	);
}

float oline(float size, vec2 coord) {
	float outline = 0.0;

	for (int x = -1; x <= 1; x++) {
		for (int y = -1; y <= 1; y++) {
			float f = (getDepth(texcoord) - getDepth(texcoord + vec2(x * (size / width), y * (size / height))));
			outline += (getDepth(texcoord) - getDepth(texcoord + vec2(x * (size / width), y * (size / height))));
		}
	}
	outline *= 10.0;
	outline *= outline;
	outline *= getDepth(coord) / 10.0;

	if (outline > 0.01) {
		outline = clamp(outline, 0.01, 1);
		outline += 0.9;
		outline = clamp(outline, 0.9, 1);
		outline *= CEL_INTENSITY;
	}

	return outline;
}

void main(){
	vec3 color = texture2D(colortex0, texcoord).rgb;

	#ifdef CEL_SHADING
		float outline = 0.0;
		float size = CEL_SIZE * intensity;
		outline = 1 - clamp(oline(size, texcoord), 0, 1);
		color = color * vec3(outline);
	#endif

	gl_FragColor = vec4(color, 1.0);
}