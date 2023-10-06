package tfc.better_with_shaders;

import org.jetbrains.annotations.ApiStatus;
import turniplabs.halplibe.util.ConfigHandler;

import java.io.File;
import java.util.Properties;

public class Config {
	// config helpers
	private static final Properties props;
	private static final ConfigHandler cfg;
	
	static {
		props = new Properties();
		props.put("shader", "none");
		props.put("shadow_res", "4800");
		props.put("close_highp", "true");
		props.put("far_highp", "false");
		cfg = new ConfigHandler("bws", props);
	}
	
	public static boolean checkPrecision(boolean close) {
		return cfg.getBoolean(close ? "close_highp" : "far_highp");
	}
	
	// slow: refer to ShaderManager
	@ApiStatus.Internal
	public static String getShader() {
		return cfg.getString("shader");
	}
	
	public static int getShadowRes() {
		return cfg.getInt("shadow_res");
	}
	
	static void setProp(String name, String value) {
		props.put(name, value);
	}
	
	static void writeConfig() {
		cfg.writeDefaultConfig();
	}
}
