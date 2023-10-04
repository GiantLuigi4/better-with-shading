package tfc.better_with_shaders.mixin;

import net.minecraft.client.GLAllocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Framebuffer;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.client.render.shader.ShadersRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.better_with_shaders.ShaderManager;
import tfc.better_with_shaders.shadow.SunCamera;
import tfc.better_with_shaders.util.GameRendererExtensions;
import tfc.better_with_shaders.util.RenderTarget;
import tfc.better_with_shaders.util.RendererExtensions;

import java.nio.FloatBuffer;

@Mixin(value = WorldRenderer.class, remap = false, priority = 999)
public abstract class WorldRendererMixin {
    @Shadow
    private Minecraft mc;

    @Shadow
    public abstract void renderWorld(float renderPartialTicks, long updateRenderersUntil);

    @Shadow
    protected abstract void setupCamera(float renderPartialTicks);

    @Shadow
    protected abstract void setupCameraTransform(float renderPartialTicks);

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderGlobal;callAllDisplayLists(ID)V"), require = 0)
    public void itsAlreadyCalled(RenderGlobal instance, int pass, double pct) {
        if (ShaderManager.INSTANCE.shadersActive()) {
            ShaderManager.INSTANCE.getDefaultShader().bind();
            ShaderManager.INSTANCE.upload(ShaderManager.INSTANCE.getDefaultShader());
            instance.callAllDisplayLists(pass, pct);
            ShaderManager.INSTANCE.finish();
            ShaderManager.INSTANCE.getDefaultShader().unbind();
        } else {
            instance.callAllDisplayLists(pass, pct);
        }
    }

    boolean sunProjection = false;

    @Inject(at = @At("TAIL"), method = "setupCamera")
    public void postSetupCam(float renderPartialTicks, CallbackInfo ci) {
        if (sunProjection) {
            this.mc.activeCamera = new SunCamera(mc, mc.thePlayer);
        }
    }

    @Inject(at = @At("TAIL"), method = "setupCameraTransform")
    public void postTransform(float renderPartialTicks, CallbackInfo ci) {
        ShaderManager.INSTANCE.extractCamera();
    }

    @Inject(at = @At("RETURN"), method = "renderWorld")
    public void postDrawWorld(float renderPartialTicks, long updateRenderersUntil, CallbackInfo ci) {
    }

    @Inject(at = @At("HEAD"), method = "getFOVModifier", cancellable = true)
    public void switchFov(float renderPartialTicks, boolean isModifiedByFOV, CallbackInfoReturnable<Float> cir) {
        if (sunProjection) {
            cir.setReturnValue(30f);
        }
    }

    @Inject(at = @At("HEAD"), method = "setupPlayerCamera", cancellable = true)
    public void prePlayerCam(float renderPartialTicks, CallbackInfo ci) {
        if (sunProjection) ci.cancel();
    }

    RenderTarget framebuffer = new RenderTarget();

    @Inject(at = @At("HEAD"), method = "renderWorld")
    public void preDrawWorld(float renderPartialTicks, long updateRenderersUntil, CallbackInfo ci) {
        if (!sunProjection) {
            int rx = mc.resolution.width;
            int ry = mc.resolution.height;
            int sx = mc.resolution.scaledWidth;
            int sy = mc.resolution.scaledHeight;
            double esx = mc.resolution.scaledWidthExact;
            double esy = mc.resolution.scaledHeightExact;

            int smRes = 1200;

            if (!framebuffer.isGenerated()) {
                framebuffer.setSize(smRes, smRes, false);
            }

            mc.resolution.width = smRes;
            mc.resolution.height = smRes;
            mc.resolution.scaledWidth = smRes;
            mc.resolution.scaledHeight = smRes;
            mc.resolution.scaledWidthExact = smRes;
            mc.resolution.scaledHeightExact = smRes;

            ICamera camera = mc.activeCamera;
            if (camera instanceof SunCamera) {
                camera = null;
            }
            this.mc.activeCamera = new SunCamera(mc, mc.thePlayer);

            sunProjection = true;
            framebuffer.bind();
            GL11.glViewport(0, 0, smRes, smRes);
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
            ((GameRendererExtensions) mc.renderGlobal).swapRender();

            GL11.glPushMatrix();
            setupCameraTransform(renderPartialTicks);
            ShaderManager.INSTANCE.extractSun(smRes, framebuffer);
            GL11.glPopMatrix();

            renderWorld(renderPartialTicks, updateRenderersUntil);
            ((GameRendererExtensions) mc.renderGlobal).swapRender();
            framebuffer.unbind();
            ((RendererExtensions) mc.render).rebind();
            sunProjection = false;

            mc.resolution.width = rx;
            mc.resolution.height = ry;
            mc.resolution.scaledWidth = sx;
            mc.resolution.scaledHeight = sy;
            mc.resolution.scaledWidthExact = esx;
            mc.resolution.scaledHeightExact = esy;
            GL11.glViewport(0, 0, mc.resolution.width, mc.resolution.height);

            mc.activeCamera = camera;
        }
    }
}
