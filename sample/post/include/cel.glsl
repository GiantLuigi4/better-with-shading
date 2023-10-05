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
