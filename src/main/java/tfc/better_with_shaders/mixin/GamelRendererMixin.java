package tfc.better_with_shaders.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.ChunkRenderer;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.better_with_shaders.ShaderManager;
import tfc.better_with_shaders.util.GameRendererExtensions;

@Mixin(value = RenderGlobal.class, remap = false)
public class GamelRendererMixin implements GameRendererExtensions {
    @Shadow private double prevSortX;
    @Shadow private double prevSortZ;
    @Shadow private double prevSortY;
    @Shadow private ChunkRenderer[] chunkRenderers;
    @Shadow private ChunkRenderer[] sortedChunkRenderers;
    @Shadow private int renderChunksWide;
    @Shadow private int renderChunksDeep;
    @Shadow private int renderChunksTall;
    @Shadow private World worldObj;
    boolean vobPresent = FabricLoader.getInstance().isModLoaded("vob");

    @Inject(at = @At("HEAD"), method = "sortAndRender")
    public void preSort(ICamera camera, int renderPass, double renderPartialTicks, CallbackInfoReturnable<Integer> cir) {
        if (!ShaderManager.INSTANCE.shadersActive()) return;

        ShaderManager.INSTANCE.getDefaultShader().bind();
        ShaderManager.INSTANCE.upload(ShaderManager.INSTANCE.getDefaultShader());
        if (
                vobPresent ||
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
                vobPresent ||
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
        ShaderManager.INSTANCE.finish();
        ShaderManager.INSTANCE.getDefaultShader().unbind();
    }

    double swapSortX = 0;
    double swapSortY = 0;
    double swapSortZ = 0;
    ChunkRenderer[] chunkRendersSwap;

    @Override
    public void swapRender() {
        double tmpX = swapSortX;
        swapSortX = prevSortX;
        prevSortX = tmpX;
        double tmpZ = swapSortZ;
        swapSortZ = prevSortZ;
        prevSortZ = tmpZ;
        double tmpY = swapSortY;
        swapSortY = prevSortY;
        prevSortY = tmpY;
        ChunkRenderer[] tmpChnks = sortedChunkRenderers;
        sortedChunkRenderers = chunkRendersSwap;
        chunkRendersSwap = tmpChnks;
    }

    @Inject(at = @At("RETURN"), method = "loadRenderers")
    public void postLoadRenderers(CallbackInfo ci) {
        if (worldObj != null) {
            this.chunkRendersSwap = new ChunkRenderer[this.renderChunksWide * this.renderChunksTall * this.renderChunksDeep];
            System.arraycopy(chunkRenderers, 0, chunkRendersSwap, 0, chunkRendersSwap.length);
        }
    }
}
