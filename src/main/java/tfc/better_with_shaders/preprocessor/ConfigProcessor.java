package tfc.better_with_shaders.preprocessor;

import tfc.better_with_shaders.preprocessor.config.ConfigLoader;

import java.util.HashMap;

public class ConfigProcessor extends Processor {
    ConfigLoader cfg;

    public ConfigProcessor(ConfigLoader cfg) {
        this.cfg = cfg;
    }

    @Override
    public String modify(String src) {
        StringBuilder builder = new StringBuilder();
        boolean inConfig = false;
        for (String s : src.split("\n")) {
            if (s.startsWith("#endconfig")) {
                inConfig = false;
                builder.append("\n");
                continue;
            }

            if (inConfig) {
                s = s.trim();

                if (s.startsWith("#define")) {
                    String name = s.split(" ")[1].trim();
                    if (cfg.values.containsKey(name)) {
                        if (cfg.values.get(name) instanceof Boolean) {
                            if ((Boolean) cfg.values.get(name))
                                builder.append("#define ").append(name).append("\n");
                            else
                                builder.append("\n");
                        } else
                            builder.append("#define ").append(name).append(" ").append(cfg.values.get(name).toString()).append("\n");
                        continue;
                    }
                }

                builder.append(s).append("\n");
                continue;
            }

            if (s.startsWith("#config")) {
                inConfig = true;
                builder.append("\n");
                continue;
            }

            builder.append(s).append("\n");
        }
        return builder.toString();
    }
}
