# 프로젝트 진행 기록

이 파일을 채워 10월 1일 발표용 PPT를 준비합니다. 구현하지 않은 기능은 '미구현'으로 표시하고, 심화 항목은 진행한 경우에만 작성하세요. 발표는 분석·조회·저장 시연을 포함해 5~10분입니다. 10월 1일 전후로 코드와 이 파일을 본인의 GitHub 저장소에 업로드하고, 저장소 링크를 강사에게 전달해 리뷰를 받습니다. 자세한 안내는 [진행 기록과 발표](docs/project-guide.md)에 있습니다.

## 1. 실행 방법

- JDK: 21
- IntelliJ에서 실행할 클래스: kr.sesac.wordcounter.Main
- 작업 디렉터리(`pom.xml`이 있는 폴더): 프로젝트 루트(java-word-counter-main)
- 설정 위치와 현재 값:`Main.java` 상단 상수 
CSV 열 : CSV_COLUMNS = {"text"} / 
TSV 열 : TSV_COLUMNS = {"document"}  / 
HTML 본문 선택자 : HTML_SELECTOR = "#content"



## 2. 구현한 기능

| 기능 | 상태(완료·진행 중·미구현) | 확인한 입력과 결과 |
|---|---|---|
| TXT 카운팅 | 완료 |`basic.txt` → 9개·6종 ✅|
| CSV·TSV·HTML 처리 | 완료 |`basic.csv` → 9개·6종 ✅<br>`basic.tsv` → 9개·6종 ✅<br>`basic.html` → 9개·6종 ✅|
| 여러 파일 순차 처리 | 완료 |`samples/equivalent` 폴더 → 시도 4·성공 4, 36개·6종 ✅|
| 상위 단어·특정 단어 조회 | 완료 | N=2 → 2개 / N=999·빈 입력 → 있는 6개만 ✅<br>`abc`·`0`·`-1` → 안내 후 재입력 ✅<br>`JAVA!` → java : 3회, `없는단어` → 0회 ✅<br>`java 자바`·`123` → 단어 하나 입력 안내 ✅|
| 전체 결과 저장 | 완료 |`out/counts.tsv` 저장 → `expected/basic-counts.tsv`와 일치 ✅|
| 잘못된 입력·실패 파일·빈 파일 처리 | 완료 |없는 경로·미지원 파일·지원 파일 없음 → 안내 후 재입력 ✅<br>invalid 4개 각각 실패 ✅, invalid 폴더 → 요약만 가능·조회·저장 차단 ✅<br>부분 성공(error-demo) → 시도2·성공1·실패1, 결과 basic-counts.tsv와 일치 ✅<br>memo.bin 추가 → 건너뜀 1 ✅<br>빈 파일 3종 → 0개·0종 정상 ✅<br>저장 실패 → 안내 후 조회 계속 ✅|

## 3. 정확성 확인과 처리 시간

- 작은 기본 샘플의 전체 결과를 정답과 비교한 방법: out/counts.tsv와 expected/basic-counts.tsv를 IntelliJ Compare Files로 비교 → 단어·횟수·정렬 순서 모두 일치(줄 구분자 차이만, 요구사항에서 허용)
- CSV 따옴표·줄바꿈을 확인한 결과:
- 일부 파일이 실패했을 때 확인한 결과:
- 결과 저장 파일 위치: out/counts.tsv (UTF-8, 탭 구분)
- 일부 파일이 실패했을 때 확인한 결과: samples/invalid의 4개 파일을 각각 분석 → 모두 시도 1·성공 0·실패 1, 실패 안내에 파일 경로와 이유 출력 (부분 성공 실험은 진행 예정)
- 일부 파일이 실패했을 때 확인한 결과: data/local/error-demo(basic.txt + broken-quote.csv) → 시도 2·성공 1·실패 1, 저장 결과가 expected/basic-counts.tsv와 일치(줄 구분자 차이만). 실패 파일의 앞부분 단어가 합계에 섞이지 않음 확인

CSV의 분석 열은 `text`입니다. 아래 입력은 각각 따로 실행합니다. 정답은 [필수 요구사항의 큰 데이터 처리](docs/requirements.md#10-큰-데이터-처리)를 참고하세요.

| 입력 | 데이터 건수 / 파일 수 | 전체 단어 수 | 종류 수 | 처리 시간 | 완료·오류 |
|---|---|---|---|---|---|
| `data/klue-ynat/news-1000.csv` | 1,000 / 1 | 6,991 | 5,052 | 10ms | 완료 |
| `data/klue-ynat/news-10000.csv` | 10,000 / 1 | 70,374 | 28,871 | 139ms | 완료 |
| `data/klue-ynat/news-full.csv` | 45,678 / 1 | 321,084 | 78,309 | 161ms | 완료  |
| `data/klue-ynat/many` | 45,678 / 16, 순차 처리 | 321,084 | 78,309 | 369ms | 완료 |

- 전체 파일 하나와 16개 파일의 **모든 단어별 횟수**를 비교한 방법과 결과:

## 4. 구현 중 해결한 문제

1~3가지를 골라 적으세요. 잘 해결되지 않은 문제도 시도한 내용과 함께 적어도 됩니다.

- 문제:
- 원인:
- 해결하거나 시도한 방법:
- 확인한 입력과 결과:

[1]
- 문제: 폴더 안 파일 중 하나가 중간에 깨지면, 그 파일의 앞부분 단어가 이미 전체 결과에 들어가 제외할 수 없음 (요구사항 9번 위반)
- 원인: 모든 파일의 단어를 하나의 Map(freq)에 바로 집계하고 있었음
- 해결: 파일마다 새 Map에 집계하는 readOneFile과, 예외 없이 끝난 파일만 전체에 합치는 mergeResult로 분리. 전체 단어 수도 합칠 때만 계산
- 확인: invalid 4개 파일 각각 실패 처리, 합계 0개로 앞부분 단어 제외 확인

[2]
- 문제: 숫자만 있는 토큰(123)이 집계되어 basic.txt가 10개·7종으로 나옴
- 원인: 숫자 판별 후 continue하는 위치가 freq.merge 뒤에 있어 이미 센 뒤에 건너뜀
- 해결: 거르는 조건(빈 문자열·숫자만)을 집계보다 앞으로 이동
- 확인: 10개·7종 → 9개·6종

[3]
- 문제: 없는 경로를 입력해도 "분석 완료"가 출력되고, 이전 분석 요약이 다시 표시됨
- 원인: analyze가 void라 호출한 쪽에서 성공·실패를 알 수 없었고, 경로 검사 전에 이전 결과를 지우는 순서였음
- 해결: analyze가 boolean을 반환하도록 바꾸고, 경로·확장자·대상 목록 검사를 모두 통과한 뒤에만 이전 결과를 초기화
- 확인: 없는 경로·pom.xml 입력 시 안내만 출력, 이전 결과 유지

## 5. 심화(진행한 경우만)

- 한 파일 처리 개선: 바꾼 부분, 전후 시간, 결과 동일 여부
- 여러 파일 병렬 처리: 입력 폴더, 스레드 수 1·2·4, 전후 시간, 결과 동일 여부
- 중단 후 재개·데이터 수집·기타: 사용법과 확인한 결과

성능을 비교했다면 측정 기기·JDK, 예열·반복 횟수, 전체 작업의 중앙값을 적습니다. 표 양식은 [심화 요구사항](docs/advanced.md)에 있습니다.

## 6. AI 대화 또는 참고 자료

### 웹 대화에서 물어본 개념·힌트·오류 설명:
- 단어 분리 정규식, 문자열 비교(equals·isEmpty), Map.merge 동작
- char 비교와 인덱스 범위(0 ~ length-1), 향상된 for문
- 메서드 분리 이유(메뉴 반복 호출, 형식별 읽기와 공통 집계 분리), static final 상수
- Commons CSV·jsoup 사용법, TSV의 setDelimiter·setQuote 설정 이유
- switch 화살표 문법과 fall-through, Scanner 입력, try-catch(NumberFormatException)
- Map.Entry와 List 정렬(Comparator), Files.newBufferedWriter로 UTF-8 저장
- 예외 메시지(InvalidPathException) 읽는 법
  
### 도움을 바탕으로 직접 구현한 내용:
- 단어 분리·집계(countText), 숫자 토큰 판별(isNumberOnly), 조회용 토큰 추출(toTokens)
- 확장자별 분기와 형식별 읽기(개발 가이드 예제를 입력 경로·설정 상수·countText 호출로 수정)
- 콘솔 메뉴 6개, 상위 N개 조회, 특정 단어 조회, 전체 결과 저장, 요약·처리 시간

### 직접 확인한 입력과 결과:
- basic.txt·csv·tsv·html 모두 9개·6종, 같은 파일 연속 분석 시 누적 없음
- out/counts.tsv와 expected/basic-counts.tsv Compare Files 일치
- 메뉴 확인 표(N=2·999·빈 입력·abc·0·-1, JAVA!·없는단어·java 자바·123) 전부 정답대로 동작
- samples/equivalent 폴더 → 36개·6종, invalid 4개 각각 실패, header-only.csv 정상 0개·0종

### 참고 링크: 
- 과제 저장소의 개발 가이드(docs/guide.md) CSV·HTML 읽기 예제

사용하지 않았다면 사용하지 않았다고 적으면 됩니다.

## 7. 발표할 내용

- 구현한 기능과 전체 처리 흐름
- 시연할 파일·폴더와 정답
- 분석 → 조회 → 저장 시연
- 해결한 문제 또는 성능 실험에서 알게 된 점
- 남은 문제와 더 개선하고 싶은 부분
