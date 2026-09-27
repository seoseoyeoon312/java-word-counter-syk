package kr.sesac.wordcounter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
// txt 읽기 ~!!
public class TxtParser implements TextParser {
    @Override
    public List<String> parse(Path file) throws IOException {
        List<String> texts = new ArrayList<>();
        try (BufferedReader reader =
                     Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) { //한줄 씩 읽기
                texts.add(line);
            }
        }
        return texts;
    }
}