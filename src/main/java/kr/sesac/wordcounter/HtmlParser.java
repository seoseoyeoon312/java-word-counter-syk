package kr.sesac.wordcounter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class HtmlParser implements TextParser {
    static final String SELECTOR = "#content";   //본문 영역 (설정)

    @Override
    public List<String> parse(Path file) throws IOException {
        List<String> texts = new ArrayList<>();
        String html = Files.readString(file, StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(html);
        Elements matches = doc.select(SELECTOR);
        if (matches.size() != 1) {
            throw new IOException("본문 요소는 정확히 하나여야 합니다.");
        }
        Element content = matches.first();
        content.select("script, style, nav, header, footer").remove();
        texts.add(content.text());
        return texts;
    }
}