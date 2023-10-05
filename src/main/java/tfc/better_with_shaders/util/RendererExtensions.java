package tfc.better_with_shaders.util;

import net.minecraft.client.render.shader.Shader;

public interface RendererExtensions {
    void rebind();

    void disableShader();
    void enableShader(Shader sdr);
}
