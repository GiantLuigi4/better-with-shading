package tfc.better_with_shaders.mixin;

import net.minecraft.client.render.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.better_with_shaders.util.FramebufferAccessor;

@Mixin(Framebuffer.class)
public class FramebufferMixin implements FramebufferAccessor {
    @Shadow private int id;

    @Override
    public int getId() {
        return id;
    }
}
