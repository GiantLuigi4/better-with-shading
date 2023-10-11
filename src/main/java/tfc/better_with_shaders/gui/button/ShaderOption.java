package tfc.better_with_shaders.gui.button;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.option.FloatOption;
import net.minecraft.client.option.GameSettings;
import tfc.better_with_shaders.ShaderManager;
import tfc.better_with_shaders.preprocessor.config.CFGCategory;

public class ShaderOption extends FloatOption {
    CFGCategory category;
    float min = 0, max = 1;
    boolean isInt;

    public ShaderOption(CFGCategory category, String name, float value) {
        super(null, name, value);
        this.category = category;
    }

    public ShaderOption setRange(float min, float max) {
        this.min = min;
        this.max = max;

        value -= min;
        value /= (max - min);

        return this;
    }

    protected float actualValue() {
        return (float) (Math.round(((value * (max - min)) + min) * 100) / 100.0);
    }

    protected String valueStr() {
        if (isInt) //noinspection RedundantCast
            return String.valueOf((int) Math.round(actualValue()));
        return String.valueOf(actualValue());
    }

    public void onUpdate(GuiSlider button) {
        float av = actualValue();
        button.displayString = name + " = " + valueStr();

        button.sliderValue = ((av - min) / (max - min));
        if (isInt) {
            category.set(name, (int) av);
        } else {
            category.set(name, av);
        }
        ShaderManager.INSTANCE.reloadShader();
    }

    @Override
    public void onUpdate() {
        System.out.println("Update");
    }

    @Override
    public String getDisplayString() {
        return name + " = " + valueStr();
    }

    @Override
    public String getDisplayStringValue() {
        return name;
    }

    @Override
    public String getDisplayStringName() {
        return name;
    }

    public ShaderOption asInt(boolean b) {
        isInt = b;
        return this;
    }
}
