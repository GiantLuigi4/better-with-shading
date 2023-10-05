package tfc.better_with_shaders.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.option.enums.RenderScale;
import net.minecraft.client.render.Framebuffer;
import net.minecraft.client.render.OpenGLHelper;
import net.minecraft.client.render.Renderer;
import net.minecraft.client.render.Texture;
import net.minecraft.client.render.shader.Shader;
import net.minecraft.client.render.shader.Shaders;
import net.minecraft.client.render.shader.ShadersRenderer;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.better_with_shaders.ShaderManager;
import tfc.better_with_shaders.util.FramebufferAccessor;
import tfc.better_with_shaders.util.RendererExtensions;

import java.nio.ByteBuffer;

@Mixin(value = ShadersRenderer.class, remap = false)
public class ShadersRendererMixin implements RendererExtensions {
    @Shadow
    @Final
    protected Framebuffer worldFramebuffer;

    @Shadow
    @Final
    protected Texture gameFramebufferTex;

    @Unique
    protected Shader bwsPostShader;

    @Shadow
    public int fbWidth;

    @Shadow
    public int fbHeight;

    @Shadow
    @Final
    protected Framebuffer gameFramebuffer;

    @Shadow
    @Final
    protected Texture gameFramebufferDepth;

    @Shadow
    @Final
    protected Texture worldFramebufferDepth;

    @Shadow
    @Final
    protected Texture worldFramebufferTex;

    public void rebind() {
        worldFramebuffer.bind();
    }

    protected final Framebuffer gameFramebufferSwap = new Framebuffer();
    protected final Texture gameFramebufferTexSwap = new Texture();
    protected final Texture gameFramebufferDepthSwap = new Texture();

    @Inject(at = @At("HEAD"), method = "setupFramebuffer")
    public void preUpdateFbo(CallbackInfo ci) {
        Minecraft mc = ((Renderer) (Object) this).mc;

        double renderScale = ((float) (mc.gameSettings.renderScale.value).scale);
        int scaledWidth = (int) (renderScale * (double) mc.resolution.width);
        int scaledHeight = (int) (renderScale * (double) mc.resolution.height);
        if (this.fbWidth != mc.resolution.width || this.fbHeight != mc.resolution.height || ((Renderer) (Object) this).renderWidth != scaledWidth || ((Renderer) (Object) this).renderHeight != scaledHeight || !this.gameFramebufferSwap.isGenerated()) {
            this.gameFramebufferSwap.generate();
            this.gameFramebufferTexSwap.generate();
            this.gameFramebufferDepthSwap.generate();

            this.gameFramebufferSwap.bind();
            this.gameFramebufferTexSwap.bind();

            boolean filtering = mc.gameSettings.renderScale.value.useLinearFiltering;
            int filterMode = filtering ? 9729 : 9728;

            GL11.glTexImage2D(3553, 0, 6408, scaledWidth, scaledHeight, 0, 6408, 5121, (ByteBuffer) ((ByteBuffer) null));
            GL11.glTexParameteri(3553, 10241, filterMode);
            GL11.glTexParameteri(3553, 10240, filterMode);
            GL11.glTexParameteri(3553, 10242, 10496);
            GL11.glTexParameteri(3553, 10243, 10496);
            ARBFramebufferObject.glFramebufferTexture2D(36160, 36064, 3553, this.gameFramebufferTexSwap.id(), 0);
            this.gameFramebufferDepthSwap.bind();
            GL11.glTexImage2D(3553, 0, 6402, scaledWidth, scaledHeight, 0, 6402, 5121, (ByteBuffer) ((ByteBuffer) null));
            GL11.glTexParameteri(3553, 10241, filterMode);
            GL11.glTexParameteri(3553, 10240, filterMode);
            GL11.glTexParameteri(3553, 10242, 10496);
            GL11.glTexParameteri(3553, 10243, 10496);
            ARBFramebufferObject.glFramebufferTexture2D(36160, 36096, 3553, this.gameFramebufferDepthSwap.id(), 0);
            ARBFramebufferObject.glBindFramebuffer(36160, 0);
        }
    }

    @Inject(at = @At("HEAD"), method = "endRenderWorld", cancellable = true)
    public void preRender(float partialTicks, CallbackInfo ci) {
        if (bwsPostShader == null) return;

        Minecraft mc = ((Renderer) (Object) this).mc;

        OpenGLHelper.checkError("pre end render game");
        GL11.glViewport(0, 0, this.fbWidth, this.fbHeight);
        gameFramebufferSwap.bind();
        if (this.bwsPostShader.isEnabled()) {
            this.bwsPostShader.bind();

            ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_READ_FRAMEBUFFER, ((FramebufferAccessor) gameFramebuffer).getId());
            ARBFramebufferObject.glBlitFramebuffer(
                    0, 0, fbWidth, fbHeight,
                    0, 0, fbWidth, fbHeight,
                    GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST
            );
            ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_READ_FRAMEBUFFER, 0);

            GL11.glDepthMask(false);

            ARBMultitexture.glActiveTextureARB(33984);
            this.worldFramebufferTex.bind();
            this.bwsPostShader.uniformInt("colortex0", 0);
            ARBMultitexture.glActiveTextureARB(33985);
            this.worldFramebufferDepth.bind();
            this.bwsPostShader.uniformInt("depthtex0", 1);

            ARBMultitexture.glActiveTextureARB(33984);
            mc.ppm.enabled = true;
            Shaders.setUniforms(mc, this.bwsPostShader, partialTicks);
            mc.ppm.enabled = false;
            Shaders.drawFullscreenRect();
            GL20.glUseProgram(0);
        }
        ARBMultitexture.glActiveTextureARB(ARBMultitexture.GL_TEXTURE1_ARB);
        this.worldFramebufferDepth.unbind();
        ARBMultitexture.glActiveTextureARB(ARBMultitexture.GL_TEXTURE0_ARB);
        this.worldFramebufferTex.unbind();

        worldFramebuffer.bind();
        ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_READ_FRAMEBUFFER, ((FramebufferAccessor) gameFramebufferSwap).getId());
        ARBFramebufferObject.glBlitFramebuffer(
                0, 0, fbWidth, fbHeight,
                0, 0, fbWidth, fbHeight,
                GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST
        );
        ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_READ_FRAMEBUFFER, 0);
        worldFramebuffer.unbind();

        GL11.glDepthMask(true);

        OpenGLHelper.checkError("bws post shader");
    }

    @Override
    public void disableShader() {
        bwsPostShader = null;
    }

    @Override
    public void enableShader(Shader sdr) {
        bwsPostShader = sdr;
    }
}
