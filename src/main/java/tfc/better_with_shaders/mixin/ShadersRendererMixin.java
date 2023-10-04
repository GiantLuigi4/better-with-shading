package tfc.better_with_shaders.mixin;

import net.minecraft.client.render.Framebuffer;
import net.minecraft.client.render.shader.ShadersRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.better_with_shaders.util.RendererExtensions;

@Mixin(ShadersRenderer.class)
public class ShadersRendererMixin implements RendererExtensions {
    @Shadow @Final protected Framebuffer worldFramebuffer;

    public void rebind() {
        worldFramebuffer.bind();
    }
}
