package tfc.better_with_shaders.preprocessor.config;

import java.util.ArrayList;
import java.util.HashMap;

public class CFGCategory {
    ArrayList<String> left = new ArrayList<>();
    ArrayList<String> right = new ArrayList<>();

    ConfigLoader loader;
    HashMap<String, String> displayToInternal = new HashMap<>();

    public CFGCategory(ConfigLoader loader) {
        this.loader = loader;
    }

    public void set(String display, Object value) {
        loader.values.put(displayToInternal.get(display), value);
    }

    public Object get(String display) {
        return loader.values.get(displayToInternal.get(display));
    }

    public ArrayList<String> getLeft() {
        return left;
    }

    public ArrayList<String> getRight() {
        return right;
    }
}
