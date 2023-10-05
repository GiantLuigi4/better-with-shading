package tfc.better_with_shaders.feature;

import net.minecraft.client.render.shader.Shader;
import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import tfc.better_with_shaders.ShaderManager;
import tfc.better_with_shaders.util.RenderTarget;

public class ShaderCapabilities {
    protected boolean mainShadow = false;
    protected boolean extendedShadow = false;
    protected boolean depthTex = false;
    protected boolean skyTex = false;
    protected boolean sunDir = false;

    public boolean usesMainShadow() {
        return mainShadow;
    }

    public boolean usesExtendedShadow() {
        return extendedShadow;
    }

    public boolean usesDepthTex() {
        return depthTex;
    }

    public boolean usesSkyTex() {
        return skyTex;
    }

    public boolean usesSunDir() {
        return sunDir;
    }

    public boolean usesShadows() {
        return mainShadow || extendedShadow;
    }

    public void setup() {
        mainShadow = false;
        extendedShadow = false;
        depthTex = false;
        skyTex = false;
        sunDir = false;

        for (Shader shader : ShaderManager.INSTANCE.allShaders()) {
            if (shader == null) continue;

            mainShadow = mainShadow | shader.getUniform("shadowMap0") != -1;
            extendedShadow = extendedShadow | shader.getUniform("shadowMap1") != -1;
            depthTex = depthTex | shader.getUniform("depthtex0") != -1;
            skyTex = skyTex | shader.getUniform("skyTex") != -1;
            sunDir = sunDir | shader.getUniform("sunDir") != -1;
        }
    }
    int minTex;

    int currentTex;

    public void tex(int i) {
        minTex = currentTex = i;
    }

    public void texture(Shader sdr, String name, RenderTarget shadowMap) {
        ARBMultitexture.glActiveTextureARB(currentTex);
        shadowMap.getDepth().bind();
        int uform = sdr.getUniform(name);
        GL20.glUniform1i(uform, currentTex - ARBMultitexture.GL_TEXTURE0_ARB);
        currentTex += 1;
    }

    public void disableTex() {
        for (int i = minTex; i < currentTex; i++) {
            ARBMultitexture.glActiveTextureARB(i);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
        ARBMultitexture.glActiveTextureARB(ARBMultitexture.GL_TEXTURE0_ARB);
    }
}
