package kr.sesac.wordcounter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;

public class Main {

    static HashMap<String, Long> freq = new HashMap<>(); //추가
    static long total = 0; //전체 단어 수 셀 변수 추가
    static final String[] CSV_COLUMNS = {"text"}; // 표라서 배열 []
    static final String[] TSV_COLUMNS = {"document"}; // 표라서 배열 []
    static final String HTML_SELECTOR = "#content"; // # - id란 뜻

    public static void main(String[] args) throws IOException {
        Path input = Path.of("samples/equivalent/basic.txt");
        System.out.println("문서 단어 분석기 - 시작 코드");
        System.out.println("입력 파일: " + input);
        System.out.println();

        analyze(input);

        System.out.println();
        System.out.println("파일 읽기 성공. 다음 단계는 단어 분리와 카운팅입니다.");
        System.out.println("구현 후 전체 9개·6종인지 expected/basic-counts.tsv와 비교하세요.");
        System.out.println("전체 단어: " + total);
        System.out.println("서로 다른 단어: " + freq.size());
        System.out.println(freq);
    }

    static void analyze(Path input) throws IOException { //파일 읽기 담당
        freq.clear();//새로 분석하기 전에 표 비우기
        total = 0;
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

        } else if (name.endsWith(".tsv")) {

        } else if (name.endsWith(".html") || name.endsWith(".htm")) {

        }
    }

    static void countText(String text) { //단어 세기 담당
        String[] tokens = text.toLowerCase().split("[^A-Za-z0-9가-힣ㄱ-ㅎㅏ-ㅣ]+"); //추가
        for (String token : tokens) { //추가
            if (token.isEmpty()) continue; //빈 문자열이면 건너뛰기
            if (isNumberOnly(token)) continue; //숫자만이면 건너뛰기
            freq.merge(token, 1L, Long::sum); //표에 + 1
            total = total + 1; // 전체 수 +1
            //소문자로 바꾸고 영문 숫자 한글이 아닌 문자에서 잘라 배열 만들기, 하나씩 꺼내보고 비거나 숫자만이면 거르기
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

