package tfc.better_with_shaders.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.better_with_shaders.gui.OptionMenu;

@Mixin(value = Minecraft.class, remap = false)
public class MenuCloseRedirectionCodeMixin {
    @Shadow public GuiScreen currentScreen;

    @Inject(at = @At("HEAD"), method = "displayGuiScreen", cancellable = true)
    public void preDisplayScreen(GuiScreen guiscreen, CallbackInfo ci) {
        if (currentScreen instanceof OptionMenu) {
            currentScreen = currentScreen.getParentScreen();
            ci.cancel();
        }
    }
}
