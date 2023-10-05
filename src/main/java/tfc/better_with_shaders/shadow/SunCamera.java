package tfc.better_with_shaders.shadow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.camera.EntityCamera;
import net.minecraft.core.entity.EntityLiving;
import org.lwjgl.opengl.GL11;

public class SunCamera extends EntityCamera {
    public SunCamera(Minecraft mc, EntityLiving entity) {
        super(mc, entity);
    }

    @Override
    public void applyGlTransformations() {
        super.applyGlTransformations();
        GL11.glTranslated(0, 0, -4 * mc.gameSettings.renderDistance.value.chunks);
        GL11.glRotated(45, 1, 0, 0);
        GL11.glRotatef(80, 0.0F, 1.0F, 0.0F);
        GL11.glTranslated(0, getY(), 0);
        GL11.glTranslated(0, -Math.min(mc.thePlayer.world.getHeightValue(
                (int) mc.thePlayer.x,
                (int) mc.thePlayer.z
        ), getY()), 0);
    }

    @Override
    public double getX() {
        return super.getX();
    }

    @Override
    public boolean showPlayer() {
        return true;
    }

    @Override
    public double getY() {
        return super.getY();
    }

    @Override
    public double getZ() {
        return super.getZ();
    }

    @Override
    public double getXRot() {
        return 0;
    }

    @Override
    public double getYRot() {
        return 0;
    }

    @Override
    public double getXRot(float renderPartialTicks) {
        return getXRot();
    }

    @Override
    public double getYRot(float renderPartialTicks) {
        return getYRot();
    }
}
