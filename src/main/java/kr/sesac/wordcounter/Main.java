package kr.sesac.wordcounter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.io.BufferedWriter;
import java.io.UncheckedIOException;

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
    static boolean hasSummary = false;
    static String lastPath = "";
    static long lastElapsedMs = 0;
    static int tried = 0;      // 시도
    static int succeeded = 0;  // 성공
    static int failed = 0;     // 실패
    static int skipped = 0;    // 지원하지 않아 건너뜀

    static final String[] CSV_COLUMNS = {"text"}; // 표라서 배열 []
    static final String[] TSV_COLUMNS = {"document"}; // 표라서 배열 []
    static final String HTML_SELECTOR = "#content"; // # - id란 뜻

    static boolean isSupported(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        return name.endsWith(".txt") || name.endsWith(".csv") || name.endsWith(".tsv") || name.endsWith(".html") || name.endsWith(".htm");
    }

    public static void main(String[] args) throws IOException { //메뉴구성
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                System.out.print("선택 > ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1" -> {
                        while (true) {
                            System.out.print("파일 또는 폴더 경로 > ");
                            String path = scanner.nextLine().trim();
                            if (analyze(Path.of(path))) break;   // 분석이 시작됐으면 반복 탈출
                        }
                        System.out.println(hasResult ? "분석 완료" : "분석 실패: 모든 파일을 처리하지 못했습니다.");
                        showSummary();
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
                        showSummary();
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

    static boolean analyze(Path input) throws IOException { //파일 읽기
        if (!Files.exists((input))) {
            System.out.println("경로를 찾을 수 없습니다.: " + input);
            return false;
        }

        // ★(2) 대상 파일 목록 만들기
        List<Path> targets = new ArrayList<>(); //처리할 파일 담을 빈 목록
        int skip = 0; //이해안감. 지원 안하는 파일 개수를 임시로 세는 변수
        if (Files.isDirectory(input)) {
            try (var stream = Files.list(input)) {
                for (Path entry : stream.sorted().toList()) { //이름 순 정렬해서 하나씩.
                    if (!Files.isRegularFile(entry)) continue; //하위 폴더면 건너 뜀
                    if (isSupported(entry)) targets.add(entry); //지원하는 확장자면 목록에 추가
                    else skip++; //아니면 건너뜀
                }
            }
        } else { //파일 하나를 직접 입력한경우
            if (!isSupported(input)) {
                System.out.println("지원하지 않는 형식입니다 (.txt .csv .tsv .html .htm): " + input);
                return false;
            }
            targets.add(input); //지원하는 유형 파일이면 목록에 하나 넣음
        }

        // ★(3) 지원 파일이 없으면 그만
        if (targets.isEmpty()) { //목록 비었으면 중단
            System.out.println("분석할 지원 파일이 없습니다: " + input);
            return false;
        }

        // (4) 이제 이전 결과 지우고 시계 켜기
        freq.clear(); total = 0; hasResult = false; //이전 결과 지우기

        tried = 0;
        succeeded = 0;
        failed = 0;
        skipped = skip;   // ★
        lastPath = input.toString(); //요약용 경로 기억
        long start = System.nanoTime(); //스톱워치 시작

        // ★(5) 파일마다 처리
        for (Path file : targets) {
            tried++;
            try {
                mergeResult(readOneFile(file));
                succeeded++;
            } catch (IOException | UncheckedIOException e) {
                failed++;
                System.out.println("실패: " + file + " (" + e.getMessage() + ")");
            }
        }

        // (6) 시계 멈추기
        lastElapsedMs = (System.nanoTime() - start) / 1000000;
        hasResult = succeeded > 0;   // 성공한 파일이 있을 때만 조회, 저장 가능
        hasSummary = true;
        return true;
    }

    static Map<String, Long> readOneFile(Path input) throws IOException {
        Map<String, Long> local = new HashMap<>();
        String name = input.getFileName().toString().toLowerCase();
        if (name.endsWith(".txt")) {
            try (BufferedReader reader =
                         Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) { //한줄 씩 읽기
//                System.out.println(line);
                    countText(line, local);
                }
            }
        } else if (name.endsWith(".csv")) {
            var format = CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .get();

            try (var reader = Files.newBufferedReader(input, StandardCharsets.UTF_8);
                 CSVParser parser = format.parse(reader)) {
                checkHeader(parser, CSV_COLUMNS);
                for (CSVRecord record : parser) {
                    if (!record.isConsistent()) {
                        throw new IOException("셀 수가 헤더와 다릅니다: " + record.getRecordNumber() + "번째 레코드");
                    }
                    for (String column : CSV_COLUMNS) {
                        countText(record.get(column), local);
                    }
                }
            }

        } else if (name.endsWith(".tsv")) {
            var format = CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setDelimiter('\t') //추가
                    .setQuote(null) //추가
                    .setTrim(true)
                    .get();

            try (var reader = Files.newBufferedReader(input, StandardCharsets.UTF_8);
                 CSVParser parser = format.parse(reader)) {
                checkHeader(parser, TSV_COLUMNS);
                for (CSVRecord record : parser) {
                    if (!record.isConsistent()) {
                        throw new IOException("셀 수가 헤더와 다릅니다: " + record.getRecordNumber() + "번째 레코드");
                    }
                    for (String column : TSV_COLUMNS) {
                        countText(record.get(column), local);
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
            countText(content.text(), local);
        }
        return local;
    }

    static void mergeResult(Map<String, Long> local) {
        for (Map.Entry<String, Long> e : local.entrySet()) {
            freq.merge(e.getKey(), e.getValue(), Long::sum);
            total += e.getValue();
        }
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


    //2
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


    //3
    static void searchWord(Scanner scanner) {
        if (!hasResult) { //조회·저장 가능 여부를 hasResult 관리. freq가 비었는지로 판단하면 "빈 파일을 정상 처리한 경우(0개·0종)"와 "분석 전·전부 실패"를 구분할 수 없기 때문
            System.out.println("파일 분석을 먼저 해주세요.");
            return;
        }
        while (true) {
            System.out.print("찾을 단어 > ");
            String word = scanner.nextLine().trim();
            List<String> tokens = toTokens(word);
            if (tokens.size() != 1) {
                System.out.println("단어 하나를 입력하세요.");
                continue;
            }
            String getToken = tokens.get(0);
            long count = freq.getOrDefault(getToken, 0L);
            System.out.println(getToken + " : " + count + "회");
            break;
        }
    }

    //4
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

    //5 //1
    static void showSummary() {
        if (!hasSummary) {
            System.out.println("파일 분석을 먼저 해주세요");
            return;
        }
        System.out.println("입력: " + lastPath);
        System.out.println("파일: 시도 " + tried + "개 / 성공 " + succeeded + "개 / 실패 " + failed + "개 / 지원하지 않아 건너뜀 " + skipped + "개");
        System.out.println("전체 단어 : " + total + "개 / 서로 다른 단어: " + freq.size() + "개");
        System.out.println("처리 시간: " + lastElapsedMs + "ms");

        if (!hasResult) {
            System.out.println("모든 파일이 실패해 조회·저장할 결과가 없습니다.");
        }
    }

    static void countText(String text, Map<String, Long> target) { //단어 세기 담당
        String[] tokens = text.toLowerCase().split("[^A-Za-z0-9가-힣ㄱ-ㅎㅏ-ㅣ]+"); //추가
        for (String token : tokens) { //추가
            if (token.isEmpty()) continue; //빈 문자열이면 건너뛰기
            if (isNumberOnly(token)) continue; //숫자만이면 건너뛰기
            target.merge(token, 1L, Long::sum); //표에 + 1
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

