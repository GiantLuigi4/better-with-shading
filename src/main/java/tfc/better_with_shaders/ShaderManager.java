package tfc.better_with_shaders;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.shader.Shader;
import net.minecraft.client.render.texturepack.TexturePackBase;
import net.minecraft.client.render.texturepack.TexturePackList;
import turniplabs.halplibe.util.ConfigHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class ShaderManager {
	private static final String shaderPackDir = FabricLoader.getInstance().getGameDir() + "/shader_packs/";
	private final Minecraft mc = Minecraft.getMinecraft(Minecraft.class);
	public static final ShaderManager INSTANCE = new ShaderManager();
	
	
	// shaders
	protected Shader DEFAULT;
	protected Shader ENTITY;
	
	public Shader getDefaultShader() {
		return DEFAULT;
	}
	
	public Shader getEntitytShader() {
		return ENTITY;
	}
	
	// config data
	String activePack = null;
	
	HashMap<String, String> fallbacks = new HashMap<>();
	
	public ShaderManager() {
		if (!new File(shaderPackDir).exists())
			new File(shaderPackDir).mkdirs();
		
		fallbacks.put("entity", "base");
	}
	
	public void useShader(String name) {
		if (activePack != null) {
			activePack = name;
			Config.setProp("shader", name);
			Config.writeConfig();
		} else activePack = name;
		init(mc.texturePackList);
	}
	
	protected InputStream getStream(String fl) {
		InputStream is = null;
		
		if (!activePack.equals("internal")) {
			File flf = new File(shaderPackDir + activePack + "/core/" + fl);
			
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
		try {
			String ext = ".fsh";
			if (string.endsWith(".vsh")) ext = ".vsh";
			
			InputStream is = getStream(name + ext);
			
			if (is == null) {
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
			throw new RuntimeException(err);
		}
	}
	
	public void init(TexturePackList list) {
		if (DEFAULT != null) {
			DEFAULT.delete();
			ENTITY.delete();
		} else {
			DEFAULT = new Shader();
			ENTITY = new Shader();
		}
		
		base = list.selectedTexturePack;
		def = list.getDefaultTexturePack();
		
		if (activePack.equals("none")) {
			DEFAULT = null;
			ENTITY = null;
			return;
		}
		
		DEFAULT.compile(string -> this.read(string, "base"), "bsege");
		ENTITY.compile(string -> this.read(string, "entity"), "bsege");
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
}
