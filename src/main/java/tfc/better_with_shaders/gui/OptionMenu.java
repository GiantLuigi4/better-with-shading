package tfc.better_with_shaders.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.core.util.helper.Color;
import tfc.better_with_shaders.ShaderManager;
import tfc.better_with_shaders.preprocessor.config.CFGCategory;

public class OptionMenu extends GuiScreen {
    public OptionMenu(GuiScreen parent) {
        super(parent);
    }

    @Override
    public void initGui() {
        this.controlList.clear();
        super.initGui();

        int top = ((ShaderMenu) getParentScreen()).ttop;
        CFGCategory category = ShaderManager.INSTANCE.cfg.getCategory("options.toml");
        int indx = 0;
        for (int i = 0; i < category.getLeft().size(); i++) {
            GuiButton btn;
            controlList.add(
                    btn = new GuiButton(
                            indx,
                            width / 2 - 202, top,
                            200, 20,
                            category.getLeft().get(i)
                    )
            );
            btn.setY(btn.getY() + (btn.getHeight() + 2) * i);
            indx++;
        }
        for (int i = 0; i < category.getRight().size(); i++) {
            GuiButton btn;
            controlList.add(
                    btn = new GuiButton(
                            indx,
                            width / 2 + 2, top,
                            200, 20,
                            category.getRight().get(i)
                    )
            );
            btn.setY(btn.getY() + (btn.getHeight() + 2) * i);
            indx++;
        }
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);
        getParentScreen().setWorldAndResolution(mc, width, height);
    }

    @Override
    public void drawScreen(int x, int y, float renderPartialTicks) {
        getParentScreen().drawScreen(x, y, renderPartialTicks);
        drawDefaultBackground();
        super.drawScreen(x, y, renderPartialTicks);
    }

    @Override
    public void drawDefaultBackground() {
        ((ShaderMenu) getParentScreen()).drawOverlayBg();
    }
}
