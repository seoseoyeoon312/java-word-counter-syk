package kr.sesac.wordcounter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ResultWriter {
    // 정렬된 결과를 TSV(word, count)로 저장! 실패하면 IOException을 부른 쪽으로
    static void save(List<Map.Entry<String, Long>> entries, Path out) throws IOException {
        Files.createDirectories(out.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            writer.write("word\tcount");
            writer.newLine();
            for (Map.Entry<String, Long> e : entries) {
                writer.write(e.getKey() + "\t" + e.getValue());
                writer.newLine();
            }
        }
    }
}