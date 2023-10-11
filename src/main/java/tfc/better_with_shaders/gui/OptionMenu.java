package tfc.better_with_shaders.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.option.FloatOption;
import net.minecraft.client.option.GameSettings;
import net.minecraft.core.util.helper.Color;
import tfc.better_with_shaders.ShaderManager;
import tfc.better_with_shaders.gui.button.IOptionButton;
import tfc.better_with_shaders.gui.button.OptionButton;
import tfc.better_with_shaders.gui.button.ShaderOption;
import tfc.better_with_shaders.gui.button.SliderButton;
import tfc.better_with_shaders.preprocessor.config.CFGCategory;

import java.util.ArrayDeque;

public class OptionMenu extends GuiScreen {
    ArrayDeque<CFGCategory> stack = new ArrayDeque<>();
    CFGCategory cat;

    public OptionMenu(GuiScreen parent, CFGCategory meow) {
        super(parent);
        this.cat = meow;
    }

    @Override
    public void initGui() {
        this.controlList.clear();
        super.initGui();

        int top = ((ShaderMenu) getParentScreen()).ttop + 10;
        int bottom = height - 34;

        int indx = 0;
        GuiButton back = new GuiButton(indx++, width / 2 - 100, bottom, 200, 20, "Back");
        controlList.add(back);
        if (stack.isEmpty()) back.displayString = "Close";
        else back.displayString = "Back";

        for (int i = 0; i < cat.getLeft().size(); i++) {
            GuiButton btn;

            Object value = cat.get(cat.getLeft().get(i));

            if (value instanceof Float || value instanceof Integer) {
                ShaderOption option = new ShaderOption(
                        cat, cat.getLeft().get(i), ((Number) value).floatValue()
                ).setRange(cat.minValue(cat.getLeft().get(i)), cat.maxValue(cat.getLeft().get(i)))
                        .asInt(value instanceof Integer);
                controlList.add(
                        btn = new SliderButton(
                                indx,
                                width / 2 - 202, top,
                                200, 20,
                                option
                        )
                );
            } else {
                controlList.add(
                        btn = new OptionButton(
                                indx,
                                width / 2 - 202, top,
                                200, 20,
                                cat.getLeft().get(i)
                        )
                );
            }
            if (!(cat.get(cat.getLeft().get(i)) instanceof CFGCategory)) {
                String val = cat.get(cat.getLeft().get(i)).toString();
                btn.displayString += " = " + val;
            }

            btn.setY(btn.getY() + (btn.getHeight() + 2) * i);
            indx++;
        }
        for (int i = 0; i < cat.getRight().size(); i++) {
            GuiButton btn;

            Object value = cat.get(cat.getRight().get(i));

            if (value instanceof Float || value instanceof Integer) {
                ShaderOption option = new ShaderOption(
                        cat, cat.getRight().get(i), ((Number) value).floatValue()
                ).setRange(cat.minValue(cat.getRight().get(i)), cat.maxValue(cat.getRight().get(i)))
                        .asInt(value instanceof Integer);
                controlList.add(
                        btn = new SliderButton(
                                indx,
                                width / 2 + 2, top,
                                200, 20,
                                option
                        )
                );
            } else {
                controlList.add(
                        btn = new OptionButton(
                                indx,
                                width / 2 + 2, top,
                                200, 20,
                                cat.getRight().get(i)
                        )
                );
            }
            if (!(value instanceof CFGCategory)) {
                String val = cat.get(cat.getRight().get(i)).toString();
                btn.displayString += " = " + val;
            }

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
    protected void buttonPressed(GuiButton guibutton) {
        super.buttonPressed(guibutton);
        if (guibutton.id == 0) {
            if (stack.isEmpty()) {
                mc.displayGuiScreen(getParentScreen());
                ShaderManager.INSTANCE.saveConfig();
                return;
            }
            cat = stack.pop();
            initGui();
            return;
        }

        if (guibutton instanceof IOptionButton) {
            String name = ((IOptionButton) guibutton).getName();
            Object value = cat.get(name);
            if (value instanceof CFGCategory) {
                stack.push(cat);
                cat = (CFGCategory) value;
                initGui();
            } else if (value instanceof Boolean) {
                cat.set(name, !((Boolean) value));
                guibutton.displayString = name + " = " + !((Boolean) value);
                ShaderManager.INSTANCE.reloadShader();
            }
        }
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
