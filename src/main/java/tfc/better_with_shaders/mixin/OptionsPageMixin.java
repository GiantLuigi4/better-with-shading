package tfc.better_with_shaders.mixin;

import net.minecraft.client.gui.options.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.better_with_shaders.gui.ShaderMenu;

@Mixin(value = GuiOptionsPageBase.class, remap = false)
public class OptionsPageMixin {
    @Shadow
    static Class<? extends GuiOptionsPageBase>[] pages;

    @Shadow
    static String[] pageLanguageKeys;

    @Inject(at = @At("RETURN"), method = "<clinit>")
    private static void postInit(CallbackInfo ci) {
//        pages = new Class[]{GuiOptionsPageGeneral.class, GuiOptionsPageAudio.class, GuiOptionsPageVideo.class, GuiOptionsPageControls.class, GuiOptionsPageTexturePacks.class, GuiOptionsPageLanguages.class};
//        pageLanguageKeys = new String[]{"options.tab.general", "options.tab.audio", "options.tab.video", "options.tab.controls", "options.tab.texturepacks", "options.tab.languages"};

        Class[] npages = new Class[pages.length + 1];
        String[] npageLanguageKeys = new String[pageLanguageKeys.length + 1];
        npages[npages.length - 1] = ShaderMenu.class;
        npageLanguageKeys[npages.length - 1] = "bws.options.tab.shader";

        System.arraycopy(pages, 0, npages, 0, pages.length);
        System.arraycopy(pageLanguageKeys, 0, npageLanguageKeys, 0, pageLanguageKeys.length);

        pages = npages;
        pageLanguageKeys = npageLanguageKeys;
    }
}
