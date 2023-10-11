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
        if (loader.categories.containsKey(displayToInternal.get(display))) {
            return loader.categories.get(displayToInternal.get(display));
        }
        return loader.values.get(displayToInternal.get(display));
    }

    public ArrayList<String> getLeft() {
        return left;
    }

    public ArrayList<String> getRight() {
        return right;
    }

    public float minValue(String display) {
        Object o = loader.mins.get(displayToInternal.get(display));
        if (o == null) System.out.println("Missing minimum value for " + display);
        if (o.getClass().equals(Float.class)) return (Float) o;
        else if (o.getClass().equals(Integer.class)) return (Integer) o;
        else throw new RuntimeException("Invalid minimum value type: " + o.getClass());
    }

    public float maxValue(String display) {
        Object o = loader.maxes.get(displayToInternal.get(display));
        if (o == null) System.out.println("Missing maximum value for " + display);
        if (o.getClass().equals(Float.class)) return (Float) o;
        else if (o.getClass().equals(Integer.class)) return (Integer) o;
        else throw new RuntimeException("Invalid maximum value type: " + o.getClass());
    }
}
