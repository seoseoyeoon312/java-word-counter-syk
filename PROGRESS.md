# 프로젝트 진행 기록

이 파일을 채워 10월 1일 발표용 PPT를 준비합니다. 구현하지 않은 기능은 '미구현'으로 표시하고, 심화 항목은 진행한 경우에만 작성하세요. 발표는 분석·조회·저장 시연을 포함해 5~10분입니다. 10월 1일 전후로 코드와 이 파일을 본인의 GitHub 저장소에 업로드하고, 저장소 링크를 강사에게 전달해 리뷰를 받습니다. 자세한 안내는 [진행 기록과 발표](docs/project-guide.md)에 있습니다.

## 1. 실행 방법

- JDK: 21
- IntelliJ에서 실행할 클래스: `kr.sesac.wordcounter.Main`
- 작업 디렉터리(`pom.xml`이 있는 폴더): 프로젝트 루트 (java-word-counter-main)
- 설정 위치와 현재 값 (각 파서 클래스 상단 상수)
  - CSV 열: `CsvParser.COLUMNS = {"text"}`
  - TSV 열: `TsvParser.COLUMNS = {"document"}`
  - HTML 본문 선택자: `HtmlParser.SELECTOR = "#content"`
  - JSON 분석 필드(심화 6): `JsonParser.FIELDS = {"text"}`
- 추가 라이브러리(심화 6): Gson 2.11.0 (pom.xml)

## 2. 구현한 기능

| 기능 | 상태(완료·진행 중·미구현) | 확인한 입력과 결과 |
|---|---|---|
| TXT 카운팅 | 완료 | basic.txt → 9개, 6종 ✅ |
| CSV, TSV, HTML 처리 | 완료 | basic.csv, basic.tsv, basic.html → 각 9개, 6종 ✅ |
| 여러 파일 순차 처리 | 완료 | samples/equivalent → 36개, 6종 (시도 4, 성공 4) ✅ |
| 상위 단어, 특정 단어 조회 | 완료 | N=2 → 2개, N=999와 빈 입력 → 6개 전부, abc, 0, -1 → 재입력 ✅<br>JAVA! → java 3회, 없는단어 → 0회, `java 자바`, 123 → 단어 하나 입력 안내 ✅ |
| 전체 결과 저장 | 완료 | out/counts.tsv → expected/basic-counts.tsv와 일치 ✅ |
| 잘못된 입력, 실패 파일, 빈 파일 처리 | 완료 | 없는 경로, 미지원 파일, 지원 파일 없음 → 안내 후 재입력 ✅<br>invalid 4개 → 각각 실패, 조회와 저장 차단 ✅<br>error-demo → 시도 2, 성공 1, 실패 1, 건너뜀 1 ✅<br>빈 파일 3종 → 0개, 0종 ✅ / 저장 실패 → 안내 후 계속 ✅ |

## 3. 정확성 확인과 처리 시간

- 정답 비교: out/counts.tsv와 expected/basic-counts.tsv를 Compare Files로 비교, 단어, 횟수, 정렬 순서 모두 일치 (줄바꿈 문자만 차이)
- CSV 따옴표와 줄바꿈: quoted-lines.csv → 5개, 3종 ✅
- 기타 샘플: leading-symbols.txt → 8개, 7종 ✅ / two-columns.csv (COLUMNS를 Q, A로 바꿔 실행) → 4개, 3종 ✅
- 결과 저장 파일 위치: `out/counts.tsv` (UTF-8, 탭 구분)
- 일부 파일이 실패했을 때: invalid 4개를 하나씩 → 모두 실패 1, 경로와 이유 출력 / error-demo → 저장 결과가 basic.txt만 분석한 정답과 일치

CSV의 분석 열은 `text`입니다. 아래 입력은 각각 따로 실행합니다. 정답은 [필수 요구사항의 큰 데이터 처리](docs/requirements.md#10-큰-데이터-처리)를 참고하세요.

| 입력 | 데이터 건수 / 파일 수 | 전체 단어 수 | 종류 수 | 처리 시간 | 완료·오류 |
|---|---|---|---|---|---|
| `data/klue-ynat/news-1000.csv` | 1,000 / 1 | 6,991 | 5,052 | 10ms | 완료 |
| `data/klue-ynat/news-10000.csv` | 10,000 / 1 | 70,374 | 28,871 | 139ms | 완료 |
| `data/klue-ynat/news-full.csv` | 45,678 / 1 | 321,084 | 78,309 | 161ms | 완료 |
| `data/klue-ynat/many` | 45,678 / 16 | 321,084 | 78,309 | 369ms | 완료 |

처리 시간 반복 측정 (실행 순서를 바꿔 3회씩)

| 실행 순서 | 입력 | 1회(ms) | 2회(ms) | 3회(ms) |
|---|---|---|---|---|
| 10000 먼저 | news-10000 | 190 | 103 | 51 |
| | news-full | 152 | 133 | 111 |
| full 먼저 | news-full | 514 | 229 | 198 |
| | news-10000 | 39 | 63 | 34 |

- 확인한 것: 순서와 관계없이 첫 분석이 가장 느림, 이후에는 데이터 크기에 따라 시간 증가
- 추정: 자바가 처음 실행하는 코드를 준비하는 시간(클래스 로딩, 실행 중 최적화) 때문으로 추정

## 4. 구현 중 해결한 문제

### [1] 실패한 파일의 앞부분 단어가 결과에 포함

- 문제: 파일이 중간에 깨지면 앞부분 단어가 이미 전체 결과에 들어가 제외 불가 (요구사항 9번)
- 원인: 모든 파일의 단어를 하나의 Map(`freq`)에 바로 집계
- 해결: 파일마다 새 Map에 세는 `readOneFile`, 예외 없이 끝난 파일만 합치는 `mergeResult`로 분리
- 확인: invalid 4개 각각 실패, 합계 0개 / error-demo 저장 결과가 basic-counts.tsv와 일치

### [2] 자동 수정으로 인한 실패 처리 우회 (심화 8)

- 문제: 파서 작성 중 빨간 줄을 IntelliJ 자동 수정으로 고치자 `catch (IOException e) { throw new RuntimeException(e); }` 추가, 깨진 파일에서 프로그램 종료
- 원인: `parse`에 `throws IOException` 선언이 없어 빨간 줄 발생, 자동 수정이 RuntimeException으로 바꿔 던지고 Main은 파일 읽기 예외만 잡아서 처리 불가
- 해결: `TextParser.parse`에 `throws IOException` 선언, 자동으로 들어간 catch 제거
- 확인: 수정 전 코드로 깨진 txt 실행 시 스택 트레이스와 함께 종료, 수정 후 실패 1로 집계하고 메뉴로 복귀 / invalid 4개 각각 실패 1

### [3] 없는 경로 입력 시 "분석 완료" 출력

- 문제: 없는 경로를 넣어도 "분석 완료"와 이전 요약 출력
- 원인: `analyze`가 void라 성공 여부 확인 불가, 검사 전에 이전 결과부터 삭제
- 해결: `analyze`가 boolean 반환, 모든 검사를 통과한 뒤에만 이전 결과 초기화
- 확인: 없는 경로, pom.xml 입력 시 안내만 출력, 이전 결과 유지

### [4] 피드백 보완

| 입력 | 수정 전 | 수정 후 |
|---|---|---|
| 권한 없는 폴더 (`C:\System Volume Information`) | 프로그램 종료 | "폴더를 읽을 수 없습니다 (AccessDeniedException)" 후 재입력 |
| `null-text.json` (`[{"text": null}]`) | 프로그램 종료 | 해당 파일만 실패 |
| `object-text.json` (`[{"text": {"message": "java"}}]`) | 프로그램 종료 | 해당 파일만 실패 |
| `bad-encoding.html` (EUC-KR 한글) | 성공으로 집계 | 해당 파일만 실패 (MalformedInputException) |

- 수정: 폴더 목록 읽기에 catch 추가 / JSON은 문자열 값만 허용 / HTML은 `Files.readString`으로 UTF-8 검사 후 jsoup에 전달
- 기존 결과 유지 확인: equivalent 36개, 6종 / invalid 4개 실패 / samples/json 시도 4, 성공 1, 실패 3

## 5. 심화(진행한 경우만)

### 심화 6. 분석 기능 확장 (JSON 형식 추가)

- 구현: `JsonParser` 추가 (Gson 2.11.0), 배열 안 객체마다 `text` 필드 값을 꺼내 집계, 필드는 `JsonParser.FIELDS`에서 변경
- 실패 처리: 배열이 아니거나, 필드가 없거나 문자열이 아니거나, JSON이 깨진 경우 실패, Gson 예외는 IOException으로 바꿔 다른 형식과 같은 방식으로 집계

| 입력 | 결과 | 확인 |
|---|---|---|
| `basic.json` (basic.txt의 각 줄을 text 필드로) | 9개, 6종 (basic.txt와 같음) | ✅ |
| `broken.json` (괄호가 안 닫힘) | "JSON 형식이 올바르지 않습니다" 후 계속 | ✅ |
| `null-text.json`, `object-text.json` | "분석 필드가 없거나 문자열이 아닙니다" 후 계속 | ✅ |
| `samples/json` 폴더 | 시도 4, 성공 1, 실패 3 / 9개, 6종 | ✅ |
| `samples/equivalent` | 36개, 6종 (JSON 추가 후에도 유지) | ✅ |

### 심화 8. 객체지향 구조 개선

**바꾸기 전과 후 비교: 같은 입력을 넣고 결과 비교**
```
Main (메뉴, 입력 검사, 분석 흐름, 집계, 결과 출력)
├─ Parsers.find(파일)       → 확장자에 맞는 파서 찾기, 지원 확장자 안내 문구 생성
│   └─ TextParser (인터페이스, parse 하나)
│       ├─ TxtParser        한 줄씩
│       ├─ CsvParser        text 열, 헤더와 셀 수 검사
│       ├─ TsvParser        document 열 (탭 구분, 따옴표 없음, 헤더 검사는 CsvParser.checkHeader 사용)
│       ├─ HtmlParser       #content 1개만, script, style, nav, header, footer 제외
│       └─ JsonParser       배열 안 객체마다 text 필드
├─ WordRules.toTokens(글)   → 단어 나누기, 소문자, 숫자만 있는 단어 제외 (분석과 조회가 같이 씀)
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

| 입력 | 바꾸기 전과 후 결과 |
|---|---|
| samples/equivalent | 36개, 6종 ✅ |
| basic.txt, basic.html | 각 9개, 6종 ✅ |
| samples/invalid | 시도 4, 실패 4, 파일별 이유 출력 ✅ |
| data/local/error-demo | 9개, 6종 (시도 2, 성공 1, 실패 1, 건너뜀 1) ✅ |
| 없는 경로, pom.xml | 안내 후 재입력 ✅ |
| news-full.csv, many (16개) | 321,084개, 78,309종 ✅ |
| 저장 결과 Compare Files → basic-counts.tsv와 같음 (줄바꿈 문자만 차이) ✅

**남은 개선**
- 분석 상태(`freq`, `total`)와 집계 흐름이 Main에 남아 있음, 분석 객체로 분리하고 Main은 메뉴와 입출력만 담당하도록 변경 예정 (심화7 구현 전에 진행할 예정)
- 파서가 파일 전체를 `List<String>`에 담아 반환, 읽으면서 바로 집계하는 방식 검토 (파일별 집계 후 성공한 것만 합치는 방식은 유지)
- 심화 7(DB에 저장하며 집계)은 발표 후 진행 예정

## 6. AI 대화 또는 참고 자료

**웹 대화에서 물어본 개념,힌트,오류 설명**
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

**도움을 바탕으로 직접 구현한 내용**
- 단어 분리와 집계(`countText`, `toTokens`, `isNumberOnly`)
- 확장자별 분기와 형식별 읽기 (개발 가이드 예제를 입력 경로, 설정 상수, countText 호출에 맞게 수정)
- 콘솔 메뉴 6개, 상위 N개 조회, 특정 단어 조회, 전체 결과 저장, 요약과 처리 시간
- (피드백 반영) 테스트 파일 작성과 처리 시간 반복 측정

**직접 확인한 입력과 결과**: 2장, 3장, 4장, 5장 표의 모든 입력

**참고 링크**
- 과제 저장소의 개발 가이드 (docs/guide.md), 심화 요구사항 (docs/advanced.md) 6번, 8번
- Apache Commons CSV: https://commons.apache.org/proper/commons-csv/
- jsoup Cookbook: https://jsoup.org/cookbook/
- Gson User Guide: https://github.com/google/gson/blob/main/UserGuide.md
- Baeldung, Counting Words in a String with Java: https://www.baeldung.com/java-word-counting
- Baeldung, Checked and Unchecked Exceptions in Java: https://www.baeldung.com/java-checked-unchecked-exceptions

## 7. 발표할 내용

- 구현한 기능과 전체 처리 흐름: 경로 검사 → 파서 선택 → 텍스트 추출 → 파일별 임시 집계 → 성공한 파일만 합산 → 요약, 조회, 저장
- 분석 → 조회 → 저장 시연: basic.txt 분석 → 상위 2개 → JAVA! 조회 → 저장 후 Compare Files
- 해결한 문제: 실패한 파일은 버리고 성공한 파일만 합치는 실패 처리, 자동 수정이 실패 처리를 우회한 문제와 수정 전후 예외가 잡히는 위치
- 남은 문제와 개선하고 싶은 부분: 분석 상태와 집계 흐름 분리, 읽으면서 바로 집계, CSV 오류 메시지 한글화, 심화 7(DB 저장)