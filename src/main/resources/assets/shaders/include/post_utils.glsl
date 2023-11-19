// requires matrices.glsl

// https://discourse.panda3d.org/t/post-processing-filter-shader-how-to-calculate-world-xyz-position-of-pixels-using-the-depth-buffer/25179/7
vec4 screenToWorld(mat4 model, mat4 proj, in float depth, in vec2 uv){
    vec4 coord = vec4(uv, depth, 1.0) * 2.0 - 1.0;

    coord = inverse(proj * model) * coord;
    coord /= coord.w; // linearize
    return coord;
}

// https://wickedengine.net/2019/09/22/improved-normal-reconstruction-from-depth/
vec3 calculateNormal(mat4 model, mat4 proj, vec2 texCoord, vec2 texel, sampler2D depth) {
//    #define NORMALS_FAST
    float d = texture2D(depth, texCoord).r;
    if (d == 1) return vec3(0);

    float d0 = texture2D(depth, texCoord + vec2(texel.x, 0)).r;
    float d1 = texture2D(depth, texCoord - vec2(texel.x, 0)).r;
    float d2 = texture2D(depth, texCoord + vec2(0, texel.y)).r;
    float d3 = texture2D(depth, texCoord - vec2(0, texel.y)).r;

    #ifdef NORMALS_FAST
//        if (d0 == 1 && d1 == 1 && d2 == 1 && d3 == 1) return vec3(0);

        vec4 crd0 = screenToWorld(model, proj, d0, texCoord + vec2(texel.x, 0));
        vec4 crd1 = screenToWorld(model, proj, d1, texCoord - vec2(texel.x, 0));
        vec4 crd2 = screenToWorld(model, proj, d2, texCoord + vec2(0, texel.y));
        vec4 crd3 = screenToWorld(model, proj, d3, texCoord - vec2(0, texel.y));

        vec3 norm = -normalize(cross(crd3.xyz - crd2.xyz, crd1.xyz - crd0.xyz));
    #else
        vec4 crd = screenToWorld(model, proj, d, texCoord);
        vec4 crd0 = screenToWorld(model, proj, d0, texCoord + vec2(texel.x, 0));
        vec4 crd1 = screenToWorld(model, proj, d1, texCoord - vec2(texel.x, 0));
        vec4 crd2 = screenToWorld(model, proj, d2, texCoord + vec2(0, texel.y));
        vec4 crd3 = screenToWorld(model, proj, d3, texCoord - vec2(0, texel.y));

        vec3 norm0 = (cross(crd2.xyz - crd.xyz, crd0.xyz - crd.xyz));
        vec3 norm1 = -(cross(crd2.xyz - crd.xyz, crd0.xyz - crd.xyz));
        vec3 norm2 = (cross(crd3.xyz - crd.xyz, crd1.xyz - crd.xyz));
        vec3 norm3 = -(cross(crd3.xyz - crd.xyz, crd0.xyz - crd.xyz));

        vec3 norm = norm0;
        if (length(norm1) < length(norm)) norm = norm1;
        if (length(norm2) < length(norm)) norm = norm2;
        if (length(norm3) < length(norm)) norm = norm3;

        norm = -normalize(norm);
    #endif

    return norm;
}
