package kr.sesac.wordcounter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {
    static HashMap<String, Long> freq = new HashMap<>();
    static long total = 0; //전체 단어 수 셀 변수 추가
    static boolean hasResult, hasSummary = false;
    static String lastPath = "";
    static long lastElapsedMs = 0;
    static int tried, succeeded, failed, skipped = 0;

    static boolean isSupported(Path file) {
        return Parsers.find(file) != null;
    }

    public static void main(String[] args) throws IOException { //메뉴구성
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                System.out.print("선택 > ");
                String choice = scanner.nextLine(); //nextLine -> 엔터로 실행 가능

                switch (choice) {
                    case "0" -> {
                        System.out.println("프로그램을 종료합니다.");
                        running = false;
                    }
                    case "1" -> {
                        while (true) {
                            System.out.print("파일 또는 폴더 경로 > ");
                            String path = scanner.nextLine().trim();
                            try {
                                if (analyze(Path.of(path))) break;
                            } catch (java.nio.file.InvalidPathException e) {
                                System.out.println("잘못된 경로입니다: " + path);
                            }
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
                    default -> System.out.println("잘못된 번호입니다.");
                }
            }
        }
    }

    static boolean analyze(Path input) throws IOException {
        if (!Files.exists(input)) {
            System.out.println("경로를 찾을 수 없습니다.: " + input);
            return false;
        }

        List<Path> targets = new ArrayList<>();
        int skip = 0; //지원하지 않는 파일 개수
        if (Files.isDirectory(input)) {
            try (var stream = Files.list(input)) {
                for (Path entry : stream.sorted().toList()) {
                    if (!Files.isRegularFile(entry)) continue; //하위 폴더는 건너뜀
                    if (isSupported(entry)) targets.add(entry);
                    else skip++;
                }
            } catch (IOException | UncheckedIOException e) { //폴더 목록을 못 읽으면 안내 후 재입력
                System.out.println("폴더를 읽을 수 없습니다: " + input + " (" + e.getClass().getSimpleName() + ")");
                return false;
            } //강사님 피드백 추가
        } else { //파일 하나를 직접 입력한경우
            if (!isSupported(input)) {
                System.out.println("지원하지 않는 형식입니다 (" + Parsers.supportedList() + "): " + input);
                return false;
            }
            targets.add(input); //지원하는 유형 파일이면 목록에 하나 넣음
        }

        if (targets.isEmpty()) { //목록 비었으면 중단
            System.out.println("분석할 지원 파일이 없습니다: " + input);
            return false;
        }
        //시계 시작
        freq.clear();
        total = 0;
        hasResult = false;
        tried = 0;
        succeeded = 0;
        failed = 0;
        skipped = skip;
        lastPath = input.toString();
        long start = System.nanoTime(); //스톱워치 시작

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

        lastElapsedMs = (System.nanoTime() - start) / 1000000;
        hasResult = succeeded > 0;
        hasSummary = true;
        return true;
    }

    static Map<String, Long> readOneFile(Path input) throws IOException {
        Map<String, Long> local = new HashMap<>();
        TextParser parser = Parsers.find(input);
        for (String text : parser.parse(input)) {
            countText(text, local);
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
    }

    static void showTop(Scanner scanner) {
        if (!hasResult) {
            System.out.println("조회 및 저장할 분석 결과가 없습니다.");
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

    static void searchWord(Scanner scanner) {
        if (!hasResult) {
            System.out.println("조회 및 저장할 분석 결과가 없습니다.");
            return;
        }
        while (true) {
            System.out.print("찾을 단어 > ");
            String word = scanner.nextLine().trim();
            List<String> tokens = WordRules.toTokens(word);
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

    static void saveResult() {
        if (!hasResult) {
            System.out.println("조회 및 저장할 분석 결과가 없습니다.");
            return;
        }
        Path out = Path.of("out", "counts.tsv");
        List<Map.Entry<String, Long>> list = sortedEntries();
        try {
            ResultWriter.save(list, out);
            System.out.println("전체 결과 " + list.size() + "개 단어를 " + out + "에 저장했습니다.");
        } catch (IOException e) {
            System.out.println("저장에 실패했습니다: " + out + " (" + e.getClass().getSimpleName() + ")");
        }
    }

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
        for (String token : WordRules.toTokens(text)) {
            target.merge(token, 1L, Long::sum);
        }
    }
}

