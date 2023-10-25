package tfc.better_with_shaders.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.options.GuiButtonTexturePack;
import net.minecraft.client.gui.options.GuiOptionsPageBase;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.render.texturepack.TexturePackBase;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.helper.Color;
import net.minecraft.core.util.helper.Utils;
import tfc.better_with_shaders.ShaderManager;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ShaderMenu extends GuiOptionsPageBase {
    GuiButton btnOpenFolder = null, btnConfigure = null;
    String[] shaderPackList = new File(shaderPackDir).list((fl, n) -> new File(fl + "/" + n).isDirectory());

    public ShaderMenu(GuiScreen parent, GameSettings settings) {
        super(parent, settings);

        String[] ishaderPackList = new String[shaderPackList.length + 2];
        for (int i = 0; i < shaderPackList.length; i++) {
            ishaderPackList[i + 2] = shaderPackList[i];
        }
        ishaderPackList[0] = "Off";
        ishaderPackList[1] = "Internal";
        shaderPackList = ishaderPackList;
    }

    private static final String shaderPackDir = FabricLoader.getInstance().getGameDir() + "/shader_packs/";

    List<GuiShaderButton> texturePackButtons = new ArrayList();

    int ttop;
    int tbottom;

    @Override
    public void initGui() {
        this.controlList.clear();
        this.texturePackButtons.clear();
        super.initGui();

        try {
            Field f = GuiOptionsPageBase.class.getDeclaredField("top");
            f.setAccessible(true);
            ttop = f.getInt(this);
            f = GuiOptionsPageBase.class.getDeclaredField("bottom");
            f.setAccessible(true);
            tbottom = f.getInt(this);
        } catch (Throwable err) {
            throw new RuntimeException(err);
        }

        for (int i = 0; i < shaderPackList.length; ++i) {
            String tp = shaderPackList[i];
            GuiShaderButton button = new GuiShaderButton(1000 + i, 0, 0, 0, 0, tp);
            button.visible = false;
            this.texturePackButtons.add(button);
        }

        I18n trans = I18n.getInstance();
        this.btnOpenFolder = new GuiButton(1, this.width / 2 - 102, ttop + 4, 200 / 2, 20, trans.translateKey("options.button.openFolder"));
        this.btnConfigure = new GuiButton(1, this.width / 2 + 2, ttop + 4, 200 / 2, 20, trans.translateKey("bws.options.button.config"));
        this.controlList.add(this.btnOpenFolder);
        this.controlList.add(this.btnConfigure);
        btnConfigure.enabled = ShaderManager.INSTANCE.cfg.getCategory("options.toml") != null;
    }

    int mouseX, mouseY;

    public void drawScreen(int x, int y, float renderPartialTicks) {
        super.drawScreen(x, y, renderPartialTicks);
        this.mouseX = x;
        this.mouseY = y;
    }

    @Override
    protected void drawPageItems(int x, int y, int width) {
        if (btnOpenFolder == null) {
            Minecraft mc = Minecraft.getMinecraft(Minecraft.class);
            setWorldAndResolution(
                    mc,
                    mc.resolution.width, mc.resolution.height
            );
        }

        this.btnOpenFolder.yPosition = y + 4;
        int previousHeights = y + 4 + 20 + 6;

        for (int i = 0; i < this.texturePackButtons.size(); ++i) {
            GuiShaderButton button = this.texturePackButtons.get(i);
            button.isSelected = ShaderManager.INSTANCE.activeShader().equals(this.shaderPackList[i]);
            button.width = width;
            button.height = 32;
            button.xPosition = x + width - button.width;
            button.yPosition = previousHeights;
            button.visible = true;
            button.drawButton(this.mc, this.mouseX, this.mouseY);
            previousHeights += button.height + 3;
        }
    }

    @Override
    protected int getTotalPageHeight() {
        return 20;
    }

    protected void buttonPressed(GuiButton guibutton) {
        super.buttonPressed(guibutton);
        if (guibutton == this.btnOpenFolder) {
            File minecraftDir = Minecraft.getMinecraft(Minecraft.class).getMinecraftDir();
            Utils.openDirectory(new File(minecraftDir, "shader_packs"));
        } else if (guibutton == this.btnConfigure) {
            mc.displayGuiScreen(new OptionMenu(this, ShaderManager.INSTANCE.cfg.getCategory("options.toml")));
        } else {
            if (guibutton instanceof GuiShaderButton) {
                ShaderManager.INSTANCE.useShader(((GuiShaderButton) guibutton).name);
                btnConfigure.enabled = ShaderManager.INSTANCE.cfg.getCategory("options.toml") != null;
            }
        }
    }

    @Override
    public void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        if (y > ttop && y < tbottom) {
            for (GuiShaderButton texturePackButton : texturePackButtons) {
                if (y > texturePackButton.yPosition && y < texturePackButton.yPosition + texturePackButton.height) {
                    buttonPressed(texturePackButton);
                }
            }
        }
    }

    @Override
    public void drawDefaultBackground() {
        if (mc.currentScreen == this || mc.theWorld == null)
            super.drawDefaultBackground();
        else drawGameBg();
    }

    public void drawGameBg() {
        int color = (this.mc.gameSettings.guiBackgroundColor.value).getARGB();
        this.drawRect(0, 0, this.width, this.ttop, color);
        this.drawRect(0, this.tbottom, this.width, this.height, color);

        if (mc.currentScreen == this) {
            this.drawRect(0, 0, this.width, this.ttop, 1593835520);
            this.drawRect(0, this.tbottom, this.width, this.height, 1593835520);
        }
    }

    public void drawOverlayBg() {
        int color = (this.mc.gameSettings.guiBackgroundColor.value).getARGB();
        this.drawRect(0, this.ttop, this.width, this.tbottom, color);

        if (mc.currentScreen != this) {
            this.drawRect(0, 0, this.width, this.ttop, 1593835520);
            this.drawRect(0, this.tbottom, this.width, this.height, 1593835520);
        }
    }
}
