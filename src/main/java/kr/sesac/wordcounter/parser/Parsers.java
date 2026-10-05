package kr.sesac.wordcounter.parser;

import java.nio.file.Path;
import java.util.Map;
import java.util.TreeSet;

public class Parsers {
    static final Map<String, TextParser> BY_EXTENSION = Map.of(
            "txt", new TxtParser(),
            "csv", new CsvParser(),
            "tsv", new TsvParser(),
            "html", new HtmlParser(),
            "htm", new HtmlParser(), //피드백
            "json", new JsonParser() //심화
    );

    public static TextParser find(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return null;
        }
        String extension = name.substring(dot + 1);
        return BY_EXTENSION.get(extension);
    }

    //안내 문구용 목록: ".csv .htm .html .tsv .txt"
    public static String supportedList() {
        StringBuilder sb = new StringBuilder();
        for (String extension : new TreeSet<>(BY_EXTENSION.keySet())) {
            sb.append(".").append(extension).append(" ");
        }
        return sb.toString().trim();
    }
}
