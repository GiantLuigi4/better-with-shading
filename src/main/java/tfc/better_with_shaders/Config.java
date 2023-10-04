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
		cfg = new ConfigHandler("bws", props);
	}
	
	// slow: refer to ShaderManager
	@ApiStatus.Internal
	public static String getShader() {
		return cfg.getString("shader");
	}
	
	static void setProp(String name, String value) {
		props.put(name, value);
	}
	
	static void writeConfig() {
		cfg.writeDefaultConfig();
	}
}
