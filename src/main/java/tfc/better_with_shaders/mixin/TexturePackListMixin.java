package tfc.better_with_shaders.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.texturepack.TexturePackBase;
import net.minecraft.client.render.texturepack.TexturePackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.better_with_shaders.Config;
import tfc.better_with_shaders.ShaderManager;

import java.io.File;

@Mixin(value = TexturePackList.class, remap = false)
public class TexturePackListMixin {
	@Inject(at = @At("RETURN"), method = "<init>")
	public void postInit(Minecraft minecraft, File file, CallbackInfo ci) {
		ShaderManager.INSTANCE.useFirstShader((TexturePackList) (Object) this, Config.getShader());
	}
	
	@Inject(at = @At("RETURN"), method = "setTexturePack")
	public void postSet(TexturePackBase newPack, CallbackInfo ci) {
		ShaderManager.INSTANCE.useFirstShader((TexturePackList) (Object) this, Config.getShader());
	}
}
