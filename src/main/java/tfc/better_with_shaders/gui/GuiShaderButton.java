package tfc.better_with_shaders.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.render.FontRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.texturepack.TexturePackBase;
import org.lwjgl.opengl.GL11;

public class GuiShaderButton extends GuiButton {
    String name;
    public boolean isSelected = false;

    public GuiShaderButton(int id, int xPosition, int yPosition, int width, int height, String name) {
        super(id, xPosition, yPosition, width, height, "");
        this.name = name;
    }

    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        Tessellator tessellator = Tessellator.instance;
        FontRenderer fontRenderer = minecraft.fontRenderer;
        if (this.isSelected) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(3553);
            tessellator.startDrawingQuads();
            tessellator.setColorOpaque_I(2139127936);
            tessellator.addVertexWithUV((double) (this.xPosition - 2), (double) (this.yPosition + this.height + 2), 0.0, 0.0, 1.0);
            tessellator.addVertexWithUV((double) (this.xPosition + this.width + 2), (double) (this.yPosition + this.height + 2), 0.0, 1.0, 1.0);
            tessellator.addVertexWithUV((double) (this.xPosition + this.width + 2), (double) (this.yPosition - 2), 0.0, 1.0, 1.0);
            tessellator.addVertexWithUV((double) (this.xPosition - 2), (double) (this.yPosition - 2), 0.0, 0.0, 0.0);
            tessellator.setColorOpaque_I(0);
            tessellator.addVertexWithUV((double) (this.xPosition - 1), (double) (this.yPosition + this.height + 1), 0.0, 0.0, 1.0);
            tessellator.addVertexWithUV((double) (this.xPosition + this.width + 1), (double) (this.yPosition + this.height + 1), 0.0, 1.0, 1.0);
            tessellator.addVertexWithUV((double) (this.xPosition + this.width + 1), (double) (this.yPosition - 1), 0.0, 1.0, 1.0);
            tessellator.addVertexWithUV((double) (this.xPosition - 1), (double) (this.yPosition - 1), 0.0, 0.0, 0.0);
            tessellator.draw();
            GL11.glEnable(3553);
        }

        this.drawString(fontRenderer, name, this.xPosition + this.width / 2 - fontRenderer.getStringWidth(name) / 2, this.yPosition + 1 + fontRenderer.fontHeight / 2, 16777215);
        if (name.equals("Off")) {
            this.drawString(fontRenderer, "No shaders; vanilla renderer", this.xPosition + this.width / 2 - fontRenderer.getStringWidth("No shaders; vanilla renderer") / 2, this.yPosition + 3 + fontRenderer.fontHeight + fontRenderer.fontHeight / 2, 8421504);
        } else if (name.equals("Internal")) {
            this.drawString(fontRenderer, "A mock up of GL11's builtin shaders", this.xPosition + this.width / 2 - fontRenderer.getStringWidth("A mock up of GL11's builtin shaders") / 2, this.yPosition + 4 + fontRenderer.fontHeight + fontRenderer.fontHeight / 2, 8421504);
        }
    }
}
