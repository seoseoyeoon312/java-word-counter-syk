package kr.sesac.wordcounter;

import java.util.ArrayList;
import java.util.List;

public class WordRules {
    // 영문·숫자·한글이 아닌 글자를 구분자로 봄
    static final String SPLIT_PATTERN = "[^A-Za-z0-9가-힣ㄱ-ㅎㅏ-ㅣ]+";

    // 텍스트 → 규칙을 적용한 토큰 목록 (소문자, 빈 토큰 ~ 숫자만 제외)
    static List<String> toTokens(String text) {
        List<String> result = new ArrayList<>();
        for (String token : text.toLowerCase().split(SPLIT_PATTERN)) {
            if (token.isEmpty()) continue;
            if (isNumberOnly(token)) continue;
            result.add(token);
        }
        return result;
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