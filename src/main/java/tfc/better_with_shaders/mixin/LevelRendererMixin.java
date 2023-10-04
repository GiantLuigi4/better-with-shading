package tfc.better_with_shaders.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.RenderEngine;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.camera.ICamera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.better_with_shaders.ShaderManager;

@Mixin(value = RenderGlobal.class, remap = false)
public class LevelRendererMixin {
	@Inject(at = @At("HEAD"), method = "sortAndRender")
	public void preSort(ICamera camera, int renderPass, double renderPartialTicks, CallbackInfoReturnable<Integer> cir) {
		if (!ShaderManager.INSTANCE.shadersActive()) return;
		
		ShaderManager.INSTANCE.getDefaultShader().bind();
		if (
				renderPass == 0 ||
						Minecraft.getMinecraft(Minecraft.class).gameSettings.fancyGraphics.value != 1
		) {
			ShaderManager.INSTANCE.getDefaultShader().uniformInt(
					"flags", 1
			);
		} else {
			ShaderManager.INSTANCE.getDefaultShader().uniformInt(
					"flags", 0
			);
		}
	}
	
	@Inject(at = @At("RETURN"), method = "sortAndRender")
	public void postSort(ICamera camera, int renderPass, double renderPartialTicks, CallbackInfoReturnable<Integer> cir) {
		if (!ShaderManager.INSTANCE.shadersActive()) return;
		
		if (
				renderPass == 0 ||
						Minecraft.getMinecraft(Minecraft.class).gameSettings.fancyGraphics.value != 1
		) {
			ShaderManager.INSTANCE.getDefaultShader().uniformInt(
					"flags", 1
			);
		} else {
			ShaderManager.INSTANCE.getDefaultShader().uniformInt(
					"flags", 1
			);
		}
		ShaderManager.INSTANCE.getDefaultShader().unbind();
	}
}
