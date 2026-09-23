package kr.sesac.wordcounter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.BufferedWriter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Main {
    static HashMap<String, Long> freq = new HashMap<>(); //추가
    static long total = 0; //전체 단어 수 셀 변수 추가

    static boolean hasResult = false;

    static final String[] CSV_COLUMNS = {"text"}; // 표라서 배열 []
    static final String[] TSV_COLUMNS = {"document"}; // 표라서 배열 []
    static final String HTML_SELECTOR = "#content"; // # - id란 뜻

    public static void main(String[] args) throws IOException { //메뉴구성
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                System.out.print("선택 > ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1" -> {
                        System.out.print("파일 또는 폴더 경로 > ");
                        String path = scanner.nextLine();
                        analyze(Path.of(path));
                        System.out.println("분석완료");
                        System.out.println("전체 단어: " + total + "개" + " / " + "서로 다른 단어: " + freq.size() + "개");
                    }
                    case "2" -> {
                        showTop(scanner);
                    }

                    case "3" -> {
                        searchWord(scanner);
                    }

                    case "4" -> {
                        saveResult();
                    }

                    case "5" -> {

                    }

                    case "0" -> {
                        System.out.println("프로그램을 종료합니다.");
                        running = false;
                    }
                    default -> System.out.println("잘못된 번호입니다.");
                }
            }
        }
    }

    static void analyze(Path input) throws IOException { //파일 읽기
        freq.clear();//새로 분석하기 전에 표 비우기
        total = 0;
        hasResult = false;
        String name = input.getFileName().toString().toLowerCase();

        if (name.endsWith(".txt")) {
            try (BufferedReader reader =
                         Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) { //한줄 씩 읽기
//                System.out.println(line);
                    countText(line);
                }
            }
        } else if (name.endsWith(".csv")) {
            var format = CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .get();

            try (var reader = Files.newBufferedReader(input, StandardCharsets.UTF_8);
                 CSVParser parser = format.parse(reader)) {
                for (CSVRecord record : parser) {
                    for (String column : CSV_COLUMNS) {
                        countText(record.get(column));
                    }
                }
            }

        } else if (name.endsWith(".tsv")) {
            var format = CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setDelimiter('\t') //추가
                    .setQuote(null) //추가
                    .get();

            try (var reader = Files.newBufferedReader(input, StandardCharsets.UTF_8);
                 CSVParser parser = format.parse(reader)) {
                for (CSVRecord record : parser) {
                    for (String column : TSV_COLUMNS) {
                        countText(record.get(column));
                    }
                }
            }
        } else if (name.endsWith(".html") || name.endsWith(".htm")) {
            Document document = Jsoup.parse(
                    input.toFile(), "UTF-8");
            Elements matches = document.select(HTML_SELECTOR);
            if (matches.size() != 1) {
                throw new IOException("본문 요소는 정확히 하나여야 합니다.");
            }
            Element content = matches.first();
            content.select("script, style, nav, header, footer").remove();
            countText(content.text());
        }

        hasResult = true;
    }

    private static void printMenu() { //메뉴 출력
        System.out.println("문서 단어 분석기");
        System.out.println("1. 새 분석 시작");
        System.out.println("2. 상위 N개 단어 보기");
        System.out.println("3. 특정 단어 횟수 찾기");
        System.out.println("4. 전체 결과 저장");
        System.out.println("5. 최근 분석 요약 보기");
        System.out.println("0. 종료");
    }

    static List<Map.Entry<String, Long>> sortedEntries() { //정렬된 목록 만들기 메서드
        List<Map.Entry<String, Long>> list = new ArrayList<>(freq.entrySet());
        list.sort((a, b) -> {
            int byCount = Long.compare(b.getValue(), a.getValue());
            if (byCount != 0) {
                return byCount;
            }
            return a.getKey().compareTo(b.getKey());
        });
        return list;
    } // freq를 목록으로 복사한 뒤, 횟수 내림차순으로 정렬하고 횟수가 같으면 단어를 compareTo 오름차순으로 정렬해 돌려준다.
    // 조회(5번)와 저장(7번)이 같은 순서를 써야 해서 메서드로 분리했다.

    static void showTop(Scanner scanner) {
        if (!hasResult) { //조회·저장 가능 여부를 hasResult 관리. freq가 비었는지로 판단하면 "빈 파일을 정상 처리한 경우(0개·0종)"와 "분석 전·전부 실패"를 구분할 수 없기 때문
            System.out.println("파일 분석을 먼저 해주세요.");
            return;
        }
        int n;
        while (true) {
            System.out.print("몇 개를 볼까요? (기본 10) > ");
            String in = scanner.nextLine().trim();
            if (in.isEmpty()) {
                n = 10;
                break;
            }
            try {
                n = Integer.parseInt(in);
                if (n < 1) {
                    System.out.println("1 이상의 정수를 입력하세요.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("1 이상의 정수를 입력하세요.");
            }
        }
        List<Map.Entry<String, Long>> list = sortedEntries();
        for (int i = 0; i < Math.min(n, list.size()); i++) {
            System.out.println((i + 1) + ". " + list.get(i).getKey() + " : " + list.get(i).getValue() + "회");
        }
    }

    static List<String> toTokens(String text) {
        List<String> result = new ArrayList<>();
        String[] tokens = text.toLowerCase().split("[^A-Za-z0-9가-힣ㄱ-ㅎㅏ-ㅣ]+");
        for (String token : tokens) {
            if (token.isEmpty()) continue;
            if (isNumberOnly(token)) continue;
            result.add(token);
        }
        return result;
    }

    static void searchWord(Scanner scanner) {
        if (!hasResult) { //조회·저장 가능 여부를 hasResult 관리. freq가 비었는지로 판단하면 "빈 파일을 정상 처리한 경우(0개·0종)"와 "분석 전·전부 실패"를 구분할 수 없기 때문
            System.out.println("파일 분석을 먼저 해주세요.");
            return;
        }
        while (true) {
            System.out.print("찾을 단어 > ");
            String word = scanner.nextLine().trim();
            List<String> tokens = toTokens(word);
            if(tokens.size() != 1) {
                System.out.println("단어 하나를 입력하세요.");
                continue;
            }
            String getToken = tokens.get(0);
            long count = freq.getOrDefault(getToken, 0L);
            System.out.println(getToken + " : " + count + "회");
            break;
        }
    }

    static void saveResult() {
        if (!hasResult) {
            System.out.println("파일 분석을 먼저 해주세요.");
            return;
        }

        Path out = Path.of("out", "counts.tsv");
        List<Map.Entry<String, Long>> list = sortedEntries();

        try {
            Files.createDirectories(out.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
                writer.write("word\tcount");
                writer.newLine();
                for (Map.Entry<String, Long> e : list) {
                    writer.write(e.getKey() + "\t" + e.getValue());
                    writer.newLine();
                }
            }
            System.out.println("전체 결과 " + list.size() + "개 단어를 " + out + "에 저장했습니다.");
        } catch (IOException e) {
            System.out.println("저장에 실패했습니다: " + e.getMessage());
        }
    }

    static void countText(String text) { //단어 세기 담당
        String[] tokens = text.toLowerCase().split("[^A-Za-z0-9가-힣ㄱ-ㅎㅏ-ㅣ]+"); //추가
        for (String token : tokens) { //추가
            if (token.isEmpty()) continue; //빈 문자열이면 건너뛰기
            if (isNumberOnly(token)) continue; //숫자만이면 건너뛰기
            freq.merge(token, 1L, Long::sum); //표에 + 1
            total = total + 1; // 전체 수 +1
        }
    }
    static boolean isNumberOnly(String word) { //숫자만 있는지 판별 담당, 공통 사용
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (!(c >= '0' && c <= '9')) {
                return false;
            }
        }
        return true;
    }


}

