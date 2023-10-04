package tfc.better_with_shaders.mixin;

import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.WorldRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tfc.better_with_shaders.ShaderManager;

@Mixin(value = WorldRenderer.class, remap = false)
public class WorldRendererMixin {
    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderGlobal;callAllDisplayLists(ID)V"))
    public void itsAlreadyCalled(RenderGlobal instance, int pass, double pct) {
        if (ShaderManager.INSTANCE.shadersActive()) {
            ShaderManager.INSTANCE.getDefaultShader().bind();
            instance.callAllDisplayLists(pass, pct);
            ShaderManager.INSTANCE.getDefaultShader().unbind();
        } else {
            instance.callAllDisplayLists(pass, pct);
        }
    }
}
