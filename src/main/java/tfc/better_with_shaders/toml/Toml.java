package tfc.better_with_shaders.toml;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class Toml {
    protected HashMap<String, Toml> categories = new HashMap<>();
    protected HashMap<String, Entry<?>> entries = new HashMap<>();
    protected Optional<String> comment = Optional.empty();

    ArrayList<String> orderedKeys = new ArrayList<>();
    ImmutableList<String> immutKeys = null;

    public Object get(String key) {
        return get(key, Object.class);
    }

    @SuppressWarnings({"unchecked", "unused"})
    public <T> T get(String key, Class<T> clazz) {
        if (key.startsWith(".")) {
            if (key.substring(1).contains(".")) {
                String[] split = key.substring(1).split("\\.", 2);
                return (T) categories.get(split[0]).get("." + split[1]);
            }
            return (T) categories.get(key.substring(1));
        } else {
            Object value = entries.get(key).getT();

            if (clazz.equals(Float.class)) return (T) (Float) ((Number) value).floatValue();
            if (clazz.equals(Double.class)) return (T) (Double) ((Number) value).doubleValue();
            if (clazz.equals(Long.class)) return (T) (Long) (((Number) value).longValue());
            if (clazz.equals(Integer.class)) return (T) (Integer) (((Number) value).intValue());
            if (clazz.equals(Short.class)) return (T) (Short) (((Number) value).shortValue());
            if (clazz.equals(Byte.class)) return (T) (Byte) (((Number) value).byteValue());

            if (clazz.equals(float.class)) return (T) (Float) ((Number) value).floatValue();
            if (clazz.equals(double.class)) return (T) (Double) ((Number) value).doubleValue();
            if (clazz.equals(long.class)) return (T) (Long) (((Number) value).longValue());
            if (clazz.equals(int.class)) return (T) (Integer) (((Number) value).intValue());
            if (clazz.equals(short.class)) return (T) (Short) (((Number) value).shortValue());
            if (clazz.equals(byte.class)) return (T) (Byte) (((Number) value).byteValue());

            return (T) value;
        }
    }

    public Toml() {
    }

    public Toml(String comment) {
        this.comment = Optional.of(comment);
    }

    public void addCategory(String comment, String name) {
        addCategory(name, new Toml(comment));
    }

    public void addCategory(String name) {
        addCategory(name, new Toml());
    }

    protected void addCategory(String name, Toml category) {
        if (name.contains(".")) {
            String[] split = name.split("\\.", 2);
            Toml toml = categories.get(split[0]);
            if (toml == null) {
                categories.put(split[0], toml = new Toml());
                orderedKeys.add("." + split[0]);
            }
            toml.addCategory(split[1], category);
        } else {
            orderedKeys.add("." + name);
            categories.put(name, category);
        }
    }

    public <T> void addEntry(String name, T value) {
        addEntry(name, new Entry<>(value));
    }

    public <T> void addEntry(String name, String comment, T value) {
        addEntry(name, new CommentedEntry<>(comment, value));
    }

    public void addEntry(String name, Entry<?> value) {
        immutKeys = null;

        if (name.contains(".")) {
            String[] split = name.split("\\.", 2);
            Toml toml = categories.get(split[0]);
            if (toml == null) {
                categories.put(split[0], toml = new Toml());
                orderedKeys.add("." + split[0]);
            }
            toml.addEntry(split[1], value);
        } else orderedKeys.add(name);

        entries.put(name, value);
    }

    public ImmutableList<String> getOrderedKeys() {
        if (immutKeys == null)
            immutKeys = ImmutableList.copyOf(orderedKeys);
        return immutKeys;
    }

    protected String repeat(String txt, int count) {
        StringBuilder dst = new StringBuilder();
        for (int i = 0; i < count; i++) dst.append(txt);
        return dst.toString();
    }

    public String toString(String rootKey, int indents) {
        StringBuilder builder = new StringBuilder();

        if (rootKey.isEmpty()) {
            if (this.comment.isPresent()) {
                String comment = this.comment.get();

                for (String re : comment.split("\n"))
                    builder.append(repeat("\t", indents)).append("# ").append(re).append("\n");
            }

            builder.append("\n");
        }

        for (String orderedKey : getOrderedKeys()) {
            String[] res;
            int offset = 0;
            int sep = 0;

            if (orderedKey.startsWith(".")) {
                if (orderedKey.substring(1).contains(".")) continue;

                Toml cat = categories.get(orderedKey.substring(1));
                String full = rootKey + (rootKey.isEmpty() ? "" : ".") + orderedKey.substring(1);

                if (cat.comment.isPresent()) {
                    String comment = cat.comment.get();

                    for (String re : comment.split("\n"))
                        builder.append(repeat("\t", indents)).append("# ").append(re).append("\n");
                }

                builder.append(repeat("\t", indents)).append("[").append(full).append("]").append("\n");

                res = cat.toString(full, 0).split("\n");
                sep = offset = 1;
            } else {
                res = entries.get(orderedKey).toString(orderedKey).split("\n");
            }

            for (String re : res) builder.append(repeat("\t", indents + offset)).append(re).append("\n");
            builder.append(repeat("\n", sep));
        }

        return builder.toString();
    }

    @Override
    public String toString() {
        return toString("", 0);
    }

    public boolean contains(String category) {
        if (category.startsWith("."))
            return categories.containsKey(category.substring(1));
        else
            return entries.containsKey(category);
    }
}
