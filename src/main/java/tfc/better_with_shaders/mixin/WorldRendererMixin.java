package tfc.better_with_shaders.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPhotoMode;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.camera.ICamera;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.better_with_shaders.Config;
import tfc.better_with_shaders.ShaderManager;
import tfc.better_with_shaders.feature.SunCamera;
import tfc.better_with_shaders.util.GameRendererExtensions;
import tfc.better_with_shaders.util.RenderTarget;
import tfc.better_with_shaders.util.RendererExtensions;

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

    @Shadow private float farPlaneDistance;

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
            this.mc.activeCamera = new SunCamera(mc, mc.thePlayer, mc.activeCamera);
        }
    }

    boolean extended = false;

    @Redirect(at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/GLU;gluPerspective(FFFF)V"), method = "setupCameraTransform")
    public void noPerspective(float fovy, float aspect, float zNear, float zFar) {
        if (sunProjection) {
            double div = 100;
            div *= mc.resolution.width / 2400;
            if (extended) div /= 8;
            GL11.glOrtho(
                    -(double)this.mc.resolution.width / div, (double)this.mc.resolution.width / div,
                    -(double)this.mc.resolution.height / div, (double)this.mc.resolution.height / div,
                    2, this.farPlaneDistance * 3.0F
            );
        } else {
            GLU.gluPerspective(fovy, aspect, zNear, zFar);
        }
    }
    
    int realResX = 0;
    int realResY = 0;
    
    // TODO: this isn't working properly
    @Redirect(at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glOrtho(DDDDDD)V"), method = "setupCameraTransform")
    public void noPerspective(double left, double right, double bottom, double top, double zNear, double zFar) {
        double div = 10;
        if (extended) div /= 8;
        if (sunProjection) {
            GL11.glOrtho(
                    0, (double)realResX / div,
                    0, (double)realResY / div,
                    2, this.farPlaneDistance * 3.0F
            );
        } else {
            GL11.glOrtho(
                    left, right,
                    bottom, top,
                    zNear, zFar
            );
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

    RenderTarget mainShadow = new RenderTarget();
    RenderTarget extendedShadow = new RenderTarget();

    @Inject(at = @At("HEAD"), method = "renderWorld")
    public void preDrawWorld(float renderPartialTicks, long updateRenderersUntil, CallbackInfo ci) {
        if (!ShaderManager.INSTANCE.getCapabilities().usesShadows()) return;

        if (!sunProjection) {
            int rx = mc.resolution.width;
            int ry = mc.resolution.height;
            int sx = mc.resolution.scaledWidth;
            int sy = mc.resolution.scaledHeight;
            double esx = mc.resolution.scaledWidthExact;
            double esy = mc.resolution.scaledHeightExact;

            realResX = mc.resolution.width;
            realResY = mc.resolution.height;
            
            int smRes = Config.getShadowRes();

            if (!mainShadow.isGenerated()) {
                mainShadow.setSize(smRes, smRes, false, Config.checkPrecision(true));
                extendedShadow.setSize(smRes, smRes, false, Config.checkPrecision(false));
            }

            ICamera camera = mc.activeCamera;
            if (camera instanceof SunCamera) {
                camera = null;
            }
            setupCamera(renderPartialTicks);
            this.mc.activeCamera = new SunCamera(mc, mc.thePlayer, mc.activeCamera);

            RenderTarget[] framebuffers = new RenderTarget[]{
                    mainShadow, extendedShadow
            };
            sunProjection = true;
            ((GameRendererExtensions) mc.renderGlobal).swapRender();

            mc.resolution.width = smRes;
            mc.resolution.height = smRes;
            mc.resolution.scaledWidth = smRes;
            mc.resolution.scaledHeight = smRes;
            mc.resolution.scaledWidthExact = smRes;
            mc.resolution.scaledHeightExact = smRes;

            GL11.glPushMatrix();
            setupCameraTransform(renderPartialTicks);
            ShaderManager.INSTANCE.extractSun(false, smRes, mainShadow);
            ShaderManager.INSTANCE.extractSun(true, smRes, extendedShadow);
            GL11.glPopMatrix();

            extended = false;
            for (RenderTarget framebuffer : framebuffers) {
                if (!extended && !ShaderManager.INSTANCE.getCapabilities().usesMainShadow()) {
                    extended = !extended;
                    continue;
                } else if (extended && !ShaderManager.INSTANCE.getCapabilities().usesExtendedShadow()) break;
                
                int q = extended ? 1 : 1;
                mc.resolution.width = smRes / q;
                mc.resolution.height = smRes / q;
                mc.resolution.scaledWidth = smRes / q;
                mc.resolution.scaledHeight = smRes / q;
                mc.resolution.scaledWidthExact = smRes / (double) q;
                mc.resolution.scaledHeightExact = smRes / (double) q;

                framebuffer.bind();
                GL11.glViewport(0, 0, smRes / q, smRes / q);
                GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
                renderWorld(renderPartialTicks, updateRenderersUntil);
                framebuffer.unbind();

                extended = !extended;
            }
            ((GameRendererExtensions) mc.renderGlobal).swapRender();
            ((RendererExtensions) mc.render).rebind();
            sunProjection = false;

            mc.resolution.width = rx;
            mc.resolution.height = ry;
            mc.resolution.scaledWidth = sx;
            mc.resolution.scaledHeight = sy;
            mc.resolution.scaledWidthExact = esx;
            mc.resolution.scaledHeightExact = esy;

            double renderScale = ((float) (mc.gameSettings.renderScale.value).scale);
            int width = (int) (renderScale * (double) mc.resolution.width);
            int height = (int) (renderScale * (double) mc.resolution.height);
            GL11.glViewport(0, 0, width, height);

            mc.activeCamera = camera;
        }
    }
}
