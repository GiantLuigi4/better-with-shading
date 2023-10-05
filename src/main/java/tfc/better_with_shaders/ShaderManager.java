package tfc.better_with_shaders;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.GLAllocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.shader.Shader;
import net.minecraft.client.render.texturepack.TexturePackBase;
import net.minecraft.client.render.texturepack.TexturePackList;
import org.lwjgl.opengl.*;
import tfc.better_with_shaders.preprocessor.ConfigProcessor;
import tfc.better_with_shaders.preprocessor.IncludeProcessor;
import tfc.better_with_shaders.preprocessor.Processor;
import tfc.better_with_shaders.util.RenderTarget;
import tfc.better_with_shaders.util.RendererExtensions;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;

public class ShaderManager {
    private static final String shaderPackDir = FabricLoader.getInstance().getGameDir() + "/shader_packs/";
    private final Minecraft mc = Minecraft.getMinecraft(Minecraft.class);
    public static final ShaderManager INSTANCE = new ShaderManager();


    // shaders
    protected Shader DEFAULT;
    protected Shader ENTITY;

    protected Shader POST;

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

    public ShaderManager() {
        if (!new File(shaderPackDir).exists())
            new File(shaderPackDir).mkdirs();

        fallbacks.put("entity", "base");

        processors = new Processor[]{
                new IncludeProcessor(this::read),
                new ConfigProcessor()
        };
    }

    public void useShader(String name) {
        if (activePack != null) {
            activePack = name;
//            Config.setProp("shader", name);
//            Config.writeConfig();
        } else activePack = name;
        init(mc.texturePackList);
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

    public void init(TexturePackList list) {
        if (DEFAULT != null) {
            DEFAULT.delete();
            ENTITY.delete();
            POST.delete();
        } else {
            DEFAULT = new Shader();
            ENTITY = new Shader();
            POST = new Shader();
        }

        base = list.selectedTexturePack;
        def = list.getDefaultTexturePack();

        if (activePack.equals("none")) {
            DEFAULT = null;
            ENTITY = null;
            POST = null;
            ((RendererExtensions) mc.render).disableShader();
            return;
        }

        loadingCore = true;
        DEFAULT.compile(string -> this.readAndProcess(string, "base"), "base");
        ENTITY.compile(string -> this.readAndProcess(string, "entity"), "entity");
        loadingCore = false;

        ((RendererExtensions) mc.render).disableShader();
        InputStream is = getStream("base.fsh");
        if (is != null) {
            try {
                is.close();
            } catch (Throwable ignored) {
            }
        } else return;
        is = getStream("base.vsh");
        if (is != null) {
            try {
                is.close();
            } catch (Throwable ignored) {
            }
        } else return;
        POST.compile(string -> this.readAndProcess(string, "base"), "post");
        ((RendererExtensions) mc.render).enableShader(POST);
    }

    public boolean shadersActive() {
        return DEFAULT != null;
    }

    public void useFirstShader(TexturePackList list, String name) {
        if (activePack == null) {
            activePack = name;
            init(list);
        }
    }

    private final FloatBuffer _proj = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer _modl = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer _camModl = GLAllocation.createDirectFloatBuffer(16);

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

        ARBMultitexture.glActiveTextureARB(ARBMultitexture.GL_TEXTURE1_ARB);
        shadowMap.getDepth().bind();
        sdr.uniformInt("shadowMap0", 1);

        ARBMultitexture.glActiveTextureARB(ARBMultitexture.GL_TEXTURE2_ARB);
        shadowMap1.getDepth().bind();
        sdr.uniformInt("shadowMap1", 2);

        ARBMultitexture.glActiveTextureARB(ARBMultitexture.GL_TEXTURE0_ARB);

        GL20.glUniformMatrix4(
                sdr.getUniform("camMatrix"), false,
                _camModl
        );
        GL20.glUniformMatrix4(
                sdr.getUniform("sunCameraMatrix"), false,
                _modl
        );
        GL20.glUniformMatrix4(
                sdr.getUniform("sunProjectionMatrix"), false,
                _proj
        );
    }

    public void finish() {
        ARBMultitexture.glActiveTextureARB(ARBMultitexture.GL_TEXTURE1_ARB);
        GL11.glBindTexture(3553, 0);
        ARBMultitexture.glActiveTextureARB(ARBMultitexture.GL_TEXTURE2_ARB);
        GL11.glBindTexture(3553, 0);
        ARBMultitexture.glActiveTextureARB(ARBMultitexture.GL_TEXTURE0_ARB);
    }

    public void extractCamera() {
        this._camModl.clear();
        GL11.glGetFloat(2982, this._camModl);
        this._camModl.position(0).limit(16);
    }
}
