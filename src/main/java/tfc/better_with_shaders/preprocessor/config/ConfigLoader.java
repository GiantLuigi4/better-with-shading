package tfc.better_with_shaders.preprocessor.config;

import tfc.better_with_shaders.toml.Toml;
import tfc.better_with_shaders.toml.TomlParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public class ConfigLoader {
    public final HashMap<String, Object> values = new HashMap<>();

    public void dump() {
        values.clear();
        categories.clear();
    }

    HashMap<String, CFGCategory> categories = new HashMap<>();

    public CFGCategory getCategory(String name) {
        return categories.get(name);
    }

    public void load(String subCat, Function<String, String> reader) {
        CFGCategory cfgCategory = new CFGCategory(this);
        String root = reader.apply(subCat);
        if (root == null) return;

        String active = null;
        try {
            Toml rootToml = TomlParser.parse(root);
            for (String orderedKey : rootToml.getOrderedKeys()) {
                if (orderedKey.equals(".left") || orderedKey.equals(".right")) {
                    active = orderedKey;

                    ArrayList<String> list;
                    if (orderedKey.equals(".left")) list = cfgCategory.left;
                    else list = cfgCategory.right;

                    Toml category = rootToml.get(orderedKey, Toml.class);

                    for (String key : category.getOrderedKeys()) {
                        if (key.startsWith(".") && !key.substring(1).contains(".")) {
                            active = orderedKey + key;

                            Toml entry = category.get(key, Toml.class);

                            String display = entry.get("name", String.class);
                            if (entry.contains("category")) {
                                String file = entry.get("category", String.class);
                                load(file, reader);
                                cfgCategory.displayToInternal.put(display, null);
                            } else if (entry.contains("boolean")) {
                                String sdrName = entry.get("boolean", String.class);
                                values.put(sdrName, entry.get("default", Boolean.class));
                                cfgCategory.displayToInternal.put(display, sdrName);
                            } else if (entry.contains("float")) {
                                String sdrName = entry.get("float", String.class);
                                values.put(sdrName, entry.get("default", Float.class));
                                cfgCategory.displayToInternal.put(display, sdrName);
                            } else if (entry.contains("integer")) {
                                String sdrName = entry.get("integer", String.class);
                                values.put(sdrName, entry.get("default", Integer.class));
                                cfgCategory.displayToInternal.put(display, sdrName);
                            }
                            list.add(display);
                        }
                    }
                }
            }
        } catch (Throwable err) {
            System.err.println("Failed to parse " + subCat + " " + active);
            throw new RuntimeException(err);
        }

        categories.put(subCat, cfgCategory);
    }

    public void load(Function<String, String> reader) {
        load("options.toml", reader);
    }
}
