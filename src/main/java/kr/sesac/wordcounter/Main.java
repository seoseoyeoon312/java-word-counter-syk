package kr.sesac.wordcounter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class Main {

    // TODO 1: 단어별 출현 횟수를 저장할 자료구조를 준비하세요. (요구사항 4. 단어별 횟수 집계)
    static HashMap<String, Long> freq = new HashMap<>(); //추가
    static long total = 0; //전체 단어 수 셀 변수 추가

    public static void main(String[] args) throws IOException {
        Path input = Path.of("samples/equivalent/basic.txt");

        System.out.println("문서 단어 분석기 - 시작 코드");
        System.out.println("입력 파일: " + input);
        System.out.println();

        analyze(input);

        System.out.println();
        System.out.println("파일 읽기 성공. 다음 단계는 단어 분리와 카운팅입니다.");
        System.out.println("구현 후 전체 9개·6종인지 expected/basic-counts.tsv와 비교하세요.");
        // TODO 4: 원문 출력 대신 집계 결과를 출력하세요.
        // TXT 카운팅 완성 후 다른 형식, 메뉴, 오류 처리, 저장을 추가하세요.
        System.out.println("전체 단어: " + total);
        System.out.println("서로 다른 단어: " + freq.size());
        System.out.println(freq);
    }

    static void analyze(Path input) throws IOException { //새로 분석하기 전에 표를 싹 비우기
        freq.clear();
        total = 0;

        try (BufferedReader reader =
                     Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
                // TODO 2: line을 요구사항 3. 단어 처리 규칙대로 단어(토큰)로 나누세요.
                String[] tokens = line.toLowerCase().split("[^A-Za-z0-9가-힣ㄱ-ㅎㅏ-ㅣ]+"); //추가
                for (String token : tokens) { //추가
                    if (token.isEmpty()) {
                        continue;
                    }
                    // TODO 3: 숫자만 있는 단어는 제외하고 단어별 횟수를 늘리세요.
                    if (isNumberOnly(token)) {
                        continue;
                    }
                    freq.merge(token, 1L, Long::sum);
                    total = total + 1;

                }

            }
        }

    }

    static boolean isNumberOnly(String word) {
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (!(c >= '0' && c <= '9')) {
                return false;
            }
        }
        return true;

    }
}

