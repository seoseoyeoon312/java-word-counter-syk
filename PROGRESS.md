# 프로젝트 진행 기록

이 파일을 채워 10월 1일 발표용 PPT를 준비합니다. 구현하지 않은 기능은 '미구현'으로 표시하고, 심화 항목은 진행한 경우에만 작성하세요. 발표는 분석·조회·저장 시연을 포함해 5~10분입니다. 10월 1일 전후로 코드와 이 파일을 본인의 GitHub 저장소에 업로드하고, 저장소 링크를 강사에게 전달해 리뷰를 받습니다. 자세한 안내는 [진행 기록과 발표](docs/project-guide.md)에 있습니다.

## 1. 실행 방법

- JDK: 21
- IntelliJ에서 실행할 클래스: kr.sesac.wordcounter.Main
- 작업 디렉터리(`pom.xml`이 있는 폴더): 프로젝트 루트 (java-word-counter-main)
- 설정 위치와 현재 값:(심화 8 구조 개선 후 각 파서 클래스 상단 상수로 이동)
  - CSV 열: `CsvParser.COLUMNS = {"text"}`
  - TSV 열: `TsvParser.COLUMNS = {"document"}`
  - HTML 본문 선택자: `HtmlParser.SELECTOR = "#content"`
  - JSON 분석 필드(심화 6): `JsonParser.FIELDS = {"text"}`
- 추가 라이브러리(심화 6): Gson 2.11.0 (pom.xml)

## 2. 구현한 기능

| 기능 | 상태(완료·진행 중·미구현) | 확인한 입력과 결과 |
|---|---|---|
| TXT 카운팅 | 완료 | `basic.txt` → 9개, 6종 ✅ |
| CSV, TSV, HTML 처리 | 완료 | `basic.csv` → 9개, 6종 ✅<br>`basic.tsv` → 9개, 6종 ✅<br>`basic.html` → 9개, 6종 ✅ |
| 여러 파일 순차 처리 | 완료 | `samples/equivalent` 폴더 → 36개, 6종 (시도 4, 성공 4) ✅ |
| 상위 단어, 특정 단어 조회 | 완료 | N=2 → 2개 ✅<br>N=999, 빈 입력 → 있는 6개만 ✅<br>`abc`, `0`, `-1` → 안내 후 재입력 ✅<br>`JAVA!` → java : 3회 ✅<br>`없는단어` → 0회 ✅<br>`java 자바`, `123` → 단어 하나 입력 안내 ✅ |
| 전체 결과 저장 | 완료 | `out/counts.tsv` 저장 → `expected/basic-counts.tsv`와 일치 ✅ |
| 잘못된 입력, 실패 파일, 빈 파일 처리 | 완료 | 없는 경로, 미지원 파일, 지원 파일 없음 → 안내 후 재입력 ✅<br>invalid 4개 → 각각 실패 ✅<br>invalid 폴더 → 요약만 가능, 조회와 저장 차단 ✅<br>`error-demo` 폴더 → 시도 2, 성공 1, 실패 1이고 결과가 `basic-counts.tsv`와 일치 ✅<br>memo.bin 추가 → 건너뜀 1 ✅<br>빈 파일 3종 → 0개, 0종 정상 ✅<br>저장 실패 → 안내 후 조회 계속 ✅ |

## 3. 정확성 확인과 처리 시간

- 작은 기본 샘플의 전체 결과를 정답과 비교한 방법: `out/counts.tsv`와 `expected/basic-counts.tsv`를 Compare Files로 비교 → 단어, 횟수, 정렬 순서 모두 일치 (줄바꿈 문자만 차이)
- CSV 따옴표와 줄바꿈을 확인한 결과: `samples/edge/quoted-lines.csv` → 5개, 3종 ✅ (따옴표 안의 쉼표와 줄바꿈을 셀 하나로 인식)
- 결과 저장 파일 위치: `out/counts.tsv` (UTF-8, 탭 구분)
- 일부 파일이 실패했을 때 확인한 결과:

| 넣은 입력 | 결과 | 확인한 것 |
|---|---|---|
| `samples/invalid`의 깨진 파일 4개를 하나씩 | 4번 모두 실패 1 | 프로그램 중단 없이 실패한 파일 경로와 이유 출력 |
| `data/local/error-demo` 폴더 (정상 basic.txt, 깨진 broken-quote.csv) | 성공 1, 실패 1 | 저장 결과가 basic.txt만 분석한 정답과 일치, 깨진 파일의 단어는 합계에서 제외 |

CSV의 분석 열은 `text`입니다. 아래 입력은 각각 따로 실행합니다. 정답은 [필수 요구사항의 큰 데이터 처리](docs/requirements.md#10-큰-데이터-처리)를 참고하세요.

| 입력 | 데이터 건수 / 파일 수 | 전체 단어 수 | 종류 수 | 처리 시간 | 완료·오류 |
|---|---|---|---|---|---|
| `data/klue-ynat/news-1000.csv` | 1,000 / 1 | 6,991 | 5,052 | 10ms | 완료 |
| `data/klue-ynat/news-10000.csv` | 10,000 / 1 | 70,374 | 28,871 | 139ms | 완료 |
| `data/klue-ynat/news-full.csv` | 45,678 / 1 | 321,084 | 78,309 | 161ms | 완료 |
| `data/klue-ynat/many` | 45,678 / 16 | 321,084 | 78,309 | 369ms | 완료 |

- 처리 시간은 1회 측정값. 실행 순서를 바꿔 같은 입력을 반복 측정한 결과

| 실행 순서 | 입력 | 1회(ms) | 2회(ms) | 3회(ms) |
|---|---|---|---|---|
| 10000 먼저 | news-10000 | 291 | 158 | 42 |
| | news-full | 309 | - | - |
| full 먼저 | news-full | 514 | 229 | 198 |
| | news-10000 | 39 | 63 | 34 |

- 확인한 것: 실행 순서와 관계없이 프로그램 실행 후 첫 분석이 가장 느림, 두 번째 이후에는 news-10000 약 40ms, news-full 약 200ms로 데이터 크기에 비례
- 추정: 첫 분석이 느린 이유는 자바가 처음 실행하는 코드를 준비하는 시간(클래스 로딩, 실행 중 최적화)으로 추정 (claude 참고)

## 4. 구현 중 해결한 문제

**[1] 실패한 파일의 앞부분 단어가 결과에 포함**
- 문제: 폴더 안 파일 중 하나가 중간에 깨지면, 그 파일의 앞부분 단어가 이미 전체 결과에 들어가 제외 불가 (요구사항 9번)
- 원인: 모든 파일의 단어를 하나의 Map(`freq`)에 바로 집계
- 해결하거나 시도한 방법: 파일마다 새 Map에 집계하는 `readOneFile`과, 예외 없이 끝난 파일만 전체에 합치는 `mergeResult`로 분리. 전체 단어 수도 합칠 때만 계산
- 확인한 입력과 결과: invalid 4개 파일 각각 실패 처리, 합계 0개로 앞부분 단어 제외 확인, `error-demo` 폴더 저장 결과가 `basic-counts.tsv`와 일치

**[2] 자동 수정으로 인한 실패 처리 우회 (심화 8)**
- 문제: 파서 클래스 작성 중 빨간 줄을 IntelliJ 자동 수정으로 고치자 `catch (IOException e) { throw new RuntimeException(e); }` 코드 추가, 깨진 파일을 만나면 실패로 세지 않고 프로그램이 멈출 위험
- 원인: `parse` 메서드에 `throws IOException` 선언이 없어 빨간 줄 발생, 자동 수정이 예외를 `RuntimeException`으로 감싸서 던지도록 변경, Main은 파일 읽기 예외만 잡기 때문에 처리 불가
- 해결하거나 시도한 방법: `TextParser`의 `parse`에 `throws IOException` 선언, 자동으로 들어간 try-catch 제거
- 확인한 입력과 결과: invalid 4개 파일 각각 실패 1로 집계, 프로그램은 메뉴로 복귀

**[3] 없는 경로 입력 시 "분석 완료" 출력**
- 문제: 없는 경로를 입력해도 "분석 완료"가 출력되고 이전 분석 요약이 다시 표시
- 원인: `analyze`가 `void`라 호출한 쪽에서 성공 여부 확인 불가, 경로 검사 전에 이전 결과부터 삭제하는 순서
- 해결하거나 시도한 방법: `analyze`가 `boolean`을 반환하도록 변경, 경로, 확장자, 대상 목록 검사를 모두 통과한 뒤에만 이전 결과 초기화
- 확인한 입력과 결과: 없는 경로나 `pom.xml` 입력 시 안내만 출력, 이전 결과 유지


**[4] 피드백 보완**

| 입력 | 수정 전 | 수정 후 (직접 확인) |
|---|---|---|
| 권한 없는 폴더 (`C:\System Volume Information`) | 프로그램 종료 | "폴더를 읽을 수 없습니다 (AccessDeniedException)" 안내 후 재입력 |
| `samples/json/null-text.json` (`[{"text": null}]`) | 프로그램 종료 | 해당 파일만 실패 |
| `samples/json/object-text.json` (`[{"text": {"message": "java"}}]`) | 프로그램 종료 | 해당 파일만 실패 |
| `data/local/bad-encoding.html` (EUC-KR로 저장한 한글) | 성공으로 집계 | 해당 파일만 실패 (MalformedInputException) |
- 폴더 목록: `Files.list` 부분에 catch 추가, 경로와 예외 이름 출력 후 재입력
- JSON: 문자열 값만 허용, JsonNull이나 객체는 `getAsString` 전에 IOException으로 실패 처리
- HTML: `Files.readString`으로 UTF-8 검사 후 jsoup에 전달
- 수정 후 기존 결과 유지 확인: equivalent 36개, 6종 / invalid 4개 실패 / `samples/json` 폴더 시도 4, 성공 1, 실패 3

## 5. 심화(진행한 경우만)

### 심화 6. 분석 기능 확장 (JSON 형식 추가)

- 구현: `JsonParser` 추가 (Gson 2.11.0 사용) `[ {...}, {...} ]`처럼 객체 여러 개가 배열로 들어 있는 JSON에서 객체마다 `text` 필드 값을 꺼내 집계, 읽을 필드는 `JsonParser.FIELDS`에서 변경
- 실패 처리: 배열이 아니거나, `text` 필드가 없거나, 괄호가 안 닫히는 등 JSON이 깨진 경우 실패 처리, Gson이 깨진 JSON에서 던지는 예외를 `IOException`으로 바꿔 다른 형식과 같은 방식으로 실패 집계
- 확인 방법: basic.txt와 같은 내용을 JSON으로 만들어 결과 비교

| 입력 | 결과 | 확인 |
|---|---|---|
| `samples/json/basic.json` (basic.txt의 각 줄을 `text` 필드로 옮김) | 9개, 6종 (basic.txt와 같음) | ✅ |
| `samples/json/broken.json` (괄호가 안 닫힘) | "실패: JSON 형식이 올바르지 않습니다" 출력 후 계속 동작 | ✅ |
| `samples/json/null-text.json` (`text` 값이 null) | "분석 필드가 없거나 문자열이 아닙니다" 출력 후 계속 동작 | ✅ |
| `samples/json/object-text.json` (`text` 값이 객체) | "분석 필드가 없거나 문자열이 아닙니다" 출력 후 계속 동작 | ✅ |
| `samples/json` 폴더 | 시도 4, 성공 1, 실패 3 / 9개, 6종 | ✅ |
| `pom.xml` | 지원하지 않는 형식 안내에 `.json` 자동 추가 | ✅ |
| `samples/equivalent` | 36개, 6종 (JSON 추가 후에도 기존 결과 유지) | ✅ |

### 심화 8. 객체지향 구조 개선

**전체 구조**
```text
Main (메뉴, 입력 검사, 분석 흐름, 집계, 결과 출력)
├─ Parsers.find(파일)       → 확장자에 맞는 파서 찾기
│   └─ TextParser (인터페이스, parse 하나)
│       ├─ TxtParser
│       ├─ CsvParser
│       ├─ TsvParser        (헤더 검사는 CsvParser.checkHeader 사용)
│       ├─ HtmlParser
│       └─ JsonParser
├─ WordRules.toTokens(글)   → 단어로 나누기 (분석과 단어 조회가 같이 씀)
└─ ResultWriter.save(결과)  → out/counts.tsv 저장
```

| 클래스 | 역할 |
|---|---|
| `TextParser` | 파서 인터페이스 |
| `TxtParser` | TXT를 한 줄씩 읽어서 돌려줌 |
| `CsvParser` | CSV의 `text` 열 값을 돌려줌, 헤더와 분석 열 검사(`checkHeader`), 셀 수 검사도 여기서 함 |
| `TsvParser` | TSV(탭 구분, 따옴표 없음)의 `document` 열 값을 돌려줌, 헤더 검사는 `CsvParser.checkHeader` 사용 |
| `HtmlParser` | `#content` 본문이 딱 1개인지 확인하고, script, style, nav, header, footer를 뺀 글을 돌려줌 |
| `JsonParser` | JSON 배열 안 객체마다 `text` 필드 값을 돌려줌 |
| `Parsers` | 확장자별로 어떤 파서를 쓸지 적어둔 등록표, 파일에 맞는 파서를 찾고 안내 문구에 쓸 지원 확장자 목록 생성 |
| `WordRules` | 글을 단어로 나누고 소문자로 바꾼 뒤 빈 단어와 숫자만 있는 단어를 뺌, 분석과 단어 조회가 같은 규칙을 씀 |
| `ResultWriter` | 정렬된 결과를 `out/counts.tsv`(UTF-8, 탭 구분)로 저장 |
| `Main` | 콘솔 메뉴, 입력 검사, 분석 흐름(파일 목록 만들기, 파일별로 따로 세기, 성공한 것만 합치기, 시간 재기), 정렬, 결과 출력 |

**클래스 구분 이유**
- 파서를 인터페이스를 묶어서 Main이 파일 형식을 몰라도 `parse` 하나로 모든 파일을 읽도록 구성
- 확장자와 파서 연결을 `Parsers` 한 곳에 모아서 새 형식을 추가할 때 고칠 곳을 한 군데로 제한
- 분석과 단어 조회가 같은 규칙을 쓰도록 단어 규칙을 `WordRules`로 분리 (`JAVA!`로 조회해도 `java`를 검색)
- 구조를 바꿔도 실패 처리 규칙이 깨지지 않도록 파일마다 따로 세고 성공한 것만 합치는 흐름은 그대로 유지

**새 형식을 추가할 때 바꾸는 곳**
1. `TextParser`를 구현한 파서 클래스 1개 추가
2. `Parsers.BY_EXTENSION`에 확장자와 파서 1줄 추가

**바꾸기 전과 후 결과 비교**
- 방법: 구조를 바꾸기 전과 후에 같은 입력을 넣고 결과 비교

| 입력 | 바꾸기 전 | 바꾼 후 | 같음 |
|---|---|---|---|
| `samples/equivalent` | 36개, 6종 | 36개, 6종 | ✅ |
| `basic.txt`, `basic.html` | 각 9개, 6종 | 각 9개, 6종 | ✅ |
| `samples/invalid` (깨진 파일 4개) | 시도 4, 실패 4, 파일별 이유 출력 | 시도 4, 실패 4, 파일별 이유 출력 | ✅ |
| `data/local/error-demo` | 9개, 6종 (시도 2, 성공 1, 실패 1, 건너뜀 1) | 9개, 6종 (시도 2, 성공 1, 실패 1, 건너뜀 1) | ✅ |
| 없는 경로, `pom.xml` | 안내 후 재입력 | 안내 후 재입력 | ✅ |
| `data/klue-ynat/news-full.csv` | 321,084개, 78,309종 | 321,084개, 78,309종 | ✅ |
| `data/klue-ynat/many` (16개) | 321,084개, 78,309종 | 321,084개, 78,309종 | ✅ |
| `quoted-lines.csv` | 5개, 3종 | 5개, 3종 | ✅ |
| 메뉴 조회 (N=3, `JAVA!`, `123`) | 정답대로 동작 | java 12, 자료구조 8, java17 4 / java : 12회 / 단어 하나 입력 안내 | ✅ |
| 저장 결과 Compare Files | `basic-counts.tsv`와 같음 | `basic-counts.tsv`와 같음 (No differences) | ✅ |

**남은 개선**
- 분석 상태(`freq`, `total`)와 집계 흐름(`analyze`, `readOneFile`, `mergeResult`, `sortedEntries`)이 Main에 남아 있음, 분석을 담당하는 객체로 분리하고 Main은 메뉴와 콘솔 입출력만 담당하도록 변경 예정 (코드 리뷰 의견, 심화 7 시작 전에 진행)
- 파서가 파일 전체 텍스트를 `List<String>`에 담아 반환, 더 큰 파일을 위해 읽으면서 바로 집계하는 방식 검토 (파일별 집계 후 성공한 결과만 합치는 방식은 유지, 코드 리뷰 의견)
- 심화 7(DB에 저장하며 집계)은 발표 후 진행 예정

## 6. AI 대화 또는 참고 자료

### 웹 대화에서 물어본 개념,힌트,오류 설명:
- 단어 분리 정규식, 문자열 비교(equals,isEmpty), Map.merge 동작
- 향상된 for문
- 메서드 분리 (메뉴 반복 호출, 형식별 읽기와 공통 집계 분리), static final 상수
- Commons CSV-jsoup 사용법, TSV의 setDelimiter-setQuote 설정 이유
- fall-through, Scanner 입력, try-catch(NumberFormatException)
- Files.newBufferedWriter로 UTF-8 저장
- (심화 8) 클래스를 나누는 순서(파서 → 등록표 → 단어 규칙,저장)
- (심화 8) 컴파일 오류 원인: 인터페이스를 클래스로 만든 경우, 메서드를 닫는 괄호 위치, IntelliJ가 만든 RuntimeException 감싸기가 실패 처리를 우회하는 문제
- (심화 8) `ExceptionInInitializerError`와 스택 트레이스의 `Caused by` 읽는 법 (등록표 `Map.of` 키 중복)
- (심화 6) Gson 선택과 JSON 트리 읽는 법(`fromJson`, `getAsJsonArray`, `get`) 설명
- (심화 6, 8) JsonParser 작성과 파서 클래스 분리는 막힌 부분에서 AI에게 코드를 받아, 한 줄씩 설명을 듣고 이해한 뒤 옮겨 넣고 직접 실행해서 결과 확인 (확인용 샘플 basic.json, broken.json은 직접 작성)
- (피드백 반영) JsonNull과 Java null의 차이, checked와 unchecked 예외의 차이, jsoup에 파일을 바로 넘길 때와 `Files.readString`으로 먼저 읽을 때의 인코딩 오류 처리 차이
- 처리 시간이 데이터 크기에 비례하지 않은 이유 (프로그램을 켜고 처음 돌린 분석이 느리게 나옴)
- git 커밋 경고(CRLF → LF)의 의미

### 도움을 바탕으로 직접 구현한 내용:
- 단어 분리와 집계(countText), 숫자만 있는 단어 판별(isNumberOnly), 조회용 단어 추출(toTokens)
- 확장자별 분기와 형식별 읽기 (개발 가이드 예제를 입력 경로, 설정 상수, countText 호출에 맞게 수정)
- 콘솔 메뉴 6개, 상위 N개 조회, 특정 단어 조회, 전체 결과 저장, 요약과 처리 시간
- (피드백 반영) 테스트 파일 작성 (null-text.json, object-text.json, bad-encoding.html)과 처리 시간 반복 측정

### 직접 확인한 입력과 결과:
- basic.txt, csv, tsv, html 모두 9개, 6종
- out/counts.tsv와 expected/basic-counts.tsv를 Compare Files로 비교해서 일치
- 상위 N개 조회(N=2, 999, 빈 입력, abc, 0, -1)와 단어 조회(JAVA!, 없는단어, java 자바, 123) 모두 정답대로 동작
- samples/equivalent 폴더 36개, 6종, invalid 4개 각각 실패, header-only.csv는 0개, 0종으로 정상
- basic.json 9개, 6종 (broken.json, null-text.json, object-text.json은 실패로 세고 계속 동작)
- 권한 없는 폴더는 안내 후 재입력, bad-encoding.html은 실패
- 구조를 바꾸기 전과 후 결과가 같음, JSON 추가 후에도 기존 형식 결과 그대로
- 처리 시간은 실행 순서를 바꿔 같은 입력을 3번씩 측정

### 참고 링크:
- 과제 저장소의 개발 가이드 (docs/guide.md), 심화 요구사항 (docs/advanced.md) 6번, 8번
- Apache Commons CSV: https://commons.apache.org/proper/commons-csv/
- jsoup Cookbook: https://jsoup.org/cookbook/
- Gson User Guide: https://github.com/google/gson/blob/main/UserGuide.md
- Baeldung, Counting Words in a String with Java: https://www.baeldung.com/java-word-counting
- Baeldung, Checked and Unchecked Exceptions in Java: https://www.baeldung.com/java-checked-unchecked-exceptions

## 7. 발표할 내용
- 구현한 기능과 전체 처리 흐름: 경로 검사 → 확장자에 맞는 파서 선택 → 형식별 텍스트 추출 → 파일별 임시 Map에 집계 → 성공한 파일만 전체 결과에 합산 → 요약, 조회, 저장
- 분석 → 조회 → 저장 시연: basic.txt 분석 → 상위 2개 (java 3회, 자료구조 2회) → `JAVA!` 조회 (java 3회) → 저장 후 `expected/basic-counts.tsv`와 Compare Files
- 해결한 문제 또는 성능 실험에서 알게 된 점: 실패한 파일은 버리고 성공한 파일만 합치는 실패 처리, IntelliJ 자동 수정이 IOException을 RuntimeException으로 감싸 실패 처리가 우회된 문제와 수정 전후 예외가 잡히는 위치
- 남은 문제와 더 개선하고 싶은 부분: 분석 상태와 집계 흐름을 Main 밖 객체로 분리, 읽으면서 바로 집계해 메모리 사용 줄이기, CSV 오류 메시지 한글화, 심화 7(DB 저장)