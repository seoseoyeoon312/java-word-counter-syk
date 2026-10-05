package kr.sesac.wordcounter.parser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TsvParser implements TextParser {
    static final String[] COLUMNS = {"document"};

    @Override
    public List<String> parse(Path file) throws IOException {
        List<String> texts = new ArrayList<>();
        var format = CSVFormat.RFC4180.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setDelimiter('\t') //차이
                .setQuote(null) //차이
                .setTrim(true)
                .get();

        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {
            CsvParser.checkHeader(parser, COLUMNS);
            for (CSVRecord record : parser) {
                if (!record.isConsistent()) {
                    throw new IOException("셀 수가 헤더와 다릅니다: " + record.getRecordNumber() + "번째 레코드");
                }
                for (String column : COLUMNS) {
                    texts.add(record.get(column));
                }
            }
        }

        return texts;
    }
}
