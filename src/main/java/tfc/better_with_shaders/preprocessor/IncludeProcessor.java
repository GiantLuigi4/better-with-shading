package tfc.better_with_shaders.preprocessor;

import java.util.function.BiFunction;

public class IncludeProcessor extends Processor {
    BiFunction<String, String, String> reader;

    public IncludeProcessor(BiFunction<String, String, String> reader) {
        this.reader = reader;
    }

    @Override
    public String modify(String src) {
        StringBuilder builder = new StringBuilder();
        for (String s : src.split("\n")) {
            if (s.startsWith("#include")) {
                String text = s.substring("#include ".length());
                text = text.trim();
                text = text.substring(1, text.length() - 1);
                String frmt = text.substring(text.lastIndexOf("."));
                text = text.substring(0, text.length() - frmt.length());
                String res = reader.apply(frmt, "include/" + text);
                builder.append(res.trim()).append("\n");
                continue;
            }
            builder.append(s).append("\n");
        }
        return builder.toString();
    }
}
