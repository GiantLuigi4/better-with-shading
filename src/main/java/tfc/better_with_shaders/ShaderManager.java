package tfc.better_with_shaders;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.GLAllocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Framebuffer;
import net.minecraft.client.render.OpenGLHelper;
import net.minecraft.client.render.Renderer;
import net.minecraft.client.render.Texture;
import net.minecraft.client.render.shader.Shader;
import net.minecraft.client.render.shader.Shaders;
import net.minecraft.client.render.texturepack.TexturePackBase;
import net.minecraft.client.render.texturepack.TexturePackList;
import net.minecraft.core.util.phys.Vec3d;
import org.lwjgl.opengl.*;
import tfc.better_with_shaders.feature.ShaderCapabilities;
import tfc.better_with_shaders.preprocessor.ConfigProcessor;
import tfc.better_with_shaders.preprocessor.IncludeProcessor;
import tfc.better_with_shaders.preprocessor.Processor;
import tfc.better_with_shaders.preprocessor.config.ConfigLoader;
import tfc.better_with_shaders.util.FramebufferAccessor;
import tfc.better_with_shaders.util.RenderTarget;
import tfc.better_with_shaders.util.RendererExtensions;
import turniplabs.halplibe.util.ConfigHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Properties;

public class ShaderManager {
    private static final String shaderPackDir = FabricLoader.getInstance().getGameDir() + "/shader_packs/";
    private final Minecraft mc = Minecraft.getMinecraft(Minecraft.class);
    public static final ShaderManager INSTANCE = new ShaderManager();


    // shaders
    protected Shader DEFAULT;
    protected Shader ENTITY;

    protected Shader FINAL;

    public Shader getDefaultShader() {
        return DEFAULT;
    }

    public Shader getEntitytShader() {
        return ENTITY;
    }

    // config data
    String activePack = null;

    HashMap<String, String> fallbacks = new HashMap<>();

    Processor[] processors;

    public final ConfigLoader cfg = new ConfigLoader();

    public ShaderManager() {
        if (!new File(shaderPackDir).exists())
            new File(shaderPackDir).mkdirs();

        fallbacks.put("entity", "base");

        processors = new Processor[]{
                new IncludeProcessor(this::read),
                new ConfigProcessor(cfg)
        };
    }

    public void useShader(String name) {
        if (activePack != null) {
            if (!activePack.equals(name)) {
                activePack = name;
                Config.setProp("shader", name);
                Config.writeConfig();
            }
        } else activePack = name;
        init(mc.texturePackList, true);
    }

    boolean loadingCore = true;

    protected InputStream getStream(String fl) {
        InputStream is = null;

        if (!activePack.equals("internal")) {
            File flf = new File(shaderPackDir + activePack + (loadingCore ? "/core/" : "/post/") + fl);

            if (flf.exists()) {
                try {
                    is = new FileInputStream(flf);
                } catch (Throwable ignored) {
                }
            }
        }

        if (is == null) is = base.getResourceAsStream("/assets/shaders/" + fl);
        if (is == null) is = def.getResourceAsStream("/assets/shaders/" + fl);

        return is;
    }

    TexturePackBase base;
    TexturePackBase def;

    protected String read(String string, String name) {
        String ext = ".fsh";
        if (string.endsWith(".vsh")) ext = ".vsh";
        else if (string.endsWith(".glsl")) ext = ".glsl";
        else if (string.endsWith(".gsh")) ext = ".gsh";

        try {
            InputStream is = getStream(name + ext);

            if (loadingCore && is == null) {
                // attempt to load default shader
                is = getStream(fallbacks.get(name) + ext);
            }

            byte[] data = new byte[is.available()];
            is.read(data);

            try {
                is.close();
            } catch (Throwable ignored) {
            }

            return new String(data);
        } catch (Throwable err) {
            throw new RuntimeException("Could not find resource " + name + ext, err);
        }
    }

    protected String readAndProcess(String string, String name) {
        String txt = read(string, name);
        for (Processor processor : processors) {
            txt = processor.modify(txt);
        }
        return txt;
    }

    public void init(TexturePackList list, boolean loadConfig) {
        if (loadConfig)
            cfg.dump();

        if (DEFAULT != null) {
            for (Shader shader : allShaders()) shader.delete();
        } else {
            DEFAULT = new Shader();
            ENTITY = new Shader();
            FINAL = new Shader();
        }

        base = list.selectedTexturePack;
        def = list.getDefaultTexturePack();

        if (activePack.equals("Off")) {
            DEFAULT = null;
            ENTITY = null;
            FINAL = null;
            ((RendererExtensions) mc.render).disableShader();
            return;
        }

        if (loadConfig) {
            cfg.load((file) -> {
                if (activePack.equals("Internal")) return null;

                File flf = new File(shaderPackDir + activePack + "/" + file);
                if (flf.exists()) {
                    try {
                        FileInputStream fis = new FileInputStream(flf);
                        byte[] data = new byte[fis.available()];
                        fis.read(data);
                        try {
                            fis.close();
                        } catch (Throwable ignored) {
                        }
                        return new String(data);
                    } catch (Throwable err) {
                        err.printStackTrace();
                    }
                }
                return null;
            });

            loadConfig();
        }

        loadingCore = true;
        DEFAULT.compile(string -> this.readAndProcess(string, "base"), "base");
        ENTITY.compile(string -> this.readAndProcess(string, "entity"), "entity");
        loadingCore = false;

        capabilities.setup();

        ((RendererExtensions) mc.render).disableShader();
        InputStream is = getStream("final.fsh");
        if (is != null) {
            try {
                is.close();
            } catch (Throwable ignored) {
            }
        } else return;
        is = getStream("final.vsh");
        if (is != null) {
            try {
                is.close();
            } catch (Throwable ignored) {
            }
        } else return;
        FINAL.compile(string -> this.readAndProcess(string, "final"), "post");
        ((RendererExtensions) mc.render).enableShader(FINAL);

        capabilities.setup();
    }

    public boolean shadersActive() {
        return DEFAULT != null;
    }

    public void useFirstShader(TexturePackList list, String name) {
        activePack = name;
        init(list, true);
    }

    protected final ShaderCapabilities capabilities = new ShaderCapabilities();

    public ShaderCapabilities getCapabilities() {
        return capabilities;
    }

    private final FloatBuffer _proj = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer _modl = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer _camModl = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer _camProj = GLAllocation.createDirectFloatBuffer(16);

    private int smRes;
    private RenderTarget shadowMap;
    private RenderTarget shadowMap1;

    public void extractSun(boolean extended, int smRes, RenderTarget framebuffer) {
        if (!extended) {
            this._modl.clear();
            GL11.glGetFloat(2982, this._modl);
            this._modl.position(0).limit(16);

            this._proj.clear();
            GL11.glGetFloat(2983, this._proj);
            this._proj.position(0).limit(16);

            this.smRes = smRes;
            this.shadowMap = framebuffer;
        } else {
            shadowMap1 = framebuffer;
        }
    }

    public void upload(Shader sdr) {
        sdr.uniformInt("shadowResolution", smRes);

        capabilities.tex(ARBMultitexture.GL_TEXTURE1_ARB);
        if (capabilities.usesMainShadow()) capabilities.texture(sdr, "shadowMap0", shadowMap.getDepth());
        if (capabilities.usesExtendedShadow()) capabilities.texture(sdr, "shadowMap1", shadowMap1.getDepth());
        ARBMultitexture.glActiveTextureARB(ARBMultitexture.GL_TEXTURE0_ARB);

        GL20.glUniformMatrix4(
                sdr.getUniform("camMatrix"), false,
                _camModl
        );
        GL20.glUniformMatrix4(
                sdr.getUniform("projectionMatrix"), false,
                _camProj
        );
        if (capabilities.usesShadows()) {
            GL20.glUniformMatrix4(
                    sdr.getUniform("sunCameraMatrix"), false,
                    _modl
            );
            GL20.glUniformMatrix4(
                    sdr.getUniform("sunProjectionMatrix"), false,
                    _proj
            );
        }
        int uform = sdr.getUniform("skyColor");
        if (uform != -1) {
            Vec3d skyCol = mc.theWorld.getSkyColor(mc.activeCamera, 1);

            Minecraft mc = Minecraft.getMinecraft(Minecraft.class);
            float celestialAngle = mc.theWorld.getCelestialAngle(0);
            float[] col = mc.theWorld.getWorldType().getSunriseColor(
                    celestialAngle,
                    1
            );
            if (col == null) {
                GL20.glUniform3f(
                        uform,
                        (float) skyCol.xCoord, (float) skyCol.yCoord, (float) skyCol.zCoord
                );
            } else {
                for (int i = 0; i < 3; i++) {
                    col[i] *= col[3] * 2;
                }
                col[0] += (float) skyCol.xCoord * (1 - col[3]);
                col[1] += (float) skyCol.yCoord * (1 - col[3]);
                col[2] += (float) skyCol.zCoord * (1 - col[3]);

                GL20.glUniform3f(
                        uform,
                        (float) col[0], (float) col[1], (float) col[2]
                );
            }
        }
        if (capabilities.usesSunDir()) {
            uform = sdr.getUniform("sunDir");

            if (uform != -1) {
                float celestialAngle = mc.theWorld.getCelestialAngle(0);
                celestialAngle += 90 / 180f;
                celestialAngle = (float) Math.toRadians(celestialAngle * 360);

                GL20.glUniform3f(uform,
                        (float) Math.sin(celestialAngle),
                        (float) -Math.cos(celestialAngle),
                        0
                );
            }
        }
    }

    public void finish() {
        capabilities.disableTex();
    }

    public void extractCamera() {
        this._camModl.clear();
        GL11.glGetFloat(2982, this._camModl);
        this._camModl.position(0).limit(16);
        this._camProj.clear();
        GL11.glGetFloat(2983, this._camProj);
        this._camProj.position(0).limit(16);
    }

    public Shader[] allShaders() {
        return new Shader[]{
                DEFAULT, ENTITY, FINAL
        };
    }

    public static void drawShader(Shader bwsPostShader, Minecraft mc, Framebuffer gameFramebuffer, Framebuffer gameFramebufferSwap, Texture gameFramebufferTexSwap, Texture gameFramebufferDepthSwap, Framebuffer worldFramebuffer, Texture worldFramebufferTex, Texture worldFramebufferDepth, float partialTicks) {
        double renderScale = ((float) (mc.gameSettings.renderScale.value).scale);
        int width = (int) (renderScale * (double) mc.resolution.width);
        int height = (int) (renderScale * (double) mc.resolution.height);

        OpenGLHelper.checkError("pre bws post shader");
        GL11.glViewport(0, 0, width, height);
        gameFramebufferSwap.bind();
        if (bwsPostShader.isEnabled()) {
            bwsPostShader.bind();

            ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_READ_FRAMEBUFFER, ((FramebufferAccessor) gameFramebuffer).getId());
            ARBFramebufferObject.glBlitFramebuffer(
                    0, 0, width, height,
                    0, 0, width, height,
                    GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST
            );
            ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_READ_FRAMEBUFFER, 0);

            GL11.glDepthMask(false);

            ARBMultitexture.glActiveTextureARB(33984);
//            worldFramebufferTex.bind();
//            bwsPostShader.uniformInt("colortex0", 0);
//            ARBMultitexture.glActiveTextureARB(33985);
//            worldFramebufferDepth.bind();
//            bwsPostShader.uniformInt("depthtex0", 1);

            ARBMultitexture.glActiveTextureARB(33984);
            mc.ppm.enabled = true;
            Shaders.setUniforms(mc, bwsPostShader, partialTicks);
            INSTANCE.upload(bwsPostShader);
            INSTANCE.capabilities.texture(
                    bwsPostShader, "colortex0", worldFramebufferTex
            );
            INSTANCE.capabilities.texture(
                    bwsPostShader, "depthtex0", worldFramebufferDepth
            );
            bwsPostShader.uniformFloat("intensity", (float) renderScale);
            mc.ppm.enabled = false;
            Shaders.drawFullscreenRect();
            INSTANCE.finish();
            GL20.glUseProgram(0);
        }
        ARBMultitexture.glActiveTextureARB(ARBMultitexture.GL_TEXTURE1_ARB);
        worldFramebufferDepth.unbind();
        ARBMultitexture.glActiveTextureARB(ARBMultitexture.GL_TEXTURE0_ARB);
        worldFramebufferTex.unbind();

        worldFramebuffer.bind();
        ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_READ_FRAMEBUFFER, ((FramebufferAccessor) gameFramebufferSwap).getId());
        ARBFramebufferObject.glBlitFramebuffer(
                0, 0, width, height,
                0, 0, width, height,
                GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST
        );
        ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_READ_FRAMEBUFFER, 0);
        worldFramebuffer.unbind();

        GL11.glDepthMask(true);

        OpenGLHelper.checkError("bws post shader");
    }

    public void reloadShader() {
        init(mc.texturePackList, false);
    }

    public String activeShader() {
        return activePack;
    }

    public void loadConfig() {
        if (new File(shaderPackDir + "/" + activePack + ".cfg").exists()) {
            Properties properties = new Properties();
            this.cfg.values.forEach((k, v) -> {
                properties.put(k, String.valueOf(v));
            });

            ConfigHandler hndlr = new ConfigHandler("../shader_packs/" + activePack, properties);
            this.cfg.values.forEach((k, v) -> {
                if (properties.containsKey(k)) {
                    try {
                        if (v.getClass().equals(Boolean.class)) {
                            this.cfg.values.replace(k, Boolean.parseBoolean(hndlr.getString(k)));
                        } else if (v.getClass().equals(Float.class)) {
                            this.cfg.values.replace(k, Float.parseFloat(hndlr.getString(k)));
                        } else if (v.getClass().equals(Integer.class)) {
                            this.cfg.values.replace(k, Integer.parseInt(hndlr.getString(k)));
                        }
                    } catch (Throwable err) {
                        err.printStackTrace();
                    }
                }
            });
        }
    }

    public void saveConfig() {
        Properties properties = new Properties();
        ConfigHandler cfg = new ConfigHandler("../shader_packs/" + activePack, properties);
        this.cfg.values.forEach((k, v) -> {
            properties.put(k, String.valueOf(v));
        });
        cfg.writeDefaultConfig();
    }
}
