package tfc.better_with_shaders.gui.button;

import net.minecraft.client.gui.GuiButton;

public class OptionButton extends GuiButton implements IOptionButton {
    String name;

    public OptionButton(int id, int xPosition, int yPosition, String text) {
        super(id, xPosition, yPosition, text);
        this.name = text;
    }

    public OptionButton(int id, int xPosition, int yPosition, int width, int height, String text) {
        super(id, xPosition, yPosition, width, height, text);
        this.name = text;
    }

    @Override
    public String getName() {
        return name;
    }
}
