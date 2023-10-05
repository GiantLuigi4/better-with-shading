package tfc.better_with_shaders.util;

import net.minecraft.client.render.Framebuffer;
import net.minecraft.client.render.Texture;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

public class RenderTarget {
    protected final Framebuffer fbo = new Framebuffer();
    protected final Texture tex = new Texture();
    protected final Texture depth = new Texture();

    public RenderTarget() {
    }

    public void setSize(int width, int height, boolean color, boolean highP) {
        this.fbo.generate();

        int filterMode = GL11.GL_LINEAR;

        this.fbo.bind();
        if (color) {
            this.tex.generate();
            this.tex.bind();
            GL11.glTexImage2D(3553, 0, 6408, width, height, 0, 6408, 5121, (ByteBuffer) ((ByteBuffer) null));
            GL11.glTexParameteri(3553, 10241, filterMode);
            GL11.glTexParameteri(3553, 10240, filterMode);
            GL11.glTexParameteri(3553, 10242, 10496);
            GL11.glTexParameteri(3553, 10243, 10496);
            ARBFramebufferObject.glFramebufferTexture2D(36160, 36064, 3553, this.tex.id(), 0);
        }
        this.depth.generate();
        this.depth.bind();
        GL11.glTexImage2D(3553, 0, highP ? GL30.GL_DEPTH_COMPONENT32F : 6402, width, height, 0, 6402, 5121, (ByteBuffer) ((ByteBuffer) null));
        GL11.glTexParameteri(3553, 10241, filterMode);
        GL11.glTexParameteri(3553, 10240, filterMode);
        GL11.glTexParameteri(3553, 10242, 10496);
        GL11.glTexParameteri(3553, 10243, 10496);
        ARBFramebufferObject.glFramebufferTexture2D(36160, 36096, 3553, this.depth.id(), 0);
    }

    public Framebuffer getFbo() {
        return fbo;
    }

    public Texture getDepth() {
        return depth;
    }

    public boolean isGenerated() {
        return fbo.isGenerated();
    }

    public void bind() {
        fbo.bind();
    }

    public void unbind() {
        fbo.unbind();
    }
}
