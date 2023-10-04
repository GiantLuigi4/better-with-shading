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
//        GL11.glTranslated(0, 0, -16 * mc.gameSettings.renderDistance.value.chunks * 3);
        GL11.glTranslated(0, 0, -16 * mc.gameSettings.renderDistance.value.chunks * 0.5);
//        GL11.glTranslated(0, 0, -16 * 3);
    }

    @Override
    public double getX() {
        return super.getX() + 100;
    }

    @Override
    public boolean showPlayer() {
        return true;
    }

    @Override
    public double getY() {
        return super.getY() + 100;
    }

    @Override
    public double getZ() {
        return super.getZ();
    }

    @Override
    public double getXRot() {
        return 45;
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
