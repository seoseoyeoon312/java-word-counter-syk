package kr.sesac.wordcounter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface TextParser {
    List<String> parse(Path file) throws IOException;
}