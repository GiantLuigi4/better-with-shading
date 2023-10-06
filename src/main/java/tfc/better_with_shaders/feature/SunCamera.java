package tfc.better_with_shaders.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPhotoMode;
import net.minecraft.client.render.camera.EntityCamera;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.client.render.culling.CameraFrustum;
import net.minecraft.client.render.culling.Frustum;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityLiving;
import org.lwjgl.opengl.GL11;

public class SunCamera implements ICamera {
    ICamera camera;
    
    Minecraft mc;
    Entity thePlayer;
    
    CameraFrustum frustum;
    
    public SunCamera(Minecraft mc, Entity thePlayer, ICamera camera) {
        this.camera = camera;
        this.mc = mc;
        this.frustum = new CameraFrustum(this);
        this.thePlayer = thePlayer;
    }

    @Override
    public void applyGlTransformations() {
        float celestialAngle = thePlayer.world.getCelestialAngle(0);
        
        GL11.glTranslated(0, 0, -8 * mc.gameSettings.renderDistance.value.chunks);
        if (celestialAngle * 360 > 90) {
            celestialAngle += 90 / 180f;
        }
        GL11.glRotatef((celestialAngle * 360.0F + 90), 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(80, 0.0F, 1.0F, 0.0F);
        GL11.glTranslated(0, getY(), 0);
        if (mc.currentScreen instanceof GuiPhotoMode) {
            GL11.glTranslated(0, -128, 0);
        } else {
            GL11.glTranslated(0, -Math.min(thePlayer.world.getHeightValue(
                    (int) getX(),
                    (int) getZ()
            ), getY()), 0);
        }
    }
    
    // the player should be included in the shadow map
    @Override
    public boolean showPlayer() {
        return true;
    }

    @Override
    public double getX() {
        return camera.getX();
    }

    @Override
    public double getY() {
        return camera.getY();
    }

    @Override
    public double getZ() {
        return camera.getZ();
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
    
    @Override
    public void tick() {
    }
    
    @Override
    public double getX(float f) {
        return camera.getX(f);
    }
    
    @Override
    public double getY(float f) {
        return camera.getY(f);
    }
    
    @Override
    public double getZ(float f) {
        return camera.getZ(f);
    }
    
    @Override
    public double getFov() {
        return camera.getFov();
    }
    
    @Override
    public CameraFrustum getFrustum() {
        return frustum;
    }
}
