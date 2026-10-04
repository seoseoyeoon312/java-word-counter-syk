package kr.sesac.wordcounter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CsvParser implements TextParser {
    static final String[] COLUMNS = {"text"};   //챗봇 데이터 분석할 땐 q,a로 변경하기

    @Override
    public List<String> parse(Path file) throws IOException {
        List<String> texts = new ArrayList<>();
        var format = CSVFormat.RFC4180.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .get();
        //메서드 체이닝.. get에서 최종 완성

        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) //상단 읽기 규칙 format 적용
         {
            checkHeader(parser, COLUMNS);

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

    //헤더 검사 도우미
    static void checkHeader(CSVParser parser, String[] columns) throws IOException {
        List<String> headers = parser.getHeaderNames();
        if (headers.isEmpty()) {
            throw new IOException("헤더를 읽을 수 없습니다.");
        }
        for (String column : columns) {
            if (!headers.contains(column)) {
                throw new IOException("분석 열이 없습니다: " + column);
            }
        }
    }
}
