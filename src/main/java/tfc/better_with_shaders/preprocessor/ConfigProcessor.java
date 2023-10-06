package tfc.better_with_shaders.preprocessor;

import java.util.HashMap;

public class ConfigProcessor extends Processor {
    HashMap<String, Object> values = new HashMap<>();

    public ConfigProcessor() {
        values.put("SOFT_PENUMUBRA", true);
        values.put("SHADOW_BRIGHTNESSS", 0.65);
        values.put("PENUMBRA_STEPS", 16);
        values.put("PENUMBRA_ROUNDNESS", 128);
    }

    @Override
    public String modify(String src) {
        StringBuilder builder = new StringBuilder();
        boolean inConfig = false;
        values.put("PENUMBRA_STEPS", 16);
        values.put("PENUMBRA_ROUNDNESS", 8);
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
                    if (values.containsKey(name)) {
                        if (values.get(name) instanceof Boolean) {
                            if ((Boolean) values.get(name))
                                builder.append("#define ").append(name).append("\n");
                            else
                                builder.append("\n");
                        } else
                            builder.append("#define ").append(name).append(" ").append(values.get(name).toString()).append("\n");
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
