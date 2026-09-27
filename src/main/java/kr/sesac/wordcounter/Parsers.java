package kr.sesac.wordcounter;

import java.nio.file.Path;
import java.util.Map;
import java.util.TreeSet;

public class Parsers {
    // 확장자 → 파서 등록표. 새 형식은 여기 한 줄만 추가
    static final Map<String, TextParser> BY_EXTENSION = Map.of(
            "txt", new TxtParser(),
            "csv", new CsvParser(),
            "tsv", new TsvParser(),
            "html", new HtmlParser(),
            "htm", new HtmlParser(),
            "json", new JsonParser()
    );

    // 파일에 맞는 파서를 찾아 줌. 지원하지 않으면 null
    static TextParser find(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return null;
        }
        String extension = name.substring(dot + 1);
        return BY_EXTENSION.get(extension);
    }

    // 안내 문구용 목록: ".csv .htm .html .tsv .txt"
    static String supportedList() {
        StringBuilder sb = new StringBuilder();
        for (String extension : new TreeSet<>(BY_EXTENSION.keySet())) {
            sb.append(".").append(extension).append(" ");
        }
        return sb.toString().trim();
    }
}