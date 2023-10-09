import tfc.better_with_shaders.toml.Toml;

public class TomlTest {
    public static void main(String[] args) {
        Toml toml1 = new Toml();

        toml1.addEntry("test", "hello!");
        toml1.addEntry("commented_entry", "this is an entry with a comment\nit has no meaning", "hello!");

        toml1.addCategory("test category", "category");
        toml1.addEntry("category.entry_in_category", "this is an entry in a category", 42);

        System.out.println(toml1);
    }
}
