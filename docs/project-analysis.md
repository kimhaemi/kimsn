# radar_new 프로젝트 분석 및 작업 이력

> 이 문서는 소스 분석 결과와 이후 작업 이력을 계속 누적 기록합니다.
> 보안상 비밀번호·IP 계정 등 민감값은 이 문서에 기록하지 않습니다.

---

## 0. 처음 읽는 분을 위한 안내

### 0.1 이 문서는 무엇인가

이 프로젝트(`radar_new`)는 **기상 레이더 관측소가 자료(파일)를 제때 보내는지 감시하고, 문제가 생기면 담당자에게 문자를 보내는 시스템**이다. 2026년 11월, OpenJDK에서 심각한 보안 취약점(Java4Shell, CVE-2026-114938)이 공개되었고, 이를 계기로 아래 작업을 진행하고 있다.

- Java 실행 환경: **OpenJDK 11 → OpenJDK 25.0.5.1 이상** (취약점이 수정된 버전)
- 프레임워크: **Spring Boot 2.7.12 → 4.1.2** (Java 25를 쓰려면 함께 올려야 함)
- 그 과정에서 발견한 **보안 문제(S1~S8) 수정**

이 문서는 "무엇을, 왜, 어떻게 바꿨고, 어떻게 확인했는지"를 처음 보는 사람도 따라 할 수 있도록 기록한다.

### 0.2 지금 상태 한눈에 보기 (2026-11-19 기준)

| 단계 | 내용 | 상태 | 자세히 |
|---|---|---|---|
| 준비 | 개발 PC에 JDK 25.0.5.1, Gradle 9.9.1 준비 | ✅ 완료 | 0.5 |
| A | 빌드 설정(Gradle) 전환 | ✅ 완료 | 6.8-A |
| B | `javax` → `jakarta` 패키지 전환 | ✅ 완료 | 6.8-B |
| C | 로그인/보안 설정 재작성 + 인증 복구(S1) | ✅ 코드·자동 테스트 완료 / ⏳ 실제 DB로 화면 확인 필요 | 6.8-C, 8.2 |
| D | DB 쿼리(JPA) 동작 검증 | ⏳ **DB 필요** — 미진행 | 8.3 |
| E | 설정 파일(yml) 정리 | ✅ 완료 | 6.8-E |
| F | 운영 실행 스크립트 JDK 25 적용 | ✅ 완료 | 6.8-F |
| K | 사용자 관리(`/users/**`) ADMIN 전용 권한 | ✅ 코드·자동 테스트 완료 / ⏳ 실제 화면 확인 필요 | 6.8-K, 8.2 |
| 보안 | S1, S5, S6, S7 해결 / S2~S4, S8 남음 | 일부 완료 | 4.1 |
| 배포 | 운영 서버 JDK 25 설치 → 순차 교체 | ⏳ 미진행 | 8.5 |

> **현재 브랜치 `ver_java25`에만 반영되어 있고, 원격(origin)에는 아직 push하지 않았다.**

### 0.3 읽는 순서

1. **전체 그림**: 1장(개요) → 2장(폴더 구조) → 3장(동작 흐름)
2. **왜 바꾸나**: 4장(발견된 문제) → 5장(라이브러리 취약점) → 6.1(Java4Shell)
3. **무엇을 바꿨나**: 6.8(단계별 변경 내역과 테스트 방법)
4. **직접 해 보기**: 0.5(환경 준비) → 0.6(빌드·테스트)
5. **앞으로 할 일**: 8장(남은 작업 따라하기)
6. **경과 기록**: 7장(작업 이력), 0.7(commit 목록)
7. **내가 직접 검증하기**: 9장(검토자 확인·검증 가이드) — 변경 내용을 직접 확인하고 결과를 기록하는 방법
8. **주석 읽는 법**: 0.8(누가 쓴 주석인지 구분하는 규칙)

### 0.4 용어 설명

| 용어 | 뜻 (이 프로젝트에서) |
|---|---|
| JDK / OpenJDK | Java 프로그램을 만들고(컴파일) 실행하는 도구 모음. `java`(실행), `javac`(컴파일) 명령이 들어 있다 |
| LTS | 장기 지원 버전. Java 25는 LTS라 보안 패치를 오래 받을 수 있다 (Java 27은 단기 버전) |
| CVE | 공개된 보안 취약점에 붙는 고유 번호. 예: CVE-2026-114938 |
| Gradle | 소스 컴파일, 라이브러리 다운로드, jar 생성을 자동으로 해 주는 빌드 도구 |
| Gradle wrapper (`gradlew`) | 프로젝트에 정해진 Gradle 버전을 자동으로 내려받아 실행하는 스크립트. **Gradle을 따로 설치하지 않아도 된다** |
| Spring Boot | 이 프로젝트가 사용하는 Java 웹/서버 프레임워크 |
| BOM | Spring Boot가 "서로 호환되는 라이브러리 버전 목록"을 정해 둔 것. 그래서 `build.gradle`에 버전을 일일이 적지 않는다 |
| `javax` → `jakarta` | Java EE가 Jakarta EE로 이름이 바뀌면서 패키지 이름도 바뀜. Spring Boot 3 이상은 `jakarta.*`만 지원 |
| JPA / Hibernate | Java 객체와 DB 테이블을 연결하는 기술. 이 프로젝트는 대부분 직접 쓴 SQL(native query)을 사용 |
| Spring Security | 로그인, 접근 권한, CSRF 방어를 담당하는 라이브러리 |
| CSRF | 사용자가 로그인한 상태를 악용해 다른 사이트가 몰래 요청을 보내는 공격. 막기 위해 서버가 발급한 **토큰**을 요청마다 함께 보내야 한다 |
| MockMvc / @WebMvcTest | 실제 서버를 띄우지 않고 웹 요청을 흉내 내서 테스트하는 방법. DB 없이 보안 설정을 검증할 때 사용 |
| SFTP / JSch | 관측소 서버에 접속해 파일을 확인하는 방식(SFTP)과 그 Java 라이브러리(JSch) |
| 누리2(Nuri2) | 문자 발송 에이전트. DB 테이블(`NURI2_NRMSG_DATA`)에 행을 넣으면 문자가 발송된다 |
| Daemon 5종 | 레이더 종류·기관별 감시 프로그램: RDR(기상청A 대형), SDR(기상청A 소형), TDWR(공항), RDR_B(기상청B 대형), SDR_B(기상청B 소형) |
| commit 해시 | Git 변경 기록의 고유 번호(예: `ade302c`). `git show ade302c`로 내용을 볼 수 있다 |

### 0.5 개발 환경 준비 (따라하기)

아래는 Ubuntu(WSL 포함) 기준이다. 실제로 이 순서로 개발 PC를 준비했다.

**① 소스 받기**
```bash
git clone https://github.com/kimhaemi/kimsn.git
cd kimsn
git checkout ver_java25        # 업그레이드 작업 브랜치
```

**② JDK 25 설치** (관리자 비밀번호 필요 — 반드시 일반 터미널 창에서 실행)
```bash
sudo apt-get update
sudo apt-get install -y openjdk-25-jdk-headless
```
- `headless`는 화면(GUI) 라이브러리만 뺀 버전이며 `javac`도 포함되어 빌드에 충분하다
- **확인 1 — 버전**: `java -version` → 첫 줄이 `openjdk version "25.0.5.1"` 이상이어야 한다
- **확인 2 — Java4Shell 패치 포함 여부**: 버전 번호만으로 판단하지 말고 패키지 변경 이력을 본다
  ```bash
  apt-get changelog openjdk-25-jdk-headless | grep CVE-2026-114938
  ```
  `+ CVE-2026-114938`이 출력되면 패치가 포함된 것이다 (2026-11-19 확인: `25.0.5.1+2-1~26.04.1`에 포함)
- **확인 3 — CPU 영향 여부**(참고): `grep -o -m1 avx512vbmi /proc/cpuinfo` 가 출력되면 Java4Shell 영향을 받는 CPU다. 이 개발 PC는 해당됨

**③ (선택) JAVA_HOME 설정** — IDE나 다른 도구가 JDK를 찾기 쉽게
```bash
echo 'export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64' >> ~/.bashrc
source ~/.bashrc
```

**④ Gradle** — **따로 설치할 필요 없다.** 프로젝트의 `./gradlew`가 Gradle 9.9.1을 자동으로 내려받는다 (`~/.gradle/wrapper/dists/`에 저장, 최초 1회 인터넷 필요). 내려받은 파일은 `gradle/wrapper/gradle-wrapper.properties`의 `distributionSha256Sum`으로 위·변조 여부를 검사한다.
```bash
./gradlew --version     # "Gradle 9.9.1", "Launcher JVM: 25.0.5.1" 이 보이면 정상
```
(참고: 작업자는 wrapper 파일을 새로 만들기 위해서만 `~/opt/gradle-9.9.1`에 Gradle을 설치했다. 다른 사람은 필요 없다.)

**⑤ Windows에서 직접 빌드할 경우**: JDK 25를 설치하고 `gradlew.bat build`를 실행한다. `.gitattributes`가 `gradlew.bat`을 Windows 줄바꿈(CRLF)으로 유지해 준다.

### 0.6 빌드와 테스트 실행 (따라하기)

**전체 빌드 (컴파일 + 자동 테스트 + jar 생성)**
```bash
./gradlew build
```
- 처음에는 라이브러리를 내려받느라 몇 분 걸릴 수 있다. 끝에 `BUILD SUCCESSFUL`이 나오면 성공
- 만들어지는 파일
  - 웹: `radar-web/build/libs/RADAR_WEB.jar`
  - 데몬: `Daemon/build/libs/RDR.jar`, `SDR.jar`, `TDWR.jar`, `RDR_B.jar`, `SDR_B.jar`
- 확인된 결과(2026-11-19): 성공, 자동 테스트 23개 모두 통과 (보안 C-01~C-14 + 권한 K-01~K-09)

**보안 자동 테스트만 실행**
```bash
./gradlew :radar-web:test
```
- 결과 보고서: `radar-web/build/reports/tests/test/index.html` (브라우저로 열기)
- DB 없이 실행된다. 테스트 내용은 6.8-C "자동 테스트" 참고

**설정 파일 검증 스크립트** (yml을 고친 뒤 확인용, 6.8-E 참고)
```bash
# 1) 정적 검사: yml 키가 Spring Boot 4에서 유효한지
python3 -I docs/verify/check_props.py Daemon/build/libs/RDR.jar Daemon/src/main/resources/application.yml
python3 -I docs/verify/check_props.py radar-web/build/libs/RADAR_WEB.jar radar-web/src/main/resources/application.yml
# 결과에 UNKNOWN / DEPRECATED 가 없으면 정상 (Web의 spring.devtools.* 1건은 정상)

# 2) 설정 로딩 테스트: 운영과 같은 라이브러리로 yml을 실제로 읽어 봄
mkdir -p /tmp/rdr && (cd /tmp/rdr && jar xf "$OLDPWD/Daemon/build/libs/RDR.jar")
java -cp "/tmp/rdr/BOOT-INF/classes:/tmp/rdr/BOOT-INF/lib/*" docs/verify/ConfigLoadTest.java "$PWD"
# 마지막 줄 "결과: PASS 53 / FAIL 0" 이면 정상
```

**자주 만나는 오류와 해결**

| 오류 메시지 | 원인 | 해결 |
|---|---|---|
| `org/gradle/api/plugins/JavaPluginConvention` | Gradle 9에서 옛 Spring Boot 2.7 플러그인을 사용 | 이미 4.1.2로 수정됨. 옛 브랜치를 빌드하는 경우에 발생 |
| `Unsupported class file major version 69` | Java 25로 만든 파일을 옛 도구(구버전 IDE 플러그인 등)가 읽음 | IDE/플러그인을 Java 25 지원 버전으로 업데이트 |
| `package javax.persistence does not exist` | `javax` import가 남아 있음 | `jakarta.persistence`로 변경 (6.8-B) |
| `Communications link failure` | DB에 접속할 수 없음 | DB 주소·방화벽 확인. DB 없는 환경에서 실행하면 정상적으로 발생 |
| `cannot use classpath wildcards` | `spring.config.import`에 `classpath*:` 사용 | `classpath:`로 변경 (6.8-E에서 수정 완료) |
| `did not discover any tests` | 테스트 실행기(JUnit 5) 설정 누락 | 루트 `build.gradle`의 `useJUnitPlatform()` (수정 완료) |
| 브라우저에서 저장/삭제 시 **403** | 요청에 CSRF 토큰이 없음 | 해당 화면이 `common.js`를 불러오는지, `<meta name="_csrf">`가 있는지 확인 (6.8-C) |
| `[ERROR] Java 버전(...)이 최소 요구 버전(25.0.5.1)보다 낮거나...` | 운영 스크립트의 버전 검사 | JDK 25.0.5.1 이상 설치 후 `JAVA_HOME` 지정 (6.8-F) |
| `check_java: command not found` | 스크립트가 옛 버전/일부만 반영 | `01cf88b` 이후 버전의 스크립트 사용 |

### 0.7 이번 작업의 commit 목록 (브랜치 `ver_java25`, 오래된 순)

각 commit은 `git show <해시>`로 변경 내용을 볼 수 있다. 문제가 생기면 `git revert <해시>`로 해당 변경만 되돌릴 수 있다.

| commit | 단계 | 내용 |
|---|---|---|
| `ade302c` | A | Spring Boot 4.1.2 / Java 25 / Gradle 9.9.1 빌드 설정 전환, wrapper 추가 |
| `3f363cf` | 문서 | 분석·계획 문서 추가 |
| `da91928` | B | common-data `javax` → `jakarta` |
| `a0543da` | B | Daemon `javax` → `jakarta` + Boot 4 API 변경 반영 |
| `a3b58b1` | B | radar-web `javax` → `jakarta` |
| `0d98238` | 문서 | B 단계 결과 |
| `b75f60c` | C | SecurityConfig 재작성, 인증 복구(S1), CSRF 활성화 |
| `b7e0a7d` | C | 화면에서 CSRF 토큰 전달 (common.js, 로그인, 로그아웃) |
| `47d1b27` | A 보완 | Daemon jar 5종 생성 오류 수정 (`targetJavaVersion`) |
| `03f0656` | 문서 | C 단계 결과 |
| `1f1ae08` | E | yml 설정 정리 (Daemon 기동 실패 오류 수정 포함) |
| `e8ad9e8` | 문서 | E 단계 테스트 방법, 검증 스크립트 |
| `83e3e1a` | F | 운영 스크립트 JDK 25 경로·버전 검사 |
| `01cf88b` | F 보완 | Web 스크립트 누락 부분 수정 |
| `3ad5473` | 보안 | SFTP 비밀번호 로그 기록 제거(S5) |
| `e5d048f` | C 검증 | DB 없이 실행되는 보안 자동 테스트 14개 |
| `7bb787f` | 빌드 | `.gitattributes` (줄바꿈 규칙) |
| `779cdc6` | 문서 | 0장·8장 추가 |
| `ab480ac` | 주석 | Claude 작성 주석에 `[Claude 2026-11-19]` 표시, 기존 주석 원문 복원 (실행 코드 변경 없음) |
| `1aa2ce5` | 문서·검증 | 0.8 주석 구분 규칙, 9장 검토자 검증 가이드, 검증 스크립트 3개(`test_process_scripts.sh`, `JschDefaults.java`, `check_comments.py`) |
| `f277315` | 주석 | Claude 주석 표시를 `/* Claude 주석 (2026-11-19): ... */` 형식으로 통일 (실행 코드 변경 없음) |
| `8e233cc` | K | 사용자 관리(`/users/**`) ADMIN 전용 권한, 403 화면, 권한 테스트 9개 |

### 0.8 주석 구분 규칙 — 누가 쓴 주석인가

Claude(AI 작업자)가 소스에 추가한 주석은 **기존 작성자의 주석과 한눈에 구분되도록** 모두 `/* Claude 주석 ... */` 모양으로 표시한다 (2026-11-19 사용자 요청으로 `f277315`에서 이 형식으로 통일. 그 전에는 `[Claude 2026-11-19]` 표시를 썼음).

| 파일 종류 / 형태 | 표시 예시 | 의미 |
|---|---|---|
| Java·Gradle·JS 한 줄 | `/* Claude 주석 (2026-11-19): 3.14.0 → BOM 관리 (3.20.0) */` | 이 주석은 Claude 작성 |
| Java·Gradle·JS 여러 줄 | `/* Claude 주석 (2026-11-19): 첫 줄` … `마지막 줄 */` | `/*`부터 `*/`까지 하나의 Claude 주석 |
| 코드 줄 끝 | `mainClass = '...' /* Claude 주석 (2026-11-19): 존재하지 않던 ... 수정 */` | 이 줄의 코드가 Claude에 의해 변경됨 |
| yml·sh·.gitattributes (`#` 주석만 가능) | `# /* Claude 주석 (2026-11-19): ... */` | 위와 같음 (`#` 뒤에 같은 모양) |
| HTML | `<!-- /* Claude 주석 (2026-11-19): ... */ -->` | 위와 같음 |
| 범위 블록 | `/* Claude 주석 (2026-11-19) 시작: ... */` ~ `/* Claude 주석 (2026-11-19) 끝: ... */` | 시작~끝 사이(코드 포함) 전체가 Claude 추가분 (운영 스크립트의 Java 실행 환경 블록) |
| 기존 코드 보존 블록 | `/* Claude 주석 (2026-11-19) 시작: 기존 코드 원문 보존 ... */` ~ `끝` | 블록 안은 **기존 코드와 기존 주석의 원문**. 새 버전에서 쓰지 않게 되어 주석 처리만 함 (`SecurityConfig.java`) |
| 새 파일 | 파일 맨 위 `/* Claude 주석 (2026-11-19): 새로 작성한 파일 ...` | 파일 전체(안의 주석 포함)가 Claude 작성 |

- **`Claude 주석`이라는 글자가 없는 주석은 모두 기존 작성자의 주석**이다. 📌, 💡 같은 이모지 주석은 기존 작성자 스타일이며 Claude 주석에는 쓰지 않는다
- 작업 중 실수로 바뀌거나 지워졌던 기존 주석(주석 처리된 import의 `javax`→`jakarta` 치환, SecurityConfig·build.gradle·운영 스크립트 주석 등)은 `ab480ac`에서 **원문 그대로 복원**했다
- 기존 주석이 붙어 있던 코드가 바뀐 경우, 기존 주석은 그대로 두고 바로 아래에 `/* Claude 주석 (...): 위 주석은 기존 ... 설명 */`을 덧붙였다. 예: `build.gradle`의 `version '4.1.2' apply false // Java 11 지원하는 안정 버전`(기존 주석) + 다음 줄 Claude 설명
- **찾는 방법**
  ```bash
  git grep -n "Claude 주석"                      # Claude 주석 전체 목록
  python3 -I docs/verify/check_comments.py       # 표시 누락·기존 주석 삭제 여부 자동 검사 (9.3.9)
  ```
- 앞으로 작업할 때도 같은 형식을 쓴다 (날짜만 작업일로 변경)

---

## 1. 개요

- **목적**: 기상 레이더 관측소 자료 수신 감시 + 장애/복구 SMS 알림 시스템
- **구성**: Gradle 멀티모듈 3개 (`common-data`, `Daemon`, `radar-web`)
- **규모**: Java 소스 105개, 약 8,200 라인
- **기술 스택**: Java 11 (`sourceCompatibility = '11'`), Spring Boot 2.7.12, Spring Data JPA, Spring Security, Thymeleaf, Log4j2, JSch(SFTP), MySQL
- **Git 브랜치**: `ver_java25` (원격: master, dev, backup, radar_new, ver_java25) — 브랜치명과 달리 빌드 설정은 아직 Java 11

## 2. 디렉터리 구조

```
radar_new/
├─ common-data/   공통 데이터 계층 (일반 jar)
│   ├─ config/     DataSourceConfig (Hikari, pool=1), YamlPropertySourceFactory
│   ├─ dto/        JPA 엔티티 역할 (*Dto) + 복합키 (pkColumn/)
│   ├─ repository/ Spring Data JPA 22개 (SmsSendRepository 592줄 최대)
│   └─ resources/common-data.yml   DB 접속 정보
│
├─ Daemon/        감시 데몬 (jar 5종: RDR, SDR, TDWR, RDR_B, SDR_B)
│   ├─ DaemonApplication   Manifest의 Radar-System-Type → 로그 파일명 분기
│   ├─ Scheduler           동적 크론. 관측소별 스레드로 StepOne → PauseTime(20s) 대기 → StepTwo
│   ├─ service/
│   │   ├─ StepOneService  SFTP(JSch) 접속 → 파일 존재/크기 확인 → receive_data INSERT
│   │   ├─ StepTwoService  연속 장애/복구 판정 → receive_condition 갱신
│   │   ├─ SmsService      장애/복구 문자 → 누리2(NURI2_NRMSG_DATA) 큐 INSERT
│   │   └─ QueryService    Repository 호출 창구
│   ├─ config/
│   │   ├─ YmlRefreshManager     외부 application.yml WatchService 감시 → 무중단 반영
│   │   └─ RadarConfigProperties 외부 yml 직접 파싱 (SnakeYAML)
│   └─ util/ SftpUtil, TimeUtil
│
├─ radar-web/     관리 웹 (Thymeleaf + Spring Security, RADAR_WEB.jar)
│   ├─ controller/  화면: /station, /stat, /manage/*, SMS 그룹·대상자, /users
│   ├─ restApi/     PostController: 설정 변경·회원 CRUD·문자 발송 등 POST 21개, GetController
│   └─ config/      SecurityConfig, WebMvcConfig, InterceptorConfig, auth/
│
├─ config/   운영 설정: 외부 application*.yml (관측소별 SFTP 접속 정보), *_process.sh 실행 스크립트
├─ Query/    운영 SQL, DB 전체 덤프 7개
├─ note/     누리2 SDK 압축본/문서, 참고 SQL
└─ libs/     로컬 jar (flatDir 저장소)
```

## 3. 동작 흐름 (Daemon)

1. 기동 옵션 `-DDAEMON_TYPE` → 기관(KMA / 이름에 MCEE 포함 시 MCEE) 및 레이더 구분(1 대형 / 2 소형 / 3 공항) 결정
2. 외부 yml `radar.config.paths.<TYPE>.cron-expression` 주기로 실행 (기본값 `30 * * * * *`)
3. **StepOne**: DB에서 관측소 목록 조회 → 관측소별 스레드(0.5초 간격 기동)가 SFTP로 해당 시각 파일·크기 확인 → 수신 이력 기록
4. **StepTwo**: 경고 기준(receive_condition_criteria)에 따라 장애/복구 판정 → 필요 시 SmsService로 문자 큐 적재

외부 설정 파일 탐색 순서: `/home/watcher/deamon/config/application.yml` → `../config/` → `./config/` → `./Daemon/config/`

## 4. 발견된 문제점

### 4.1 보안 (긴급)

| # | 등급 | 내용 | 위치 |
|---|---|---|---|
| S1 | ✅ 코드 해결 (`b75f60c`, 런타임 검증 대기) | 웹 인증 사실상 비활성화: `/**` 및 `anyRequest()` 모두 `permitAll()`, `authenticated()` 전부 주석. 인터셉터 등록도 주석. 비로그인으로 회원 생성/삭제·문자 발송·설정 변경 가능. CSRF도 disable | `radar-web/.../config/SecurityConfig.java`, `WebMvcConfig.java:16` |
| S2 | 🔴 | 관측소 root 계정 등 SFTP 비밀번호가 평문으로 Git 커밋됨 (파일당 약 35건), 동일 비밀번호 다수 관측소 공유 | `config/application*.yml` (4개) |
| S3 | 🔴 | DB 접속 비밀번호 평문 Git 커밋 | `common-data/src/main/resources/common-data.yml`, `Query/DB 접속 정보.sql`, `note/그룹 감시자료 설정.sql` 1~2행(2026-11-19 추가 발견) |
| S4 | 🔴 | GitLab 복구 코드, DB 전체 덤프 Git 커밋 | `note/gitlab-recovery-codes.txt`, `Query/dump/` |
| S5 | ✅ 해결 (`3ad5473`) | SFTP 비밀번호를 매 주기 로그에 기록 → 이제 `******`/`(미설정)`만 기록. **이미 쌓인 운영 로그 파일에는 비밀번호가 남아 있으므로 삭제·비밀번호 교체 필요** | `Daemon/.../service/StepOneService.java:105` |
| S6 | ✅ 해결 (`ade302c`) | `spring-boot-devtools`가 `implementation`으로 운영 jar에 포함 | `radar-web/build.gradle` |
| S7 | ✅ 해결 (`ade302c`) | `common-data/build.gradle`에 HTTP 저장소(`http://apache.org`, `allowInsecureProtocol`) 설정 | `common-data/build.gradle` |
| S8 | 🟡 (2026-11-19 추가) | SFTP 접속 시 서버 신원 확인 생략(`StrictHostKeyChecking=no`) → 중간자 공격으로 가짜 서버에 비밀번호가 전달될 수 있음. 해결하려면 운영 서버에 관측소별 `known_hosts`를 준비해야 하므로 서버 작업과 함께 진행 | `Daemon/.../util/SftpUtil.java:35` |

> S2~S4는 Git 이력에 남아 있으므로 파일 삭제만으로 부족 → **비밀번호/복구코드 교체 우선**, 이후 이력 정리(git filter-repo 등) 검토.

### 4.2 구조/코드

- Daemon `bootJar_*` 태스크: `mainClass = RadarDaemonApplication`(존재하지 않음) vs `Start-Class = DaemonApplication` 불일치
- 외부 설정 경로 하드코딩 (`/home/watcher/deamon/...`, `deamon` 오타)
- 주기마다 `newCachedThreadPool()` 신규 생성, `awaitTermination` 없이 고정 대기 후 StepTwo 진행 → SFTP 지연 시 StepOne 완료 전 판정 가능
- 백업 파일 혼재: `ConfigFileEnum_back.java`, `SmsService.java.back`, `config/application copy.yml`
- `jsch` 이중 존재: `com.jcraft:jsch:0.1.55`(build.gradle 사용, 유지보수 중단) / `libs/jsch-0.2.20.jar`(mwiede 포크) → `ade302c`에서 mwiede 2.28.8로 통일
- ⚠️ **(2026-11-19 추가) jsch 교체로 오래된 SSH 서버 접속이 실패할 수 있음** → 6.8-J, 8.4 참고
- 운영 스크립트의 `stop` 명령은 부모 프로세스 ID가 1인 프로세스만 종료함(`awk '{if(1 == $3) ...}'`). RHEL 서버에서는 정상이지만 다른 환경(WSL 등)에서는 종료되지 않음 (6.8-F 테스트에서 확인, 기존 동작이라 변경하지 않음)
- 운영 스크립트 `start all` 분기의 실행 여부 검사가 `grep "RADAR_TITLE"`(변수 `$` 누락)이라 항상 "실행 중 아님"으로 판단 → `start all` 사용 시 중복 실행 가능 (기존 버그, 변경하지 않음. `start`만 사용 권장)
- 로그인 실패 메시지가 화면에 표시되지 않음: `AuthenticationFailure`는 세션에 저장, `LoginController`는 request attribute에서 읽음 (기존 버그)
- `LoginController`의 `POST /logout` 메서드는 Spring Security가 먼저 처리하므로 호출되지 않음 (정리 대상)
- `RadarConfigProperties.getDynamicPath()`는 어디서도 호출되지 않는 코드 (정리 대상)

## 5. 의존성 취약점 점검 요약 (2026-11-19, OSV.dev 기준)

- 방법: Spring Boot 2.7.12 BOM 관리 버전 기준으로 OSV.dev 조회 (로컬에 Java/Gradle 없어 실제 의존성 트리 미확인)
- 결과: 고유 CVE 약 123건 (Critical 16 / High 45 / Moderate 52 / Low 11)

| 라이브러리 (현재) | 주요 이슈 | 조치 버전 |
|---|---|---|
| Tomcat embed 9.0.75 | 42건 (Critical 9, CVE-2025-24813 RCE 등) | 9.0.121 |
| Thymeleaf 3.0.15 | Critical 3건 (표현식 샌드박스 우회) | 3.1.5 |
| Spring Security 5.7.8 / crypto 5.7.12 | CVE-2023-34034 등 | 5.7.16+ (OSS 지원 종료) |
| Spring Framework 5.3.27 | 30건+, 다수 6.2.x에서만 수정 | Java 17 + Boot 3.x 필요 |
| SnakeYAML 1.30 | CVE-2022-1471 (RCE) | 2.x |
| Jackson 2.13.5 | 14건 | 2.18.11 |
| mysql-connector-j 8.0.33 | CVE-2023-22102 | 9.2.0 |
| org.json 20230618 | CVE-2023-5072 | 20260719 |
| Log4j2 2.17.2 | Moderate 5건 | 2.25.5 |
| commons-lang3 3.14.0 | CVE-2025-48924 | 3.18.0 |

- 근본 해결: **Java 17/21 + Spring Boot 3.5.x 이상** 이전 (javax → jakarta 전환 포함)
- Java SE 11 런타임: 11.0.33+10 미만 버전은 2026년 10월 CPU 및 후속 Security Alert(CVE-2026-91403, CVE-2026-114820) 대상
- 미점검: `nuri2.jar`, `nuri2_ojdbc23.jar` (비공개 라이브러리)

---

## 6. OpenJDK 업그레이드 계획 (Java 11 → 25 LTS)

### 6.1 배경: CVE-2026-114938 "Java4Shell" (2026-11-17 공개)

- **내용**: OpenJDK의 최적화 Base64 디코더에서 메모리가 손상되는 문제. 인증 없이 원격 코드 실행(RCE)이 가능하고, CVSS 4.0 기준 9.2점(Critical)
- **영향 범위**: OpenJDK 17.0.6 이상이면서 **AVX-512 VBMI를 지원하는 x86-64 CPU**. ARM64는 해당 없음
- **수정 버전**: 17.0.21.1 / 21.0.13.1 / **25.0.5.1** / 27.0.1.1. Microsoft 빌드는 11.0.33.1도 배포됨
- **Java 11 영향 여부**: 공개 분석은 "17.0.6 이상"으로 설명하지만 Microsoft는 11.0.33.1을 배포함 → 사용하는 배포판의 공지로 확인 필요
- **주의**: 2026-11-17 기준으로 일부 배포판(OS 저장소 등)에는 아직 반영되지 않음. 버전 번호만 보고 판단하지 말고, 배포판 공지에서 CVE-2026-114938 수정 포함 여부를 확인할 것

### 6.2 목표 버전

| 항목 | 현재 | 목표 | 비고 |
|---|---|---|---|
| JDK | OpenJDK 11 | **OpenJDK 25.0.5.1 이상 (LTS)** | 27은 LTS가 아닌 단기 버전. 브랜치명 `ver_java25`와도 일치 |
| Gradle | 버전 미확인 (wrapper jar 없음, gradlew.bat만 있음) | **9.x** (현재 9.9.1) | Java 25를 실행하려면 Gradle 9.1 이상 필요 |
| Spring Boot | 2.7.12 | **4.1.x** (현재 4.1.2) | 대안: 3.5.x (현재 3.5.16). 변경은 적지만 지원 종료가 빠름 |
| Spring Framework / Security | 5.3.27 / 5.7.8 | 7.x / 7.x | Boot가 관리 |
| Hibernate | 5.6.15 | 7.x | Boot가 관리 |
| Thymeleaf | 3.0.15 | 3.1.x | Boot가 관리. Critical CVE도 해소 |
| Lombok | 1.18.26 | **1.18.48** | 1.18.26은 Java 25에서 컴파일 실패 |
| JSch | com.jcraft 0.1.55 | **com.github.mwiede:jsch 2.28.x** | 패키지명이 같아 코드 변경 거의 없음 |
| mysql-connector-j | 8.0.33 | Boot가 관리 (9.x) | |
| org.json | 20230618 | 20260719 이상 | |
| modelmapper | 2.3.9 | 3.2.x 이상 | 사용처 확인 후 제거도 검토 |

> Spring Boot 2.7이나 Spring 5.3, 기존 Lombok·Byte Buddy로는 Java 25 클래스 파일을 다룰 수 없다. 따라서 **JDK만 교체하는 것은 불가능하고**, 프레임워크 업그레이드가 반드시 함께 필요하다.

### 6.3 소스 수정 범위

#### A. 빌드 설정 (Gradle)

| 파일 | 수정 내용 |
|---|---|
| `gradle/wrapper/*`, `gradlew` | wrapper를 Gradle 9.x로 다시 생성 (현재 `.gitignore`가 `gradle/`, `gradlew`를 제외하고 있으므로 같이 정리) |
| `build.gradle` (루트) | 플러그인: `org.springframework.boot` 4.1.x, `io.spring.dependency-management` 1.1.x |
| | `sourceCompatibility = '11'` 제거 → `java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }` (Gradle 9에서는 project 수준 `sourceCompatibility` 속성이 제거됨) |
| | 직접 지정한 버전 제거: `spring-boot-starter-log4j2:2.7.12`, `spring-boot-starter-test:2.7.12`, `spring-security-crypto:5.7.12` → Boot BOM이 관리하도록 |
| | `commons-lang3` 3.18 이상, `modelmapper` 상향, `lombok` 1.18.48 |
| | 의미 없는 저장소 제거: `https://maven.org`, `https://gradle.org` |
| `common-data/build.gradle` | HTTP 저장소(`http://apache.org`, `allowInsecureProtocol`) 제거, jsch를 mwiede 2.28.x로 교체 |
| `Daemon/build.gradle` | `spring-boot-starter-batch` 제거 (소스에서 Batch를 사용하지 않음), `org.json` 상향, jsch 교체 |
| | `bootJarTaskType = tasks.bootJar.getClass()` 방식 → `org.springframework.boot.gradle.tasks.bundling.BootJar` 직접 사용 |
| | `mainClass = 'kr.or.kimsn.radar.daemon.RadarDaemonApplication'` → `DaemonApplication`으로 수정 (현재 존재하지 않는 클래스) |
| `radar-web/build.gradle` | Boot 4 스타터 이름 변경 반영: `spring-boot-starter-web` → `spring-boot-starter-webmvc` |
| | `spring-boot-devtools` → `developmentOnly` |
| | 템플릿 4개가 `sec:` 네임스페이스를 쓰므로 `thymeleaf-extras-springsecurity6` 추가 (현재 의존성이 없어 동작하지 않음) |
| `libs/` | `spring-*-2.7.12.jar`, `jsch-0.1.55.jar`, `spring-boot-dependencies-2.7.12.pom` 등 구버전 jar 정리. `nuri2*.jar`만 남기기 |

#### B. Java 소스: `javax` → `jakarta` 전환 (56개 파일)

| 기존 패키지 | 사용 수 | 변경 | 주요 위치 |
|---|---|---|---|
| `javax.persistence.*` | 125 | `jakarta.persistence.*` | common-data `dto/`, `pkColumn/`, `repository/` |
| `javax.servlet.*` | 26 | `jakarta.servlet.*` | radar-web controller, auth, InterceptorConfig |
| `javax.transaction.*` | 9 | `org.springframework.transaction.annotation.Transactional` 권장 (또는 `jakarta.transaction`) | 서비스, Repository |
| `javax.annotation.PostConstruct/PreDestroy` | 2 | `jakarta.annotation.*` | Daemon `YmlRefreshManager` |
| `javax.sql.DataSource` | 1 | **변경 없음** (JDK 표준) | `DataSourceConfig` |

#### C. Spring Security 7 (`radar-web/.../config/SecurityConfig.java`)

- `.and()`, `authorizeRequests()`, `antMatchers()`, `csrf().disable()`가 제거됨 → 람다 DSL과 `authorizeHttpRequests(auth -> auth.requestMatchers(...))`로 다시 작성
- `http.getSharedObject(AuthenticationManagerBuilder.class)` 방식 → `UserDetailsService`와 `PasswordEncoder`를 Bean으로 등록하면 자동으로 구성됨
- **보안 이슈 S1을 함께 해결**: `/login`과 정적 리소스만 `permitAll`, 나머지는 `authenticated()`. CSRF를 다시 켜고, Thymeleaf form과 Ajax 요청에 CSRF 토큰 추가
- `auth/PrincipalDetails`, `UserSecurityService`, `AuthenticationSuccess/Failure`의 인터페이스 변경 여부 확인

#### D. JPA / Hibernate 7

- `@Query` 43개가 모두 `nativeQuery = true` → HQL 문법 변경 영향은 작음
- 네이티브 쿼리의 반환 타입 매핑(`Integer`/`Long`/`BigInteger`, interface projection) 확인 필요. Hibernate 6 이상에서는 반환 타입 변환이 달라짐
- `@IdClass`/`@EmbeddedId` 15곳: 복합키 클래스의 `equals`/`hashCode`, `Serializable` 확인
- `@GeneratedValue(IDENTITY)` (MembersDto, MenuDto): 그대로 사용 가능
- MySQL dialect를 지정하고 있다면 제거 (자동 감지)

#### E. 설정 파일 (yml)

- Boot 4에서 이름이 바뀐 속성 확인: `spring-boot-properties-migrator`를 한 번 runtime에 추가해 경고 확인
- `devtools` 설정(Daemon, radar-web `application.yml`)을 운영 프로파일에서 제외
- `log4j2.yml`: Log4j 2.25.x에서 YAML 설정을 쓰려면 `jackson-dataformat-yaml`이 필요함 (Boot 4는 Jackson 3) → 동작 확인. 안 되면 `log4j2.xml`로 변환
- Daemon `RadarConfigProperties`: SnakeYAML 2.x는 `new Yaml()` 생성 방식은 같지만 기본 `LoaderOptions`가 엄격해짐 → 외부 yml 로딩 확인

#### F. 운영 스크립트 (`config/*_process.sh` 7개)

- `java` 실행 경로를 JDK 25로 고정: `JAVA_HOME=/usr/lib/jvm/<jdk-25.0.5.1>`, `$JAVA_HOME/bin/java`
- 주석으로 남은 Java 8 경로 정리
- 필요하면 JVM 옵션 정리 (Java 25에서 제거된 옵션이 있는지 확인)

#### G. 그 밖의 확인 사항

- Jackson: 코드에서 쓰는 것은 `@JsonInclude`, `@JsonValue` 어노테이션뿐이고 Jackson 3에서도 패키지가 같음 → 영향 작음
- `json-simple` (1.1.1, 2012년 이후 유지보수 중단): 사용처 1곳 → Jackson이나 org.json으로 교체 검토
- 주기마다 `Executors.newCachedThreadPool()`을 새로 만드는 부분: Java 21 이상의 가상 스레드나 고정 풀로 개선 검토 (필수 아님)

### 6.4 진행 순서 (권장)

1. **운영 서버 확인**: CPU의 AVX-512 VBMI 지원 여부(`grep -o avx512vbmi /proc/cpuinfo`), 현재 JDK의 정확한 배포판과 빌드
2. **응급 조치 (선택)**: 업그레이드가 끝나기 전까지는 현재 JDK 11 배포판이 공지한 패치 빌드(예: 11.0.33.1)를 적용
3. **개발 환경**: JDK 25.0.5.1과 Gradle 9.x 설치, wrapper 다시 생성
4. **빌드 전환 (A)** → **javax에서 jakarta로 (B)** → **Security (C)** → **JPA (D)** → **설정 (E)** 순서로 단계별 컴파일
5. **기능 검증**: 로그인/권한, 관측소별 SFTP 감시(StepOne), 장애 판정(StepTwo), 누리2 문자 적재, 통계 화면
6. **배포**: 운영 서버에 JDK 25 설치 → 스크립트(F) 반영 → Daemon 5종과 Web을 순차 교체. 기존 JDK 11은 롤백용으로 보관

### 6.5 예상 작업량

| 구분 | 파일 수 | 난이도 |
|---|---|---|
| Gradle 설정 | 4개 + wrapper | 중 (Gradle 9와 Boot 4 문법) |
| javax → jakarta | 약 56개 | 하 (일괄 치환) |
| SecurityConfig와 인증 | 약 5개 | 중 (S1 해결 포함) |
| JPA 네이티브 쿼리 검증 | Repository 22개 | 중 (실행 테스트 필요) |
| 템플릿 (CSRF, sec 네임스페이스) | 약 30개 확인 | 하~중 |
| 운영 스크립트 | 7개 | 하 |

### 6.6 진행 체크리스트

- [x] 개발 PC(WSL)에 OpenJDK 25.0.5.1+2 설치 (Ubuntu `openjdk-25-jdk-headless 25.0.5.1+2-1~26.04.1`, 변경 이력에 CVE-2026-114938 수정 명시). 설치 경로 `/usr/lib/jvm/java-25-openjdk-amd64`, `/usr/bin/java`로 PATH 등록됨
- [ ] (선택) `~/.bashrc`에 `JAVA_HOME` 설정 — 사용자 직접 진행
- [x] 개발 PC에 Gradle 9.9.1 설치 (`~/opt/gradle-9.9.1`, SHA-256 검증) / 프로젝트 wrapper 9.9.1 생성 (`gradlew`, `gradlew.bat`, `gradle/wrapper/`, `distributionSha256Sum` 포함)
- [ ] 운영 서버 CPU의 AVX-512 VBMI 지원 여부 확인 (참고: 개발 PC CPU는 **지원함** → 영향 대상)
- [ ] 운영 JDK 배포판과 정확한 빌드 확인 (`java -version`)
- [ ] (응급) 현재 JDK 11 배포판 패치 빌드 적용 여부 결정
- [ ] 노출된 비밀번호와 GitLab 복구 코드 교체 (S2~S4)
- [x] Spring Boot 목표 버전 확정 → **4.1.2** (2026-11-19 사용자 결정)
- [x] A. Gradle 설정 전환 (설정 단계와 의존성 다운로드 성공, 컴파일은 B 단계 필요)
- [x] B. javax → jakarta 전환 (common-data와 Daemon 컴파일 성공, radar-web은 C 단계 이후 컴파일 가능)
- [x] `.gitignore` 정리: `gradle/`, `gradlew` 제외 규칙 제거, wrapper를 Git에 포함
- [x] C. SecurityConfig 재작성 및 S1 해결 (코드와 빌드 완료)
- [x] C 자동 테스트: DB 없이 보안 규칙 14개 검증 (`e5d048f`, 14/14 통과, 대조군 확인)
- [ ] C 화면 확인: 실제 DB가 있는 환경에서 화면별 저장/삭제(Ajax)와 검색 폼 동작 확인 (8.2)
- [x] S5. SFTP 비밀번호 로그 제거 (`3ad5473`)
- [x] `.gitattributes` 추가 (`7bb787f`)
- [ ] SFTP 접속 호환성 확인: 관측소 서버가 새 jsch의 알고리즘을 지원하는지 (8.4)
- [x] K. 사용자 관리 ADMIN 전용 권한 (`8e233cc`, 테스트 23/23, 대조군 확인)
- [ ] K 화면 확인: USER/ADMIN 계정으로 사용자 관리 접근, 403 화면 표시 (8.2)
- [ ] 운영 DB에 ADMIN 계정이 1명 이상 있는지, `roles`가 NULL인 회원이 없는지 확인 (8.5)
- [ ] D. JPA 네이티브 쿼리 검증
- [x] E. 설정 파일 정리 (`1f1ae08`, 검증 완료 → 6.8-E)
- [ ] (D 단계에서) `spring.jpa.open-in-view` 실제 적용 여부 결정 (현재 잘못된 위치에 있어 효과 없음)
- [x] F. 운영 스크립트 수정 (`83e3e1a`, `01cf88b`)
- [ ] 운영 서버 JDK 25 설치 경로 확인 후 스크립트 기본값(`/usr/lib/jvm/java-25-openjdk`)과 맞는지 확인 (8.5)
- [ ] 기능 검증 및 순차 배포 (8.5)

### 6.7 참고 자료

- CVE-2026-114938 (Java4Shell):
  [Microsoft](https://devblogs.microsoft.com/java/java-openjdk-november-2026-critical-patch-update/) ·
  [Wiz](https://www.wiz.io/blog/cve-2026-114938-rce-java4shell-openjdk) ·
  [Qualys](https://blog.qualys.com/vulnerabilities-threat-research/2026/11/17/how-to-find-mitigate-and-patch-the-critical-java4shell-flaw-cve-2026-114938-now) ·
  [Endor Labs](https://www.endorlabs.com/learn/java4shell-unauthenticated-rce-reported-in-openjdk-17-cve-2026-114938) ·
  [Datadog](https://securitylabs.datadoghq.com/articles/java4shell-remote-code-execution-in-openjdk-cve-2026-114938/)
- Java SE 11 2026년 10월 Security Alert:
  [CVE-2026-91403](https://www.oracle.com/security-alerts/alert-cve-2026-91403.html) ·
  [CVE-2026-114820](https://www.oracle.com/security-alerts/alert-cve-2026-114820.html)
- 의존성 CVE 조회: [OSV.dev](https://osv.dev). 원본 결과는 세션 임시 폴더에 있었으므로 필요하면 다시 조회

### 6.8 단계별 변경 내역

#### A. 빌드 설정 (2026-11-19 완료)

| 파일 | 변경 |
|---|---|
| `build.gradle` | Boot 플러그인 2.7.12 → **4.1.2**, dependency-management 1.0.15 → **1.1.7** |
| | `sourceCompatibility/targetCompatibility = '11'` → `java { toolchain { languageVersion = 25 } }`, JavaCompile UTF-8 공통 지정 |
| | 저장소가 아닌 URL(`https://maven.org`, `https://gradle.org`) 제거 |
| | 하드코딩 버전 제거 (BOM 관리): starter-log4j2, starter-test, spring-security-crypto, commons-lang3 |
| | `junit-platform-launcher` testRuntimeOnly 추가 (Gradle 9 요구) |
| | `com.fasterxml.jackson.dataformat:jackson-dataformat-xml` → `tools.jackson.dataformat:jackson-dataformat-xml` (Boot 4는 Jackson 3 사용). yaml 모듈은 Log4j2(Jackson 2)용으로 유지 |
| | modelmapper 2.3.9 → 3.2.8 |
| `common-data/build.gradle` | HTTP 저장소 블록(`http://apache.org`, `allowInsecureProtocol`, 중복 flatDir) 제거 (S7 해결), jsch → `com.github.mwiede:jsch:2.28.8` |
| `Daemon/build.gradle` | `spring-boot-starter-batch` 제거 (사용하지 않음), org.json 20230618 → 20260814, jsch → mwiede 2.28.8 |
| | `bootJar_*` 태스크 타입: `tasks.bootJar.getClass()` → `org.springframework.boot.gradle.tasks.bundling.BootJar` |
| | `mainClass` `RadarDaemonApplication`(존재하지 않음) → `DaemonApplication` |
| `radar-web/build.gradle` | `spring-boot-starter-web` → `spring-boot-starter-webmvc` (Boot 4에서 이름 변경) |
| | devtools `implementation` → `developmentOnly` (S6 해결) |
| | `thymeleaf-extras-springsecurity6` 추가 → **주의**: 템플릿의 `sec:` 속성이 이제 실제로 동작하므로, 로그인 상태에 따라 화면 요소가 보이거나 숨겨질 수 있음 (C 단계 인증 복구와 함께 확인) |

commit: `ade302c`

다운로드 확인한 주요 버전: Spring 7.0.10, Security 7.1.2, Tomcat 11.0.27, Hibernate 7.4.13, Log4j 2.25.5, SnakeYAML 2.6, Thymeleaf 3.1.5, Jackson 3.1.7 (+ Jackson 2.21.7, Log4j2용), mysql-connector-j 9.7.0, jsch 2.28.8, Lombok 1.18.48

#### B. javax → jakarta 전환 (2026-11-19 완료)

| 모듈 | commit | 변경 | 컴파일 |
|---|---|---|---|
| common-data | `da91928` | 41개 파일: `javax.persistence` → `jakarta.persistence`, `javax.transaction` → `jakarta.transaction` (import만 변경) | ✅ 성공 |
| Daemon | `a0543da` | `YmlRefreshManager` (`jakarta.annotation`), `StepTwoService` (`jakarta.transaction`) | ✅ 성공 |
| | | **Boot 4 API 변경**: `DaemonApplication`의 `@EntityScan` 패키지 → `org.springframework.boot.persistence.autoconfigure` | |
| | | **Spring 6+ API 변경**: `Scheduler` Trigger 람다 `nextExecutionTime()`(Date) → `nextExecution()`(Instant) | |
| radar-web | `a3b58b1` | 13개 파일: `javax.servlet` → `jakarta.servlet` 등, `RadarWebApplication`의 `@EntityScan` 패키지 변경 | ⏳ `SecurityConfig` 오류만 남음 (C 단계) |

- 변경하지 않은 것: `javax.sql.DataSource`(JDK 표준), 주석 처리된 javax import 3곳
- `jakarta.transaction.Transactional`은 기존 동작을 그대로 유지하려고 1:1로 바꿨음. Spring `@Transactional`로 통일할지는 이후 검토
- 남은 경고: `SftpUtil.java:32`의 `Session.setPassword(String)` deprecation (mwiede jsch). 동작에는 영향 없음

#### C. Spring Security 7 및 S1 인증 복구 (2026-11-19 코드 완료, 런타임 검증 대기)

| commit | 파일 | 변경 |
|---|---|---|
| `b75f60c` | `SecurityConfig.java` | Security 7 람다 DSL로 재작성 (`authorizeHttpRequests`, `requestMatchers`) |
| | | **허용 경로**: `/login`, `/css/**`, `/js/**`, `/images/**`, `/fonts/**`, `/favicon.ico`, `/error`, `/common/error`. **그 외 전부 인증 필요** |
| | | CSRF 보호 활성화 (기존 `csrf().disable()` 제거) |
| | | 로그아웃: `POST /logout`, 세션 무효화, `JSESSIONID`/`userId` 쿠키 삭제, `/login?logout`으로 이동 |
| | | `AuthenticationManagerBuilder` 수동 구성 제거 (`UserSecurityService`와 `BCryptPasswordEncoder` Bean 자동 연결) |
| `b7e0a7d` | `layout/config.html` | `_csrf`, `_csrf_header`, `_csrf_parameter` meta 추가 (default_layout을 쓰는 모든 화면에 적용) |
| | `static/js/common/common.js` | jQuery `ajaxSend`로 모든 Ajax 요청 헤더에 토큰 추가 (`fnCall` POST 24곳, `default.js`의 `$.post` 포함). `method="post"` 폼(10개)에는 화면 로드 시 hidden 토큰 자동 삽입 |
| | `views/login.html` | `action` → `th:action="@{/loginProc}"` (Thymeleaf가 토큰 자동 삽입) |
| | `layout/header.html` | 로그아웃 GET 링크 → 숨김 POST 폼 제출 |
| `47d1b27` | `Daemon/build.gradle` | (A 단계 보완) `bootJar_*` 태스크에 `targetJavaVersion` 지정. 지정하지 않으면 Boot 4에서 jar 생성 실패 |

**빌드 검증 (완료)**
- `./gradlew assemble` 성공. `RADAR_WEB.jar`(약 84MB)에 devtools가 **포함되지 않음** 확인 (S6). jsch 2.28.8, spring-security 7.1.2, thymeleaf-extras-springsecurity6 포함
- `:Daemon:buildAllRadarJar` 성공. 5종 jar의 manifest가 `Start-Class: kr.or.kimsn.radar.daemon.DaemonApplication`, `Radar-System-Type`, `Build-Jdk-Spec: 25`
- 기동 시도 로그에서 Log4j2가 `log4j2.yml`(YamlConfiguration)을 정상 로드하는 것 확인

**실제 서버 기동으로 확인하려던 시도 (실패 → 자동 테스트로 대체)**: DB 없이 웹 서버만 띄워 확인하려 했으나, `DataSourceConfig`의 DB 연결 풀(Hikari)이 기동 즉시 DB에 접속해 서버가 뜨지 않았다. 지연 초기화(lazy-init)를 켜도 JPA(`entityManagerFactory`)가 먼저 초기화되어 실패했고, 테스트용 인메모리 DB(H2)를 붙이는 방법은 작업 환경의 권한 정책으로 실행하지 못했다. 그래서 **서버를 띄우지 않고 웹 요청을 흉내 내는 자동 테스트**를 만들었다 (아래).

##### 자동 테스트 (2026-11-19, commit `e5d048f`) — DB 없이 실행

- **파일**: `radar-web/src/test/java/kr/or/kimsn/radar/securitytest/SecurityConfigTest.java`
- **실행**: `./gradlew :radar-web:test` (보고서: `radar-web/build/reports/tests/test/index.html`)
- **원리**
  - `@WebMvcTest`는 웹 계층(요청 처리, Spring Security 필터, Thymeleaf 화면)만 띄우고 DB·JPA는 띄우지 않는다
  - **실제 운영 코드를 그대로 사용**: `SecurityConfig`, `AuthenticationSuccess`, `AuthenticationFailure`, `LoginController`, `login.html`
  - **가짜로 바꾼 것은 회원 조회(`UserSecurityService`) 하나뿐**: DB 대신 테스트 계정 `tester`(비밀번호는 BCrypt로 암호화해 저장, 권한 USER)만 돌려주고, 다른 아이디는 "사용자 없음" 처리
  - 테스트를 별도 패키지(`kr.or.kimsn.radar.securitytest`)에 두고 테스트 전용 설정(`TestApp`)을 사용 → DB 연결을 요구하는 `RadarWebApplication`(`@EnableJpaRepositories`)이 로드되지 않음
  - `MockMvc`로 요청을 보내고 응답 코드·이동 주소·쿠키·화면 내용을 검사
- **추가한 테스트 의존성** (`radar-web/build.gradle`, 테스트에만 사용되어 운영 jar에는 포함 안 됨): `spring-boot-starter-webmvc-test`, `spring-boot-starter-security-test`
- **결과: 14개 모두 통과**

| # | 시나리오 | 요청 | 기대 결과 (= 실제 결과) | 의미 |
|---|---|---|---|---|
| C-01 | 로그인 화면 | 비로그인 `GET /login` | 200, 화면에 `name="_csrf"` hidden 입력과 `action="/loginProc"` 포함 | 로그인 화면은 열리고 CSRF 토큰이 자동 삽입됨 |
| C-02 | 정적 리소스 | 비로그인 `GET /css/radar/common.css` | 200 | 로그인 화면의 CSS/JS가 차단되지 않음 |
| C-03 | 관리 화면 차단 | 비로그인 `GET /manage/station_status` | 302 → `/login` | **S1 해결 확인**: 로그인 없이 관리 화면 불가 |
| C-04 | API 차단 (토큰 없음) | 비로그인 `POST /users/admin_user_del` | 403 | 외부에서 회원 삭제 API 호출 불가 |
| C-05 | API 차단 (토큰 있음) | 비로그인 `POST /users/admin_user_del` + CSRF | 302 → `/login` | 토큰이 있어도 로그인 없으면 실행 안 됨 |
| C-06 | CSRF 방어 | 로그인 사용자 `POST` (토큰 없음) | 403 | 로그인 상태를 악용한 위조 요청 차단 |
| C-07 | 정상 요청 통과 | 로그인 사용자 `POST` + CSRF | 404 | 보안 필터 통과 (테스트에는 해당 컨트롤러를 넣지 않았으므로 404가 정상) |
| C-08 | 정상 화면 통과 | 로그인 사용자 `GET /manage/station_status` | 404 | 위와 같음 |
| C-09 | 로그인 성공 | `POST /loginProc` (tester/올바른 비밀번호) + CSRF | 302 → `/`, `userId` 쿠키 = tester | `AuthenticationSuccess` 정상 동작 |
| C-10 | 로그인 실패 (비밀번호) | 틀린 비밀번호 | 302 → `/login?error=true` | `AuthenticationFailure` 정상 동작 |
| C-11 | 로그인 실패 (아이디) | 없는 아이디 | 302 → `/login?error=true` | 위와 같음 |
| C-12 | 위조 로그인 차단 | `POST /loginProc` (토큰 없음) | 403 | 외부 사이트에서 로그인 요청 위조 불가 |
| C-13 | 로그아웃 | 로그인 사용자 `POST /logout` + CSRF | 302 → `/login?logout`, `userId` 쿠키 삭제(유효기간 0) | 로그아웃 정상 |
| C-14 | 강제 로그아웃 차단 | `POST /logout` (토큰 없음) | 403 | 외부 사이트에서 강제 로그아웃 불가 |

- **처음 실행했을 때 실패한 내용과 수정 (참고)**
  1. `did not discover any tests` → Gradle 9는 JUnit 5 실행 설정이 필요. 루트 `build.gradle`에 `useJUnitPlatform()` 추가
  2. 14개 전부 `UsernameNotFoundException` → 테스트 코드 문제: Mockito에서 "아무 아이디 → 예외" 스텁을 먼저 등록한 뒤 "tester → 계정" 스텁을 등록하는 순간 첫 스텁이 실행됨. 인자 값으로 분기하는 스텁 하나로 변경
  3. C-03, C-05 실패: 실제 이동 주소는 `/login`인데 비교 패턴 `**/login`이 맨 앞 `/`와 맞지 않음. 정확한 주소 비교로 변경 (**보안 동작은 처음부터 정상이었음**)
- **대조군 확인 (테스트가 실제로 문제를 잡아내는지)**: `SecurityConfig`를 예전 상태(전체 허용 `anyRequest().permitAll()` + CSRF 끄기 `csrf.disable()`)로 **임시 변경**하고 테스트를 돌림 → **7개 실패**(C-01, C-03, C-04, C-05, C-06, C-12, C-14). 즉 누군가 보안 설정을 다시 풀면 테스트가 실패해서 알려 준다. 확인 후 원래대로 복구했고 `git status`로 변경 없음을 확인함
- **자동 테스트로 확인할 수 없는 것** → 8.2에서 실제 화면으로 확인 필요
  - 각 화면의 Ajax(`fnCall`) 저장/삭제가 `common.js`를 통해 토큰을 제대로 보내는지 (브라우저의 JavaScript 동작)
  - 검색 폼(통계, 이력, 문자 결과)이 토큰과 함께 제출되는지
  - 실제 DB 회원 계정으로 로그인
  - 세션 만료 후 Ajax 요청 시 화면 동작 (로그인 페이지 HTML이 돌아와 오류처럼 보일 수 있음)

**참고 / 후속 검토**
- 권한(Role) 구분은 아직 없음. 로그인한 사용자는 모두 회원 관리(`/users/**`) 등 전체 기능 사용 가능. ADMIN 전용으로 제한할지는 검토 필요 (권한 값: `USER`/`ADMIN`, `ROLE_` 접두어 없음 → `hasAuthority('ADMIN')` 형태로 써야 함)
- `LoginController`의 `@PostMapping("/logout")`은 Security LogoutFilter가 먼저 처리하므로 호출되지 않음 (정리 대상)
- 로그인 실패 메시지: `AuthenticationFailure`는 메시지를 세션에 저장하는데 `LoginController`는 request attribute를 읽음 → 메시지가 보이지 않는 기존 버그 (변경 없음)
- 템플릿에 `xmlns:sec` 선언은 있지만 실제 `sec:` 속성 사용은 0건 → extras를 추가해도 화면 변화 없음

#### E. 설정 파일 정리 (2026-11-19 완료, commit `1f1ae08`)

##### 변경 내용

| 파일 | 변경 | 근거 |
|---|---|---|
| `Daemon/.../application.yml` | `spring.config.use-legacy-processing: true` 제거 | Boot 3.0에서 삭제된 키. Boot 4.1.2 jar의 `org/springframework/boot/context/config/` 전체 클래스에서 참조 0건 → 무시되던 키 |
| | `spring.config.import`: `optional:classpath*:common-data.yml` → `optional:classpath:common-data.yml` | **기동 테스트에서 발견한 실제 오류**: `IllegalStateException: Location 'classpath*:common-data.yml' cannot use classpath wildcards`. Boot 2.7에서는 legacy processing이 켜져 있어 import 자체가 처리되지 않았기 때문에 드러나지 않았음 |
| | `server.servlet.encoding.*` (charset, force, enabled) 제거 | Daemon은 웹 서버가 없음 (Daemon jar 메타데이터에 servlet 인코딩 속성 0건) → 원래 효과 없음. Boot 4에서는 `spring.servlet.encoding.*`으로 이름이 바뀌어 옛 키가 `level: error`로 삭제됨 (`spring-boot-web-server-4.1.2.jar` 메타데이터, since 4.0.0) |
| | `spring.devtools.livereload.enabled` 제거 | Daemon 모듈에 devtools 의존성 없음 |
| `radar-web/.../application.yml` | `spring.config.use-legacy-processing: true` 제거 | 위와 동일 |
| | devtools 설정은 유지하고 주석 추가 | `spring-boot-devtools-4.1.2.jar` 메타데이터에 존재하는 정상 키. developmentOnly 실행에서만 적용 |
| `common-data.yml` | 주석만 추가 (값 변경 없음) | `jpa.open-in-view: false`가 `spring.` 아래가 아니라 `config.` 아래에 있어서 Spring이 읽지 않음. 실제 적용값은 기본값 `true`. 고치면 화면 렌더링 중 지연 로딩 동작이 바뀔 수 있어 D 단계에서 DB 테스트 후 결정. `config.datasource.max-pool` 등 풀 옵션도 `DataSourceConfig.java`의 하드코딩 값이 사용됨 |

변경하지 않은 것: `config/application*.yml`(외부 운영 설정. `radar.*` 커스텀 키만 있어 Boot 버전 영향 없음), `log4j2.yml` 2종(아래 T2-4에서 정상 확인), `spring.thymeleaf.cache: false`(운영에서는 true가 바람직하지만 동작 변경이라 별도 검토)

##### 테스트 방법과 결과

모든 테스트는 개발 PC(WSL Ubuntu 26.04, OpenJDK 25.0.5.1+2, Gradle 9.9.1)에서 실행했다. 실행 위치는 세션 임시 폴더이며 프로젝트나 서버 경로에는 파일을 만들지 않았다. **운영 DB와 관측소 서버에는 접속하지 않았고**, 비밀번호와 IP 값은 출력하지 않도록 작성했다.

**T0. 빌드**
- 명령: `./gradlew assemble :Daemon:buildAllRadarJar --no-daemon`
- 결과: ✅ 성공. `RADAR_WEB.jar`와 Daemon 5종 jar(RDR, SDR, TDWR, RDR_B, SDR_B) 생성
- 수정한 yml이 jar에 들어갔는지 확인: `RDR.jar`를 풀어서 `BOOT-INF/classes/application.yml`에 `classpath:common-data` 반영됨을 확인

**T1. 설정 키 정적 검사 (Boot 4 메타데이터 대조)**: 스크립트 `docs/verify/check_props.py`
- 방법: 빌드된 boot jar 안 `BOOT-INF/lib/*.jar` 전부에서 `META-INF/spring-configuration-metadata.json`과 `additional-spring-configuration-metadata.json`을 모음 → 알려진 속성, deprecated/삭제 속성(대체 키 포함) 목록 생성 → yml의 모든 leaf 키를 펼쳐서 `spring.`/`server.`/`logging.`/`management.` 접두어 키를 대조. `logging.level.<패키지>` 같은 Map 타입 하위 키는 부모 속성의 타입으로 판정
- 명령: `python3 -I docs/verify/check_props.py <boot.jar> <yml>...`
- 메타데이터 규모: RADAR_WEB.jar 속성 721개 (deprecated/삭제 106개), RDR.jar 속성 467개 (deprecated/삭제 68개)

| 대상 | 수정 전 | 수정 후 |
|---|---|---|
| radar-web `application.yml` (키 14개 → 13개) | UNKNOWN 2: `spring.config.use-legacy-processing`, `spring.devtools.livereload.enabled` | UNKNOWN 1: `spring.devtools.livereload.enabled` (**예상된 결과**: 운영 jar에 devtools가 없어 메타데이터도 없음. devtools jar에는 정의돼 있음을 별도 확인) |
| Daemon `application.yml` (키 14개 → 9개) | UNKNOWN 5: `server.servlet.encoding.charset/enabled/force`, `spring.config.use-legacy-processing`, `spring.devtools.livereload.enabled` | **문제 키 0건** |
| `common-data.yml`, `config/application.yml` | 커스텀 접두어(`config.`, `radar.`)만 있어 검사 대상 아님 | 동일 |

- 한계: 정적 검사는 **값의 형식**은 보지 않는다. 실제로 `classpath*:` 와일드카드 오류는 T1에서 잡히지 않았고 T3 기동 테스트에서 발견됨

**T2. 설정 로딩 테스트 (실제 런타임 라이브러리)**: 프로그램 `docs/verify/ConfigLoadTest.java`
- 방법: `RDR.jar`를 임시 폴더에 풀고, 그 안의 `BOOT-INF/classes`와 `BOOT-INF/lib/*`(Spring Boot 4.1.2, Spring 7.0.10, SnakeYAML 2.6, Log4j 2.25.5, Jackson 2.21.7 등 운영과 같은 jar)를 classpath로 Java 25 단일 파일 실행
- 명령: `java -cp "<RDR.jar 풀린 폴더>/BOOT-INF/classes:<...>/BOOT-INF/lib/*" docs/verify/ConfigLoadTest.java <프로젝트 루트>`
- 결과: ✅ **PASS 53 / FAIL 0** (yml 최종 수정 후 다시 실행해도 동일)

| 그룹 | 무엇을 흉내 냈나 | 확인 내용 | 결과 |
|---|---|---|---|
| T2-1 | `YmlRefreshManager.reloadYml()`: `YamlPropertySourceLoader`로 외부 yml을 읽어 Environment 최상단에 추가 | `config/application.yml`, `config/application_real.yml` 각각: `global.mode`, `PauseTime`(Long), `sessionTimeOut`/`connectTimeOut`(Integer), `call_from`/`template_code` 조회 | 2파일 × 6건 PASS |
| | `Scheduler`의 동적 크론 | 5개 시스템(KMA_RDR, KMA_SDR, KMA_TDWR, MCEE_RDR, MCEE_SDR)의 `cron-expression`을 Spring 7 `CronExpression.parse()`로 파싱 | 2파일 × 5건 PASS |
| | `StepOneService`의 관측소별 접속 정보 조회 (`radar.config.paths.<TYPE>.stations.<SITE>.*`) | 관측소별 IP/ID/PASSWORD/PATH/FILE 존재, PORT→Integer, file_size_min/max→Long 변환. 관측소 수 11/3/2/7/11곳(총 34곳)이 PyYAML로 별도 집계한 수와 일치 | 2파일 × 5건 PASS |
| T2-2 | `DataSourceConfig`의 `@PropertySource(factory = YamlPropertySourceFactory)` | `common-data.yml`의 driver-class-name = `com.mysql.cj.jdbc.Driver`, url 형식 `jdbc:mysql://`, username/password 존재, MySQL 드라이버 클래스 로드(mysql-connector-j 9.7.0) | 5건 PASS |
| T2-3 | 모듈 `application.yml` 수정 결과 | Daemon·Web 각각: `use-legacy-processing` 없음, `server.servlet.encoding.*` 없음, `spring.config.import`가 common-data.yml 유지, `logging.config` 유지, `spring.application.name` 유지 | 2파일 × 5건 PASS |
| T2-4 | Log4j2의 `log4j2.yml` 해석 (Boot 4는 Jackson 3를 쓰지만 Log4j2는 Jackson 2 YAML이 필요) | Daemon·Web 각각: `YamlConfiguration` 생성, appender 정의 해석(Daemon: RollingFile_Appender / Web: Console_Log, RollingFile_Appender), logger 정의 해석. `initialize()`만 하고 `start()`는 하지 않아 로그 파일은 생성하지 않음 | 2파일 × 3건 PASS |

**T3. 실제 jar 기동 테스트 (가짜 DB)**
- 방법: 빈 임시 폴더에서 `java -jar RDR.jar --config.datasource.url='jdbc:mysql://127.0.0.1:1/none'` 와 `java -jar RADAR_WEB.jar --server.port=18081 --config.datasource.url=...` 실행 (각 90초 제한). 명령행 인자가 `@PropertySource`보다 우선순위가 높으므로 **DB 접속은 항상 존재하지 않는 로컬 주소로 향함** → DB 연결 단계 이전까지의 기동 과정(설정 처리, 로깅, Bean 정의)을 확인
- Daemon: 외부 `config/application.yml`은 임시 폴더 기준 경로에 없어서 읽지 않음 (YmlRefreshManager 탐색 경로 밖). `DAEMON_TYPE`을 지정하지 않았고, 스케줄러가 시작되기 전에 종료되므로 SFTP 접속 없음

| 대상 | 1차 결과 (수정 전 import) | 2차 결과 (수정 후) |
|---|---|---|
| Daemon `RDR.jar` | ❌ `Location 'classpath*:common-data.yml' cannot use classpath wildcards`로 즉시 실패 → **import 수정의 근거** | ✅ `Starting DaemonApplication ... using Java 25.0.5.1` → `profile is active: "local-daemon"` → `Bootstrapping Spring Data JPA repositories in DEFAULT mode` → `Found 24 JPA repository interfaces` → 가짜 DB에서 `Communications link failure`로 종료 (**예상된 결과**) |
| Web `RADAR_WEB.jar` | ✅ `Starting RadarWebApplication ... using Java 25.0.5.1` → `Tomcat initialized with port 18081` → 가짜 DB에서 `Communications link failure` (예상된 결과) | (웹 yml은 1차 실행 전에 이미 수정돼 있었음) |

- 공통 확인: Log4j2가 jar 내부 `log4j2.yml`을 `YamlConfiguration`으로 로드하고, 상대 경로 `./logs/`에 `RDR.log` / `RADAR_WEB.log` 생성 (임시 폴더 안)
- 확인하지 못한 것: DB 연결 이후 단계(Hibernate 초기화, Repository 쿼리, 스케줄러 실행). D 단계에서 DB 연동 테스트로 확인 필요

#### F. 운영 실행 스크립트 JDK 25 적용 (2026-11-19 완료, commit `83e3e1a`, `01cf88b`)

##### 배경

운영 서버에서는 `config/*_process.sh` 스크립트로 프로그램을 시작/종료한다. 기존 스크립트는 그냥 `java` 명령을 썼기 때문에, 서버에 Java가 여러 개 설치돼 있으면 **어떤 Java로 실행될지 보장되지 않았다**(PATH 순서에 따라 Java 8/11이 실행될 수 있음). Java 25 전용으로 빌드한 jar를 옛 Java로 실행하면 `UnsupportedClassVersionError`로 실패하고, 패치 전 Java 25로 실행하면 Java4Shell에 노출된다.

##### 변경 내용

| 대상 | 변경 |
|---|---|
| `RDR`, `RDR_B`, `SDR`, `SDR_B`, `TDWR`, `RADAR_WEB` `_process.sh` (6개) | ① `JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-25-openjdk}`: 서버에 `JAVA_HOME` 환경 변수가 있으면 그 값, 없으면 기본 경로 사용 |
| | ② `java` → `"$JAVA_BIN"`(= `$JAVA_HOME/bin/java`)로 실행: PATH와 관계없이 정해진 JDK 사용 |
| | ③ `check_java` 함수: `start` 때 실행 파일이 있는지, `java.version`이 `25.0.5.1` 이상인지 검사. 아니면 `[ERROR]` 메시지 출력 후 **시작하지 않음**(종료 코드 1). 버전 비교는 `sort -V`(버전 순서 정렬) 사용 |
| | ④ `version` 명령에 Java 버전 한 줄 추가 출력 |
| `radar_process.sh` | **변경 안 함**: `daemon-xx.jar`를 가리키는 예전 템플릿으로, 실제 운영에 쓰이지 않음 |

- 기본 경로 `/usr/lib/jvm/java-25-openjdk`는 RHEL 계열 패키지 설치 시의 일반적인 경로다. **운영 서버에 실제로 설치된 경로가 다르면** ① 스크립트 기본값을 고치거나 ② 서버에서 `export JAVA_HOME=<실제 경로>`를 설정하면 된다
- 기존 동작(시작/종료/목록 로직, 로그 파일 위치, `DAEMON_TYPE` 값)은 바꾸지 않았다

##### 테스트 방법과 결과

- **문법 검사**: `bash -n <스크립트>` → 6개 모두 통과
- **실행 테스트 준비** (운영 경로는 건드리지 않음)
  1. 임시 폴더에 스크립트를 복사하고 `RADAR_HOME`만 임시 폴더로 바꿈 (그 외 내용은 원본과 동일함을 `diff`로 확인)
  2. **테스트용 jar(Probe)**: 진짜 프로그램 대신, 받은 `DAEMON_TYPE`·`java.version`·`java.home`을 출력하고 30초 뒤 종료하는 작은 jar를 만들어 `RDR.jar` 등의 이름으로 배치
  3. **가짜 JDK**: `java.version = 21.0.5`, `25.0.5`를 출력하는 가짜 `bin/java`를 만들어 낮은 버전 상황을 재현
- **1차 결과: Web 스크립트 결함 발견 → 수정**: 처음 수정(`83e3e1a`)에서 Web 스크립트에만 `check_java` 정의가 빠져 `check_java: command not found`, `nohup: failed to run command ''` 오류가 났다. 원인은 블록 복사 시 끝 위치를 `}`+줄바꿈으로 찾았는데 `${JAVA_HOME:-...}` 줄에서 먼저 걸린 것. **이 결과를 확인하기 전에 commit이 같은 명령 안에서 실행되어** 결함이 있는 상태로 commit됐고, `01cf88b`에서 바로 수정했다. 이후로는 테스트 확인 후에 commit하도록 단계를 나눔
- **최종 결과 (6개 스크립트 전부 같은 테스트)**

| 케이스 | 조건 | 기대 | RDR | RDR_B | SDR | SDR_B | TDWR | RADAR_WEB |
|---|---|---|---|---|---|---|---|---|
| F1 | `JAVA_HOME`이 없는 경로 | `[ERROR] Java 실행 파일을 찾을 수 없습니다`, 종료 코드 1 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| F2 | Java 21 | `[ERROR] Java 버전(21.0.5)이 ... 낮거나`, 종료 코드 1 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| F3 | Java 25.0.5 (패치 직전 버전) | 종료 코드 1 (**25.0.5 < 25.0.5.1 판정 확인**) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| F6 | 실제 JDK 25.0.5.1 | `Java: .../java (25.0.5.1)` 출력 후 기동. Probe 출력에서 `java.version=25.0.5.1` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| F6' | `DAEMON_TYPE` 전달 | KMA_RDR / MCEE_RDR / KMA_SDR / MCEE_SDR / KMA_TDWR / (Web 없음) | ✅ KMA_RDR | ✅ MCEE_RDR | ✅ KMA_SDR | ✅ MCEE_SDR | ✅ KMA_TDWR | ✅ null |

- **RDR 스크립트로 추가 확인한 것**
  - F4 `JAVA_HOME` 미지정: 기본값 `/usr/lib/jvm/java-25-openjdk/bin/java`를 찾음 → 개발 PC(Ubuntu)에는 `-amd64`가 붙은 경로라 `[ERROR]` (**의도된 동작**: 경로가 다르면 시작하지 않음)
  - F5 `version`: `RDR` + `openjdk version "25.0.5.1" 2026-11-17`
  - F7 `list`: 실행 중인 프로세스 표시 정상
  - F8 이미 실행 중일 때 `start`: `RDR Alive.` 출력, 중복 실행 안 함
  - F9 `stop`: **WSL에서는 종료되지 않음**. 기존 로직이 부모 프로세스 ID가 1인 프로세스만 종료하는데 WSL에서는 부모가 `/init`(PID 2513)이기 때문. RHEL 서버에서는 PID 1(systemd)이 부모가 되므로 기존처럼 동작할 것으로 판단. **기존 로직이며 이번에 바꾸지 않음** → 운영 서버에서 `stop` 동작을 한 번 확인할 것
- 테스트 후 임시 프로세스는 모두 종료했음 (`pgrep`으로 0개 확인)

#### S5. SFTP 비밀번호 로그 기록 제거 (2026-11-19 완료, commit `3ad5473`)

- **문제**: `StepOneService`가 수집 주기마다 관측소 SFTP 비밀번호를 `log.info("[{} pwd]: {}", ...)`로 로그 파일에 기록했다. 로그 파일을 볼 수 있는 사람은 누구나 관측소 root 비밀번호를 알 수 있었다
- **변경**: 비밀번호가 설정돼 있으면 `******`, 비어 있으면 `(미설정)`만 기록
  ```java
  log.info("[{} pwd]: {}", siteStr, sitePwd.isEmpty() ? "(미설정)" : "******");
  ```
- **확인 방법**
  - `./gradlew :Daemon:compileJava` 성공
  - 전체 소스에서 비밀번호 변수를 로그로 출력하는 코드 검색: `grep -rnE "log\.[a-z]+\(.*([Pp]wd|[Pp]assword)"` → 수정한 줄 외에는 주석 처리된 2줄(`LoginController`)뿐. `System.out`으로 출력하는 곳도 없음
  - `SftpUtil`의 로그도 확인: 호스트·경로·파일명만 기록하고 비밀번호는 기록하지 않음
- **남은 조치 (서버 작업)**: 운영 서버의 기존 로그 파일(`/home/watcher/deamon/*/logs/`)에 이미 비밀번호가 남아 있다. 로그 파일 정리와 **관측소 비밀번호 교체**가 필요하다 (S2와 함께 진행)

#### G. `.gitattributes` 추가 (2026-11-19 완료, commit `7bb787f`)

- **왜 필요한가**: Windows용 `gradlew.bat`은 Windows 줄바꿈(CRLF)이어야 하고, 리눅스용 셸 스크립트(`*.sh`, `gradlew`)는 LF여야 한다. 셸 스크립트에 CRLF가 섞이면 `/bin/bash^M: bad interpreter` 오류로 실행되지 않는다. Git 설정이 사람마다 달라도 같은 결과가 나오도록 저장소에 규칙을 넣었다
- **규칙**: `/gradlew.bat` → CRLF, `/gradlew`·`*.sh` → LF, `*.jar` → 바이너리(변환 금지). 영향 범위를 줄이려고 `note/` 폴더의 다른 배치 파일에는 적용하지 않음
- **확인**: `git check-attr`로 규칙 적용 확인, `git add --renormalize .` 실행 후 변경된 파일 0개 확인 (저장소 안의 파일은 이미 LF로 저장돼 있어 내용 변화 없음)

#### J. SFTP 라이브러리(jsch) 교체에 따른 접속 호환성 점검 (2026-11-19, DB 없이 확인 가능한 부분)

- **배경**: A 단계에서 jsch를 `com.jcraft:jsch:0.1.55`(2018년 이후 유지보수 중단)에서 `com.github.mwiede:jsch:2.28.8`(유지보수 중인 후속판)로 바꿨다. 코드(패키지명)는 같지만, **새 버전은 보안상 약한 암호 알고리즘을 기본으로 허용하지 않는다**
- **확인 방법**: 두 버전 jar로 각각 `JSch.getConfig(...)` 기본값을 출력하는 작은 프로그램을 실행해 비교 (예전 jar는 `libs/jsch-0.1.55.jar`, 외부 접속 없음)

| 항목 | 0.1.55 (기존)에만 있고 2.28.8 (현재)에서 빠진 것 | 2.28.8에 새로 생긴 것 |
|---|---|---|
| 키 교환 (kex) | `diffie-hellman-group14-sha1`, `diffie-hellman-group-exchange-sha1`, `diffie-hellman-group1-sha1` | `curve25519-sha256`, `mlkem768x25519-sha256`, `diffie-hellman-group14-sha256`, `group16/18-sha512` 등 |
| 서버 호스트 키 | `ssh-rsa`(SHA-1 서명), `ssh-dss` | `rsa-sha2-256`, `rsa-sha2-512`, `ssh-ed25519` 등 |
| 암호화 (cipher) | `aes*-cbc`, `3des-*`, `blowfish-cbc` | `aes128/256-gcm@openssh.com` |
| 무결성 (MAC) | `hmac-md5`, `hmac-sha1-96`, `hmac-md5-96` | `hmac-sha2-*-etm@openssh.com`, `hmac-sha2-512` |

- **영향**: 관측소 서버의 SSH(OpenSSH)가 **7.2보다 오래된 버전**(예: CentOS/RHEL 6 계열의 OpenSSH 5.x)이면 `rsa-sha2-*`를 지원하지 않아 `Algorithm negotiation fail` 또는 `UnknownHostKey`류 오류로 **접속이 실패할 수 있다**. 관측소 장비(Rainbow5 등)에 오래된 OS가 있는지 확인이 필요하다
- **확인과 대응 방법** → 8.4

#### K. 사용자 관리 ADMIN 전용 권한 (2026-11-19 완료, commit `8e233cc`)

##### 배경 — 왜 필요한가

- C 단계에서 "로그인한 사람만" 쓸 수 있게 했지만, **로그인한 사람은 누구나(USER 포함) 사용자 관리 기능을 쓸 수 있었다**
- 더 큰 문제: 회원 생성 화면(`admin_user_form.html:104`)이 **권한 값을 브라우저에서 정해 서버로 보낸다** (`roles: memberClass == "A" ? "ADMIN" : "USER"`). 따라서 USER 계정으로 로그인한 사람이 회원 생성 API에 `roles=ADMIN`을 보내 **스스로 관리자 계정을 만들 수 있었다**
- 이번 변경으로 `/users/**` 전체를 ADMIN만 쓸 수 있게 하여 이 경로를 막았다

##### 변경 내용

| 파일 | 변경 |
|---|---|
| `SecurityConfig.java` | `ADMIN_PATHS = { "/users/**" }` 추가, `.requestMatchers(ADMIN_PATHS).hasAuthority("ADMIN")` (공개 경로 다음, `anyRequest().authenticated()` 앞) |
| `templates/error/403.html` (새 파일) | 권한 없음 안내 화면. 기존 `error/404.html`을 복사해 제목과 문구만 변경 ("403 - Forbidden", "관리자(ADMIN) 계정으로 로그인하세요") |
| `SecurityConfigTest.java` | 권한 테스트 K-01~K-09 추가, 기존 C-06·C-07 대상 경로 변경 (아래) |

- **ADMIN 전용이 된 경로** (`/users/**` 아래 전부)
  - 화면: `/users/admin_user`(사용자 목록), `/users/admin_user_form`(등록·수정), `/users/joinForm`
  - API: `/users/admin_user_create`, `/users/admin_user_modify`, `/users/admin_user_del`
- **그 외 기능**(관측소, 통계, 문자, 설정 화면 등)은 지금처럼 로그인한 모든 사용자가 사용 가능
- **`hasRole`이 아니라 `hasAuthority`를 쓴 이유**: `PrincipalDetails.getAuthorities()`가 DB `watchdog_member.roles` 값(`Roles` enum 이름)을 그대로 권한 이름으로 돌려준다 → 권한 이름이 `"ADMIN"`/`"USER"`다. Spring의 `hasRole("ADMIN")`은 `"ROLE_ADMIN"`을 찾기 때문에 맞지 않는다. 이 전제는 테스트 K-08로 확인한다
- **적용 전 데이터 확인**: 2026-07-16 덤프(`Query/dump/dump-watchdog-*.sql`)의 `watchdog_member`는 2명이고 `roles`가 ADMIN 1명, USER 1명이었다 (개인정보는 출력하지 않고 개수만 집계). **운영 DB에 ADMIN이 한 명도 없으면 아무도 사용자 관리를 할 수 없게 되므로** 배포 전에 반드시 확인한다 (8.5)

##### 테스트 방법과 결과 — `./gradlew :radar-web:test` (DB 없이 실행)

- 테스트 계정 2개를 가짜 회원 조회(Mock)에 등록: `tester`(USER), `admin1`(ADMIN). 비밀번호는 BCrypt로 암호화해 저장
- `with(user(...))`로 로그인 상태를 흉내 낼 때도 실제와 같은 권한 이름(`"USER"`/`"ADMIN"`)을 사용 (Spring 테스트 기본값 `ROLE_USER`를 쓰면 실제와 달라짐)
- **결과: 전체 23개 통과** (기존 14개 + 권한 9개)

| # | 시나리오 | 기대 결과 (= 실제) |
|---|---|---|
| K-01 | USER로 사용자 목록 화면 `GET /users/admin_user` | 403 |
| K-02 | USER로 회원 생성 API `POST /users/admin_user_create` + CSRF | 403 (**USER가 관리자 계정을 만들 수 없음**) |
| K-03 | USER로 회원 수정·삭제 API | 둘 다 403 |
| K-04 | ADMIN으로 사용자 화면 2개·생성·삭제 API | 모두 보안 통과 (테스트에는 컨트롤러가 없어 404) |
| K-05 | ADMIN으로 일반 관리 화면 | 보안 통과 (ADMIN도 다른 기능 사용 가능) |
| K-06 | **실제 로그인 흐름**: `admin1`로 `/loginProc` 로그인 → 받은 세션으로 `/users/admin_user` | 보안 통과. 실제 `PrincipalDetails`가 만든 권한으로 판정됨 |
| K-07 | 실제 로그인 흐름: `tester`(USER)로 로그인 → `/users/admin_user` | 403 |
| K-08 | `PrincipalDetails`의 권한 이름 | ADMIN → `"ADMIN"`, USER → `"USER"` (`hasAuthority` 전제 확인) |
| K-09 | `error/403.html` 템플릿 렌더링 | "403 - Forbidden"과 안내 문구 포함 |

- **기존 테스트 조정**: C-06·C-07은 "로그인 사용자 + CSRF 유무"를 보는 테스트인데, 대상이었던 `/users/admin_user_del`이 ADMIN 전용이 되어 USER는 CSRF와 상관없이 403이 된다. 테스트 의도를 유지하려고 대상을 설정 변경 API `/manage/station_status_modify`로 바꿨다
- **처음 실행 시 실패 1건과 수정**: K-09를 처음에는 `/error` 요청으로 확인하려 했으나 Spring 기본 오류 화면(Whitelabel)이 나왔다. 진단용 임시 테스트로 확인한 결과, 테스트 환경(MockMvc)에는 `DispatcherServlet` Bean이 없어 Boot의 "상태 코드별 오류 화면 연결" Bean(`conventionErrorViewResolver`)이 만들어지지 않는 것이 원인이었다 (실제 앱에서는 만들어짐 — 기존 `error/404.html`도 같은 방식). 그래서 K-09는 **템플릿을 Thymeleaf로 직접 렌더링해 내용을 확인**하는 방식으로 바꾸고, "403 상황에서 이 화면이 실제로 뜨는지"는 8.2 화면 확인 항목에 추가했다. 진단용 테스트는 삭제했다
- **대조군**: ADMIN 규칙 줄을 임시로 빼고 실행 → **K-01, K-02, K-03, K-07 4개 실패** (USER가 사용자 관리에 접근 가능해짐을 테스트가 잡아냄). 확인 후 원복 (`hasAuthority("ADMIN")` 존재 확인)
- 전체 `./gradlew build` 성공, `check_comments.py` 사라진 기존 주석 0건

##### 남은 확인과 참고

- **메뉴 표시**: 메뉴는 DB(`/getMenuData`)에서 오므로 USER에게도 "사용자 관리" 메뉴가 보일 수 있다. 눌러도 403 화면이 나올 뿐 기능은 막혀 있다. 메뉴를 숨길지는 결정 사항 (8.6)
- **`roles`가 비어 있는(NULL) 회원**: `PrincipalDetails.getAuthorities()`가 `user.getRoles().toString()`을 호출하므로 로그인 시 오류가 날 수 있다 (기존 코드의 위험, 이번에 변경하지 않음). 배포 전 확인 SQL은 8.5

---

## 7. 작업 이력

| 일자 | 작업 | 결과 / 비고 |
|---|---|---|
| 2026-11-19 | Java 버전 확인 | 로컬(WSL)에 JDK 미설치. 프로젝트는 Java 11 타깃. Java 11 취약 버전 조사 |
| 2026-11-19 | 의존성 CVE 전체 점검 | 약 123건 확인 (5장 참조). 수정 미적용 |
| 2026-11-19 | 소스 구조 파악 | 2~4장 작성. 보안 이슈 S1~S7 식별 |
| 2026-11-19 | 분석 문서 생성 | 본 파일(`docs/project-analysis.md`) 생성 |
| 2026-11-19 | OpenJDK 업그레이드 계획 수립 | CVE-2026-114938(Java4Shell) 조사. 목표 JDK 25.0.5.1과 Spring Boot 4.1.x. 소스 수정 범위 정리 (6장). 수정은 아직 적용하지 않음 |
| 2026-11-19 | 문서 보완 | 진행 체크리스트(6.6)와 참고 자료 링크(6.7) 추가 |
| 2026-11-19 | 개발 PC OpenJDK 25 설치 | Ubuntu 26.04 보안 저장소의 `openjdk-25-jdk-headless 25.0.5.1+2-1~26.04.1` 설치 (패키지 변경 이력에 CVE-2026-114938 포함 확인). `java`/`javac` 25.0.5.1 동작 확인. JAVA_HOME 자동 설정은 권한 정책으로 보류(사용자 직접 설정 안내). 개발 PC CPU가 AVX-512 VBMI를 지원해 Java4Shell 영향 대상이었음 |
| 2026-11-19 | Gradle 9.9.1 설치와 wrapper 생성 | 배포본 SHA-256 검증 후 `~/opt/gradle-9.9.1`에 설치. 빈 임시 프로젝트에서 wrapper 생성 후 프로젝트에 복사 (`gradlew.bat` 교체, `gradlew`·`gradle/wrapper/` 신규). `./gradlew --version` 확인 결과 Gradle 9.9.1 / JVM 25.0.5.1. 현재 build.gradle로 `./gradlew help`를 실행하면 **실패(예상한 결과)**: `build.gradle:16`에서 `org/gradle/api/plugins/JavaPluginConvention` 오류. Spring Boot 2.7 플러그인이 Gradle 9에서 삭제된 API를 사용하기 때문 → 6.3-A 빌드 설정 전환 필요 |
| 2026-11-19 | **6.3-A 빌드 설정 전환 (Spring Boot 4.1.2)** | 6.8-A 변경 내역 참조. `./gradlew help` 성공, 3개 모듈 runtimeClasspath 다운로드 실패 0건. `compileJava`는 예상대로 실패: `package javax.persistence does not exist` 등 (common-data에서 javac 오류 출력 한도 200건 도달) → 다음 단계 B(javax → jakarta) |
| 2026-11-19 | A 단계와 문서 Git commit | `.gitignore`에서 `gradle/`, `gradlew` 규칙 제거 후 wrapper 포함 commit `ade302c`, 문서 commit `3f363cf` (브랜치 `ver_java25`) |
| 2026-11-19 | **6.3-B javax → jakarta 전환** | 모듈별 commit: common-data `da91928`, Daemon `a0543da`, radar-web `a3b58b1`. common-data와 Daemon 컴파일 성공. 그 외 Boot 4/Spring 6 API 변경 2건(`@EntityScan` 패키지, Trigger 반환형) 함께 수정. radar-web은 SecurityConfig(C 단계)만 남음. 상세는 6.8-B |
| 2026-11-19 | **6.3-C Security 7 재작성 + S1 인증 복구** | commit `b75f60c`(SecurityConfig), `b7e0a7d`(화면 CSRF 토큰 처리), `47d1b27`(Daemon bootJar targetJavaVersion 보완). 전체 `assemble`과 Daemon 5종 jar 빌드 성공, 운영 jar에서 devtools 제외 확인. **런타임 검증은 DB가 필요해 미완료** → 6.8-C 검증 절차 |
| 2026-11-19 | **6.3-E 설정 파일 정리** | commit `1f1ae08`. `use-legacy-processing` 제거(2곳), Daemon의 `server.servlet.encoding.*`·devtools 제거, `common-data.yml`의 효과 없는 jpa 설정 주석 표시. **기동 테스트로 실제 오류 1건 발견·수정**: Daemon `spring.config.import`의 `classpath*:` 와일드카드 때문에 Boot 4에서 기동 실패하던 문제. 검증: T1 정적 검사(Daemon 문제 키 0건), T2 설정 로딩 53/53 PASS, T3 가짜 DB 기동 테스트 통과. 테스트 스크립트는 `docs/verify/`에 보관 |
| 2026-11-19 | **6.3-F 운영 스크립트 JDK 25 적용** | commit `83e3e1a`. 6개 스크립트에 `JAVA_HOME` 기본값, `$JAVA_BIN` 실행, `check_java`(25.0.5.1 이상 검사) 추가. **Web 스크립트에 함수 정의가 빠진 채 commit됨**(테스트 결과 확인 전에 commit이 같은 명령에서 실행) → `01cf88b`에서 수정. 최종: 6개 모두 F1~F3(차단)·F6(정상 기동, DAEMON_TYPE 전달) 통과. 기존 `stop` 로직은 WSL에서 동작하지 않음(PPID 조건, 서버에서 확인 필요) |
| 2026-11-19 | **보안 S5 해결** | commit `3ad5473`. SFTP 비밀번호 로그 기록 → `******`. 다른 비밀번호 출력 코드 없음 확인. 기존 운영 로그 정리·비밀번호 교체는 서버 작업으로 남음 |
| 2026-11-19 | **C 단계 자동 테스트 추가** | commit `e5d048f`. `@WebMvcTest` 기반 14개 시나리오, DB 없이 14/14 통과. 대조군(보안 설정을 예전처럼 풀었을 때) 7개 실패 확인 후 원복. `./gradlew build` 전체(컴파일+테스트+jar 6종) 처음으로 성공 |
| 2026-11-19 | `.gitattributes` 추가 | commit `7bb787f`. `gradlew.bat` CRLF, `*.sh`·`gradlew` LF, `*.jar` binary. renormalize 결과 변경 파일 0개 확인 |
| 2026-11-19 | jsch 알고리즘 호환성 점검 | 0.1.55와 2.28.8의 기본 알고리즘 비교: `ssh-rsa`(SHA-1), `ssh-dss`, sha1 키 교환, CBC 암호화 등이 기본값에서 빠짐 → 오래된 관측소 서버 접속 실패 가능성. 서버 확인 절차와 대응 코드를 8.4에 정리. 보안 이슈 S8(호스트 키 검증 생략) 추가 |
| 2026-11-19 | **주석 구분 표시와 기존 주석 복원** | commit `ab480ac`. 사용자 요청: "파일 수정 시 내가 작성한 주석과 구분". Claude 주석 전체에 `[Claude 2026-11-19]` 표시(여러 줄은 시작/끝 표시, 새 파일은 머리말). 작업 중 바뀌거나 지워졌던 기존 주석을 원문 복원(주석 처리 import의 javax 3줄, SecurityConfig 주석 약 15줄은 "기존 코드 원문 보존" 블록으로, build.gradle 계열 6곳, 운영 스크립트 6개의 `#JAVA_HOME` 줄, login.html 1줄). 실행 코드 변경 0건(diff 분석). 재검증: build·테스트 14/14, 스크립트 6개, ConfigLoadTest 53/53, check_props 문제 0, 사라진 기존 주석 0건 |
| 2026-11-19 | **검토자 검증 가이드(9장)·검증 스크립트 추가** | 사용자 요청: "내가 어떻게 확인하고 검증해야 하는지". 단계별 diff 검토 포인트·실행 명령·기대 결과·기록표 작성. 수작업 검증을 스크립트로 정리: `docs/verify/test_process_scripts.sh`(운영 스크립트 6개 × 7항목, 결과 FAIL 0), `JschDefaults.java`, `check_comments.py`(표시 없는 추가 주석 1건=기존 주석 복원, 사라진 기존 주석 0건). 9장의 모든 명령을 문서에 적힌 그대로 실행해 기대 결과와 일치 확인 (이 환경에 `unzip`이 없어 대체 명령도 확인) |
| 2026-11-19 | **주석 표시 형식 변경** | commit `f277315`. 사용자 요청: "명확하게 내가 아닌 것 확인할 수 있게. 예: `/* Claude 주석 */`". 모든 Claude 주석을 `/* Claude 주석 (2026-11-19): ... */`(yml·sh는 `# /* ... */`, HTML은 `<!-- /* ... */ -->`)로 변환, `check_comments.py`도 새 표시 인식으로 수정. 검증: build·테스트 14/14, 스크립트 FAIL 0, 사라진 기존 주석 0건. **절차 실수**: 코드 변경 여부 검사와 commit을 한 명령으로 실행해, 검사가 "코드 변경 파일 3개"를 보고했는데도 commit됨. 직후 확인 결과 2개(build.gradle 계열)는 여러 줄 블록 주석의 둘째 줄을 검사 로직이 코드로 오인한 것, 1개(check_comments.py)는 의도한 검사 로직 수정으로 실제 문제는 없었음. 이후 검사와 commit을 분리 |
| 2026-11-19 | **K. 사용자 관리 ADMIN 전용 권한** | commit `8e233cc`. `/users/**` → `hasAuthority("ADMIN")`, `error/403.html` 추가. 발견: 회원 생성 화면이 권한 값을 브라우저에서 보내 USER도 관리자 계정을 만들 수 있었음 → 이번 변경으로 차단. 덤프 기준 ADMIN 1명·USER 1명 확인(개수만). 테스트 23/23(권한 9개 추가). K-09 첫 시도 실패(테스트 환경에 오류 화면 연결 Bean 없음) → 템플릿 직접 렌더링으로 변경. 대조군: ADMIN 규칙 제거 시 4개 실패 확인 후 원복 |
| 2026-11-19 | **로컬 DB 웹 테스트 ① 접속·기동·비로그인 확인** | 환경: Windows MySQL 8.0.41(포트 3308, 스키마 watchdog/nuri/nuri2). WSL의 `localhost`는 Windows가 아니라 접속 불가, Windows 호스트 주소로는 `root`가 localhost 전용이라 거부 → 사용자 허용 후 WSL 대역 전용 테스트 계정(3개 스키마 권한, 사용자가 먼저 만들어 둔 계정의 비밀번호를 사용자 허용하에 재설정) 사용. 접속 정보는 저장소 밖 `~/.radar-localdb.env`(권한 600)에 두고 환경 변수로 주입 — 사용자가 수정한 `common-data.yml`(로컬 비밀번호 포함)은 commit하지 않음. 사전 점검(조회만): 회원 7명(ADMIN 1, USER 6, roles NULL 0), 비밀번호 7명 모두 BCrypt, STOPSMS=1. **웹 서버 기동 성공**(Java 25.0.5.1, Hikari·Hibernate 초기화, ERROR/WARN 0). 비로그인 HTTP 확인 R-01~R-09 기대대로(로그인 화면+CSRF, 보호 화면·/users 302→login, 토큰 없는 POST 403, 토큰 있어도 비로그인 302, 정적 200, 없는 아이디 로그인 → error=true(실제 JPA 회원 조회 실행), 토큰 없는 로그인 403). Windows 브라우저에서 `http://localhost:8080` 접속 확인. 로그인 후 확인은 테스트 계정 결정 대기 |
| 2026-11-19 | **로컬 DB 웹 테스트 ② 로그인 후 확인·로컬 DB 구조 맞추기** | 사용자가 브라우저로 `test` 계정 로그인(13:05) → **로그인 성공**(Security 7 + 실제 DB 회원 조회 + BCrypt 확인). 직후 메인 화면 500: `Unknown column 'sd1_0.agency_cd'` (`MenuService.getStationList` ← `MenuController.main`). 원인: 로컬 DB가 기후부(MCEE) 기능 이전 구조 — 엔티티 대비 컬럼 8개 없음(`station_rdr` agency_cd·style_attr·status, `receive_condition`·`station_status`·`sms_target_group_link` agency_cd, `sms_target_group` agency_cd·gubun). 업그레이드 전 코드(`b250e18`)도 같은 컬럼을 쓰므로 **Java 25/Boot 4 문제 아님**. 처음에 "로컬 DB = 2026-07-16 덤프"로 잘못 추정했으나, 덤프에는 8개 컬럼과 기후부 데이터가 이미 있음을 확인(덤프 구조 ↔ 엔티티 누락 0건) → 로컬 DB는 덤프보다 이전 상태. 사용자 허용하에 로컬 DB 수정(접속 주소가 로컬 DB가 아니면 중단하는 실행기 사용): ① 컬럼 8개 추가(저장소 `Query/기후부 등록 query.sql`, `note/그룹 감시자료 설정.sql`의 ALTER와 같은 정의) ② 기존 행 agency_cd='KMA', 문자 그룹 gubun(지점 구분값, 묶음 그룹 0), 지도 좌표 style_attr ③ 사용자 요청으로 덤프에서 **기상청B(MCEE) 18개 지점**(station_rdr 18·station_status 10·receive_condition 18·sms_target_group 10·link 3행)과 **기상청A 제주공항 JIA(gubun 3, TDWR)**(각 1행, link 2행) 추가 — 모두 rollback 시험 실행으로 건수 확인 후 한 트랜잭션으로 반영. 회원·수신 기록·문자 수신자는 개인정보/운영 기록이라 가져오지 않음. 전부 `docs/verify/localdb_schema_sync.sql`로 정리(INSERT IGNORE, 다시 실행해도 0건 — 재실행으로 확인, 되돌리기 SQL 포함). 수정 후 엔티티 ↔ 로컬 DB 누락 컬럼 0건. 로그: 13:05 오류 1건 이후 **ERROR/Exception 0건**, 메인·관측소(KSN) 화면 30초 자동 갱신 정상. 수신 표가 빈 것은 로컬 DB에 오늘 수신 기록이 없어서(Daemon 미실행)이며 정상. WARN: Thymeleaf `Deprecated unwrapped fragment expression`(`layout/default_layout` 10·17행, 화면마다 2건) — 동작 영향 없음, 다음 Thymeleaf에서 제거 예정이라 `~{...}` 형식으로 정리 권장(미적용). 보안 발견: `note/그룹 감시자료 설정.sql` 1~2행에 DB root 비밀번호 평문(Git 추적 중) → S3에 추가 |
| 2026-11-19 | 문서 보강 | 처음 보는 사람을 위한 0장(현재 상태, 용어, 환경 준비, 빌드·테스트 방법, 자주 나는 오류, commit 목록)과 8장(DB·서버가 필요한 남은 작업 따라하기) 추가. 6.8-C/F/S5/G/J 상세 기록 |

---

## 8. 남은 작업 따라하기 (DB·서버가 필요한 작업)

> 이 장은 아직 진행하지 않은 작업의 **절차서**다. 진행한 뒤에는 결과를 7장(작업 이력)에 기록한다.

### 8.1 DB 연동 테스트 환경 준비 — ⚠️ 먼저 꼭 읽기

**절대 하지 말 것**
- **운영 DB로 테스트하지 않는다.** Daemon은 테스트 모드에서도 수신 이력(`receive_data`)과 상태(`receive_condition`)를 기록하고, 문자 큐(`nuri2.NURI2_NRMSG_DATA`)에 행을 넣는다. 누리2 에이전트가 연결된 DB라면 **실제 문자가 발송된다**
- `common-data.yml`에 개발 DB 비밀번호를 적어 commit하지 않는다 (아래 환경 변수 방식 사용)

**① 개발용 MySQL 준비**
- 쿼리에서 스키마 이름을 직접 쓰므로(`nuri.NURI_MSG_DATA`, `nuri2.NURI2_NRMSG_DATA` 등) 개발 DB에도 **`watchdog`, `nuri`, `nuri2` 스키마를 같은 이름으로** 만들어야 한다
- `Query/dump/`의 덤프로 복원할 수 있다 (`dump-watchdog-*.sql`, `dump-nuri-*.sql`, `dump-nuri2-*.sql`). 덤프에는 회원·연락처 등 실제 데이터가 들어 있으므로 개발 DB 접근을 제한한다
  ```bash
  # 예시 (개발용 MySQL에서)
  mysql -u root -p < Query/dump/dump-watchdog-202607161334.sql
  mysql -u root -p < Query/dump/dump-nuri-202607161334.sql
  mysql -u root -p < Query/dump/dump-nuri2-202607161334.sql
  ```
  (덤프 파일에 `CREATE DATABASE`/`USE` 문이 없으면 스키마를 먼저 만들고 `mysql -u root -p <스키마명> < 덤프파일`로 복원)

- (2026-11-19 추가) **로컬 DB가 지금 소스보다 예전 구조이면** 로그인 후 `Unknown column ... agency_cd` 오류가 난다. 덤프로 다시 복원하거나, `docs/verify/localdb_schema_sync.sql`(컬럼 8개 추가 + 기상청B 18개 지점·제주공항 추가, 다시 실행해도 안전)을 개발 DB에서 실행한다. **운영 DB에는 실행하지 않는다**

**② 문자 발송 끄기 (개발 DB에서)**
- Daemon은 `sms_send_onoff` 테이블의 `code='STOPSMS'` 행이 `value=1`일 때만 문자를 보낸다 (`StepTwoService.java:265`)
  ```sql
  UPDATE watchdog.sms_send_onoff SET value = 0 WHERE code = 'STOPSMS';   -- 문자 발송 OFF
  ```
- 그리고 개발 DB에는 누리2 에이전트를 연결하지 않는다 (이중 안전장치)

**③ DB 접속 정보는 환경 변수로 넣기** (파일 수정 없음)
- `DataSourceConfig`는 `config.datasource.*` 값을 읽는다. Spring은 환경 변수 `CONFIG_DATASOURCE_URL`처럼 점(.)을 밑줄(_)로, 소문자를 대문자로 바꾼 이름도 같은 값으로 인식하며, 이 값이 yml보다 우선한다
  ```bash
  export CONFIG_DATASOURCE_URL='jdbc:mysql://<개발DB주소>:3306/watchdog?characterEncoding=UTF-8'
  export CONFIG_DATASOURCE_USERNAME='<개발DB계정>'
  export CONFIG_DATASOURCE_PASSWORD='<개발DB비밀번호>'
  ```
- 명령행 인자(`--config.datasource.password=...`)도 가능하지만 `ps` 명령으로 다른 사용자에게 보일 수 있어 권장하지 않는다
- (2026-11-19 확인) 운영과 같은 라이브러리(RDR.jar 내부)로 `StandardEnvironment` + `YamlPropertySourceFactory`(common-data.yml)를 구성하고 환경 변수 `CONFIG_DATASOURCE_URL`, `CONFIG_DATASOURCE_USERNAME`을 지정해 조회 → **환경 변수 값이 yml 값보다 우선 적용됨**을 확인

### 8.2 C 단계 화면 확인 (로그인·CSRF)

**실행**: `./gradlew build` 후, 8.1 환경 변수를 설정한 터미널에서
```bash
java -jar radar-web/build/libs/RADAR_WEB.jar --server.port=8080
```
로그에 `Started RadarWebApplication ... using Java 25.0.5.1`이 나오면 브라우저에서 `http://localhost:8080` 접속

**확인 항목** (자동 테스트 C-01~C-14로 이미 확인된 것은 제외하고, 브라우저에서만 확인 가능한 것 위주)

| # | 할 일 | 기대 결과 | 문제일 때 확인할 것 |
|---|---|---|---|
| 1 | 로그인하지 않고 `http://localhost:8080/manage/station_status` 입력 | 로그인 화면으로 이동 | SecurityConfig |
| 2 | 개발 DB에 있는 실제 계정으로 로그인 | 메인 화면(`/`)과 메뉴 표시 | `watchdog_member` 테이블의 비밀번호가 BCrypt 형식(`$2a$...`)인지 |
| 3 | 브라우저 개발자 도구(F12) → Elements에서 `<meta name="_csrf"` 검색 | 토큰 값이 있는 meta 태그 3개 | `layout/config.html` |
| 4 | 관리 화면 각각에서 **저장/수정/삭제** 1회씩 (아래 화면 목록) | 정상 처리 | F12 → Network에서 해당 요청이 **403**이면 요청 헤더에 `X-CSRF-TOKEN`이 있는지 확인 → 없으면 그 화면이 `common.js`를 불러오지 않는 것 |
| 5 | 검색 폼 제출: 통계(`/stat/{site}`), 관측 이력(`/station/hist/{site}`), 문자 결과(`/manage/sms_send_result`, `/manage/sms_send_result_nuri2`) | 정상 조회 | Network에서 폼 데이터에 `_csrf`가 포함됐는지 |
| 6 | 우측 상단 **Logout** 클릭 | 로그인 화면으로 이동, 뒤로 가기 후 관리 화면 접근 시 다시 로그인 요구 | `layout/header.html`의 logoutForm |
| 6-1 | **USER 계정**으로 로그인 → 주소창에 `/users/admin_user` 입력 (또는 사용자 관리 메뉴 클릭) | "403 - Forbidden" 안내 화면(`error/403.html`) | 기본 오류 화면(Whitelabel)이 나오면 `templates/error/403.html` 배포 여부 확인 |
| 6-2 | **ADMIN 계정**으로 로그인 → 사용자 관리에서 사용자 등록·수정·삭제 1회씩 | 정상 처리 | 403이면 그 계정의 `roles`가 `ADMIN`인지 DB 확인 |
| 6-3 | USER 계정으로 다른 관리 화면(관측소, 문자 설정 등) 저장 | 정상 처리 (ADMIN 전용은 사용자 관리뿐) | |
| 7 | 로그인 후 30분 이상(세션 만료) 방치 → 저장 버튼 | 현재는 로그인 화면 HTML이 응답으로 와서 오류처럼 보일 수 있음 | 필요하면 후속 개선(만료 시 로그인 화면으로 이동하도록) |

**저장/수정/삭제가 있는 관리 화면 목록** (`PostController`의 POST 21개 기준)

| 화면(GET) | 저장 동작(POST) |
|---|---|
| `/manage/station_set_rc` 경고 기준 설정 | `/manage/station_set_rc_modify` |
| `/manage/station_set_rcc` | `/manage/station_set_rcc_modify` |
| `/manage/station_set_rs` 수신 설정 | `/manage/station_set_rs_modify` |
| `/manage/sms_set_msg` 문자 메시지 설정 | `/manage/sms_set_msg_modify` |
| `/manage/station_status` 지점 운영 상태 | `/manage/station_status_modify` |
| `/manage/sms_target_group` 등 문자 대상 그룹 | `/manage/sms_target_group_modify`, `_member_modify`, `_member_unlink`, `_member_addlink`, `_unlink`, `_addlink` |
| `/manage/sms_target_member` 문자 대상자 | `/manage/sms_target_member_modify`, `_delete`, `_add` |
| `/manage/sms_send_onoff` 문자 발송 ON/OFF | `/manage/sms_send_onoff_modify` |
| `/manage/sms_send_nuri2` 문자 발송 | `/manage/sms_send_save_nuri2` (⚠️ 실제 문자 큐 적재 — 개발 DB에서만) |
| `/manage/sms_template` 문자 템플릿 | `/manage/sms_temp_add`, `_modify`, `_delete` |
| `/users/admin_user` 사용자 관리 | `/users/admin_user_create`, `_modify`, `_del` |

### 8.3 D 단계: DB 쿼리(JPA) 검증

**왜 필요한가**: Hibernate가 5.6 → 7.4로 크게 바뀌었다. 이 프로젝트의 `@Query` 43개는 모두 직접 쓴 SQL(native query)이라 문법 영향은 작지만, **조회 결과를 Java 타입으로 바꾸는 방식**(예: 숫자 컬럼이 `BigInteger`/`Long`/`Integer` 중 무엇으로 오는지)이 달라져 실행 시점에 오류가 날 수 있다. 이는 컴파일로는 잡히지 않고 **실제로 쿼리를 실행해야** 드러난다.

**① Web 화면 전체 열어 보기**: 8.2 실행 상태에서 아래 화면을 모두 열고, 서버 로그(`./logs/RADAR_WEB.log`)에 `ERROR`/`Exception`이 없는지 확인
- 메인 `/`, 관측소 `/station/{site}`, 이력 `/station/hist/{site}`, 통계 `/stat/{site}`, 8.2의 관리 화면 전부, 문자 결과 화면 2개, 사용자 관리
- 오류가 나면 로그에서 `Repository` 이름과 메서드를 찾아 해당 `@Query`의 반환 타입을 확인

**② Daemon 테스트 모드 실행** (SFTP 접속 없이 판정·DB 기록 흐름 확인)
- Daemon에는 외부 yml의 `radar.config.global.mode: "test"`일 때 SFTP 대신 지정한 결과를 넣는 기능이 있다 (`StepOneService.java:58`)
- 준비: 작업 폴더를 만들고 `config/application.yml`을 복사한 뒤 아래처럼 수정 (Daemon은 실행 위치 기준 `./config/application.yml`을 찾는다)
  ```yaml
  radar:
    config:
      global:
        mode: "test"
        target_site: "KWK"          # 테스트할 관측소 코드 (비우면 전체)
        recv_condition: "MISS"      # RECV(수신) 또는 MISS(미수신)
        codedtl: "file_no"          # ok, file_no, filesize_no, network_no, network_ok, file_ok, filesize_ok
  ```
- 실행 (8.1의 환경 변수 설정 후)
  ```bash
  cd <작업 폴더>
  java -DDAEMON_TYPE=KMA_RDR -jar <프로젝트>/Daemon/build/libs/RDR.jar
  ```
- 확인
  - 로그(`./logs/RDR.log`): `[⚙️ 실시간 가상 테스트 가드 발동]`, `receive_data insert 결과`, `2번째 프로세스 상태 판정 진입`, `정산 루프 최종 완료`가 순서대로 나오는지, `ERROR` 없는지
  - DB: `watchdog.receive_data`에 `TEST_INJECTED_...` 파일명 행 추가, `receive_condition` 상태 변경
  - 문자: `STOPSMS=0`이면 로그에 `[장애시 문자 발송 기능 off]`. 문자 흐름까지 확인하려면 **누리2 에이전트가 연결되지 않은 개발 DB에서만** `STOPSMS=1`로 바꾸고 `nuri2.NURI2_NRMSG_DATA`에 행이 들어가는지 확인
  - `recv_condition`/`codedtl`을 바꿔 가며(미수신 → 복구) 장애/복구 판정이 기존과 같은지 확인. 실행 중 yml을 고치면 `YmlRefreshManager`가 자동으로 다시 읽는다
- 끝나면 `Ctrl+C`로 종료

**③ `open-in-view` 결정**: 현재 설정이 잘못된 위치에 있어 효과가 없다(6.8-E). 아래처럼 실행해 ①의 화면을 다시 열어 보고 `LazyInitializationException`이 없으면 `common-data.yml`의 설정을 `spring.jpa.open-in-view: false`로 옮긴다
```bash
java -jar radar-web/build/libs/RADAR_WEB.jar --spring.jpa.open-in-view=false
```

### 8.4 SFTP 접속 호환성 확인 (jsch 교체 영향, 6.8-J)

**① 관측소 서버의 SSH 버전 확인** — 관측소에 접속할 수 있는 **운영 서버에서** 관측소마다 실행
```bash
ssh -vv -o BatchMode=yes -p <포트> <계정>@<관측소IP> exit 2>&1 | grep -E "remote software version|kex: algorithm|host key algorithm"
```
- `remote software version OpenSSH_7.4` 처럼 **7.2 이상이면 대부분 문제없음**
- `OpenSSH_5.x`, `6.x` 이거나 SSH가 OpenSSH가 아닌 장비(전용 장비 펌웨어 등)면 접속 실패 가능성이 높다
- (`BatchMode=yes`라서 비밀번호를 묻지 않고 협상 정보만 보고 끝난다)

**② 실제 접속 확인**: 8.3-② 방식에서 `mode: "real"`로 실행하되, **개발 DB의 관측소 목록을 1곳만 활성(status=1)**으로 두고 한 주기 실행 → 로그의 `[{관측소} 접속 유무]: true` 확인

**③ 실패할 경우 대응** (오래된 서버에만 옛 알고리즘을 추가로 허용) — `SftpUtil.open()`의 `session.setConfig(config)` 전에 추가
```java
// 오래된 관측소 서버 호환: 기본값 뒤에 옛 알고리즘을 덧붙임 (보안이 약하므로 필요한 경우에만)
config.put("server_host_key", JSch.getConfig("server_host_key") + ",ssh-rsa");
config.put("PubkeyAcceptedAlgorithms", JSch.getConfig("PubkeyAcceptedAlgorithms") + ",ssh-rsa");
config.put("kex", JSch.getConfig("kex") + ",diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1");
```
- 근본 해결은 관측소 서버의 SSH 업그레이드다. 위 코드는 임시 대응이며 적용 시 7장에 기록한다

### 8.5 운영 배포 절차와 되돌리기(롤백)

**① 배포 전 확인**
- 운영 서버 CPU: `grep -o -m1 avx512vbmi /proc/cpuinfo` (출력되면 Java4Shell 영향 대상 → 업그레이드 시급)
- 현재 Java: `java -version`, 운영 스크립트가 쓰는 Java 경로
- 8.2~8.4 완료 여부
- **ADMIN 계정 확인** (권한 적용 후 사용자 관리는 ADMIN만 가능):
  ```sql
  SELECT roles, COUNT(*) FROM watchdog.watchdog_member GROUP BY roles;   -- ADMIN이 1 이상이어야 함, NULL 행이 없어야 함
  ```
  ADMIN이 없으면 배포 전에 관리자 계정 1개의 `roles`를 `'ADMIN'`으로 지정한다. `roles`가 NULL인 회원은 로그인 시 오류가 날 수 있으므로 `'USER'` 등으로 채운다

**② JDK 25 설치 (운영 서버, RHEL 계열 예시)**
- 배포판 저장소에 `25.0.5.1` 이상이 있는지, **변경 이력에 CVE-2026-114938이 있는지** 확인 후 설치
  ```bash
  sudo dnf install java-25-openjdk-headless          # 패키지 이름은 배포판에 따라 다를 수 있음
  rpm -q --changelog java-25-openjdk-headless | grep CVE-2026-114938
  ls /usr/lib/jvm/                                    # 실제 설치 경로 확인
  ```
- 설치 경로가 `/usr/lib/jvm/java-25-openjdk`가 아니면 스크립트의 `JAVA_HOME` 기본값을 고치거나 `export JAVA_HOME=<경로>` 설정
- **기존 JDK 11은 지우지 않는다** (롤백용)

**③ 백업**
```bash
cp -a /home/watcher/deamon /home/watcher/deamon.bak_$(date +%Y%m%d)
cp -a /home/watcher/radarWebsite /home/watcher/radarWebsite.bak_$(date +%Y%m%d)
```

**④ 교체 순서** (한 번에 바꾸지 않고 하나씩, 각 단계마다 한 주기 이상 지켜본 뒤 다음으로)
1. 관측소가 가장 적은 **TDWR(공항 2곳)** → `./TDWR_process.sh stop` → 새 `TDWR.jar`·새 `TDWR_process.sh` 복사 → `./TDWR_process.sh start`
2. 로그 확인: `Java: ... (25.0.5.1)`, `Starting DaemonApplication ... using Java 25.0.5.1`, 관측소별 `접속 유무: true`, `ERROR` 없음
3. 이상 없으면 SDR → SDR_B → RDR → RDR_B 순으로 같은 방법 반복
4. 마지막으로 Web(`RADAR_WEB_process.sh`) 교체 후 8.2 확인 항목 중 1·2·4·6 재확인
- `stop` 후 `./XXX_process.sh list`로 프로세스가 실제로 종료됐는지 꼭 확인 (6.8-F의 stop 관련 주의)

**⑤ 되돌리기 (문제 발생 시)**
1. `./XXX_process.sh stop` (종료 안 되면 `list`로 PID 확인 후 `kill <PID>`)
2. 백업 폴더의 **예전 jar와 예전 스크립트**를 원래 위치로 복사 (예전 jar는 Java 11용이므로 예전 스크립트(PATH의 java 사용)와 함께 되돌려야 함)
3. `./XXX_process.sh start` 후 정상 동작 확인
4. 원인 파악 후 7장에 기록

### 8.6 결정이 필요한 사항

| # | 항목 | 선택지 | 비고 |
|---|---|---|---|
| 1 | 노출된 비밀번호 교체 (S2~S5) | 관측소 SFTP, DB, GitLab 복구 코드 교체 | **가장 시급**. Git 이력에도 남아 있으므로 교체가 우선, 이력 정리(`git filter-repo`)는 그다음 |
| 2 | 관리 기능 권한 구분 | ✅ **적용함** (`8e233cc`): 사용자 관리(`/users/**`)는 ADMIN만. 추가로 ADMIN 전용으로 할 기능(예: 문자 발송 ON/OFF, 경고 기준 설정)이 있으면 `ADMIN_PATHS`에 경로 추가 | 사용자 관리 메뉴를 USER에게 숨길지도 결정 필요 |
| 3 | `open-in-view` | 8.3-③ 결과에 따라 | |
| 4 | `spring.thymeleaf.cache` | 운영은 `true` 권장 (현재 `false`) | 화면 응답 속도 |
| 5 | SFTP 호스트 키 검증 (S8) | `known_hosts` 준비 후 `StrictHostKeyChecking=yes` | 서버 작업 필요 |
| 6 | 정리 대상 파일 삭제 | `ConfigFileEnum_back.java`, `SmsService.java.back`, `config/application copy.yml`, `config/radar_process.sh`, `libs/`의 옛 jar, 호출되지 않는 코드 | 삭제해도 Git 이력에는 남음 |
| 7 | 기존 버그 수정 | 로그인 실패 메시지 미표시, 스크립트 `start all` 중복 실행 | 6.8-C 참고 / 4.2 참고 |
| 8 | 원격 저장소 push | `ver_java25` 브랜치 push 및 병합 방식 | 8.2~8.4 확인 후 권장 |

---

## 9. 검토자 확인·검증 가이드 — 직접 확인하는 방법

> 이 장은 **작업 결과를 받은 사람이 직접** "무엇이 바뀌었는지 보고, 정말 동작하는지 확인하는" 절차다.
> Claude가 실행한 테스트를 그대로 다시 돌려 볼 수 있도록 명령과 기대 결과를 함께 적었다.
> 각 항목을 확인하면 9.5의 표에 결과를 기록한다.

### 9.1 준비 (처음 한 번)

```bash
cd <프로젝트 폴더>
git checkout ver_java25
git log --oneline b250e18..HEAD      # 이번 작업 commit 목록 (0.7과 같아야 함)
java -version                         # 25.0.5.1 이상
./gradlew build                       # 마지막에 BUILD SUCCESSFUL
```
- `./gradlew build`가 성공하면 **컴파일 + 보안·권한 자동 테스트 23개 + jar 6종 생성**이 모두 통과한 것이다
- 실패하면 0.6의 "자주 만나는 오류" 표 참고

### 9.2 변경 내용 읽는 방법

| 보고 싶은 것 | 명령 / 방법 |
|---|---|
| commit 하나의 변경 내용 | `git show <해시>` (예: `git show b75f60c`) |
| commit 하나가 바꾼 파일 목록만 | `git show --stat <해시>` |
| 파일 하나의 작업 전/후 비교 | `git diff b250e18 -- <파일경로>` (예: `git diff b250e18 -- radar-web/src/main/java/kr/or/kimsn/radar/web/config/SecurityConfig.java`) |
| 작업 전 원본 파일 보기 | `git show b250e18:<파일경로>` |
| Claude가 추가한 주석 전체 | `git grep -n "Claude 주석"` |
| 화면으로 보기 | VS Code/IntelliJ의 Git 히스토리(파일 우클릭 → Git → Show History), 또는 GitHub에 push 후 commit 화면 |

- `b250e18`은 **작업 시작 전 마지막 commit**(기존 작성자의 "dump sql")이다. 모든 비교의 기준점이다
- 검토할 때는 commit을 **오래된 순서**로 보면 흐름을 따라가기 쉽다 (0.7 표 순서)

### 9.3 단계별 확인 체크리스트

각 단계마다 ① **눈으로 확인할 것**(diff 검토 포인트) ② **실행해서 확인할 것**(명령과 기대 결과)으로 나눴다.

#### 9.3.1 A. 빌드 설정 — commit `ade302c`, `47d1b27`

① 눈으로: `git show ade302c -- build.gradle common-data/build.gradle Daemon/build.gradle radar-web/build.gradle`
- [ ] `org.springframework.boot` 버전이 `4.1.2`인가
- [ ] Java 버전이 `JavaLanguageVersion.of(25)`(toolchain)로 바뀌었는가
- [ ] 의존성에 버전 숫자를 직접 적은 곳이 꼭 필요한 것만 남았는가 (json-simple, modelmapper, jsr305, org.json, jsch)
- [ ] `http://` 저장소가 없어졌는가 (S7)
- [ ] devtools가 `developmentOnly`인가 (S6)

② 실행:
```bash
./gradlew :radar-web:dependencies --configuration runtimeClasspath | grep -E "spring-core:|tomcat-embed-core:|spring-security-core:|thymeleaf:|snakeyaml:|log4j-core:|jsch:" | sort -u
```
- 기대: `spring-core:7.0.10`, `tomcat-embed-core:11.0.27`, `spring-security-core:7.1.2`, `thymeleaf:...3.1.5.RELEASE`, `snakeyaml:2.6`(또는 `-> 2.6`), `log4j-core:2.25.5`, `jsch:2.28.8`
```bash
jar tf radar-web/build/libs/RADAR_WEB.jar | grep -c devtools
```
- 기대: `0` (운영 jar에 devtools 없음)
```bash
unzip -p Daemon/build/libs/RDR.jar META-INF/MANIFEST.MF | grep -E "Start-Class|Radar-System-Type|Build-Jdk-Spec"
# unzip이 없으면: mkdir /tmp/mf && cd /tmp/mf && jar xf <프로젝트>/Daemon/build/libs/RDR.jar META-INF/MANIFEST.MF && cat META-INF/MANIFEST.MF
```
- 기대: `Start-Class: kr.or.kimsn.radar.daemon.DaemonApplication`, `Radar-System-Type: RDR`, `Build-Jdk-Spec: 25`

#### 9.3.2 B. javax → jakarta — commit `da91928`, `a0543da`, `a3b58b1`

① 눈으로: `git show --stat da91928` 등. 대부분 **import 줄만** 바뀌었는지 확인
- [ ] `git show a0543da`에서 import 외 코드 변경은 2곳뿐인가: `@EntityScan` import 경로, `Scheduler`의 `nextExecution()`

② 실행:
```bash
grep -rn "^import javax\." --include=*.java . | grep -v "javax.sql"
```
- 기대: **출력 없음** (실제 import 중 `javax.sql.DataSource`만 남아야 함. 주석 처리된 `// import javax...` 3줄은 기존 주석이라 그대로 있음)

#### 9.3.3 C. 로그인·보안 — commit `b75f60c`, `b7e0a7d`, `e5d048f`

① 눈으로: `git diff b250e18 -- radar-web/src/main/java/kr/or/kimsn/radar/web/config/SecurityConfig.java`
- [ ] `PUBLIC_PATHS`(로그인 없이 열리는 경로)가 `/login`, 정적 리소스, 오류 화면뿐인가
- [ ] `.anyRequest().authenticated()`인가 (예전: `permitAll()`)
- [ ] `csrf().disable()`이 없어졌는가
- [ ] `git show b7e0a7d`: `common.js`가 모든 Ajax 요청 헤더와 POST 폼에 토큰을 넣는가, `login.html`이 `th:action`인가, 로그아웃이 POST 폼인가

② 실행 — 자동 테스트:
```bash
./gradlew :radar-web:test
```
- 기대: `BUILD SUCCESSFUL`. 보고서 `radar-web/build/reports/tests/test/index.html`을 브라우저로 열면 **23 tests, 0 failures** 와 C-01~C-14, K-01~K-09 이름이 보임 (각 시나리오 의미는 6.8-C, 6.8-K 표)

③ (선택) 대조군 — 테스트가 진짜로 문제를 잡는지 직접 확인
1. `SecurityConfig.java`에서 `.csrf(csrf -> { })`를 `.csrf(csrf -> csrf.disable())`로, `.anyRequest().authenticated()`를 `.anyRequest().permitAll()`로 **임시로** 바꾼다
2. `./gradlew :radar-web:test` → **7개 실패**해야 정상 (C-01, C-03, C-04, C-05, C-06, C-12, C-14)
3. **반드시 원복**: `git checkout -- radar-web/src/main/java/kr/or/kimsn/radar/web/config/SecurityConfig.java` 후 `git status`에 변경이 없는지 확인

④ 실제 화면 확인: DB가 필요하다 → 8.2

#### 9.3.4 E. 설정 파일 — commit `1f1ae08`

① 눈으로: `git diff b250e18 -- Daemon/src/main/resources/application.yml radar-web/src/main/resources/application.yml common-data/src/main/resources/common-data.yml`
- [ ] 삭제된 키가 6.8-E 표와 같은가 (`use-legacy-processing`, Daemon의 `server.servlet.encoding.*`·devtools)
- [ ] Daemon의 `spring.config.import`가 `classpath:`(별표 없음)인가
- [ ] `common-data.yml`은 **주석만** 추가되고 값은 그대로인가

② 실행 (먼저 `./gradlew build`):
```bash
# (1) 설정 키 정적 검사
python3 -I docs/verify/check_props.py Daemon/build/libs/RDR.jar Daemon/src/main/resources/application.yml
python3 -I docs/verify/check_props.py radar-web/build/libs/RADAR_WEB.jar radar-web/src/main/resources/application.yml
```
- 기대: Daemon은 모든 줄이 `ok`. Web은 `UNKNOWN spring.devtools.livereload.enabled` 1줄만 (운영 jar에 devtools가 없어서 생기는 정상 결과)
```bash
# (2) 설정 로딩 테스트 — 운영과 같은 라이브러리로 yml을 실제로 읽음 (외부 접속 없음)
rm -rf /tmp/rdr && mkdir -p /tmp/rdr && (cd /tmp/rdr && jar xf "$OLDPWD/Daemon/build/libs/RDR.jar")
java -cp "/tmp/rdr/BOOT-INF/classes:/tmp/rdr/BOOT-INF/lib/*" docs/verify/ConfigLoadTest.java "$PWD"
```
- 기대: 마지막 줄 `결과: PASS 53 / FAIL 0`
- 참고: 실행한 폴더에 `logs/` 폴더가 생길 수 있다 (Log4j 초기화. `.gitignore` 대상이라 Git에는 올라가지 않음)
```bash
# (3) 기동 테스트 — 가짜 DB 주소로 실행해 "설정 처리 → DB 접속 단계"까지 가는지 (운영 DB 접속 안 함)
mkdir -p /tmp/boot && cd /tmp/boot
timeout 90 java -jar <프로젝트>/Daemon/build/libs/RDR.jar --config.datasource.url='jdbc:mysql://127.0.0.1:1/none'
grep -E "Starting DaemonApplication|profile is active|Found [0-9]+ JPA|Communications link failure|wildcards" logs/RDR.log
```
- 기대: `Starting DaemonApplication ... using Java 25.0.5.1`, `profile is active: "local-daemon"`, `Found 24 JPA repository interfaces`, 마지막에 `Communications link failure`(가짜 DB라 정상). **`cannot use classpath wildcards`가 나오면 실패**
- ⚠️ `--config.datasource.url` 인자를 빼먹으면 `common-data.yml`의 실제 DB로 접속을 시도하므로 반드시 넣는다

#### 9.3.5 F. 운영 스크립트 — commit `83e3e1a`, `01cf88b`

① 눈으로: `git diff b250e18 -- config/RDR_process.sh`
- [ ] `# /* Claude 주석 (2026-11-19) 시작: ...` ~ `끝` 블록과 `check_java` 호출, `"$JAVA_BIN"` 실행 줄 외에 바뀐 곳이 없는가
- [ ] 6개 스크립트의 추가 블록이 똑같은가 (블록 시작~끝 표시 사이를 비교):
  ```bash
  blk() { sed -n '/Claude 주석 (2026-11-19) 시작: Java 실행 환경/,/Claude 주석 (2026-11-19) 끝: Java 실행 환경/p' "$1"; }
  for f in RDR_B SDR SDR_B TDWR RADAR_WEB; do diff <(blk config/RDR_process.sh) <(blk config/${f}_process.sh) >/dev/null && echo "$f 같음" || echo "$f 다름"; done
  ```
  기대: 5줄 모두 `같음`

② 실행 — 자동 검증 스크립트 (운영 경로를 건드리지 않고 임시 폴더에서 실행 후 정리):
```bash
bash docs/verify/test_process_scripts.sh                    # Ubuntu (JDK: /usr/lib/jvm/java-25-openjdk-amd64)
bash docs/verify/test_process_scripts.sh /usr/lib/jvm/java-25-openjdk   # RHEL 등 경로가 다른 경우
```
- 기대: 스크립트마다 7개 `PASS`(문법, 경로 없음 거부, Java 21 거부, Java 25.0.5 거부, 25.0.5.1 시작, JDK 25로 실행, DAEMON_TYPE 전달), 마지막 줄 `결과: FAIL 0`

③ 운영 서버에서 (배포 시): `./RDR_process.sh version` → Java 버전 표시, `start` → `Java: ... (25.0.5.1)` 표시, `stop` 후 `list`로 **실제 종료 확인** (6.8-F의 stop 주의)

#### 9.3.6 S5. 비밀번호 로그 제거 — commit `3ad5473`

```bash
grep -rnE "log\.[a-z]+\(.*(sitePwd|[Pp]assword)" --include=*.java . | grep -v "^\S*:\s*//"
```
- 기대: `StepOneService.java`의 `"******"` 처리된 줄 1개만
- 실제 동작은 8.3-②(Daemon 테스트 모드) 실행 시 로그의 `[관측소 pwd]: ******` 로 확인

#### 9.3.7 G. 줄바꿈 규칙 — commit `7bb787f`

```bash
git check-attr eol -- gradlew.bat gradlew config/RDR_process.sh
git ls-files --eol -- gradlew.bat gradlew 'config/*.sh'
```
- 기대: `gradlew.bat: eol: crlf`, 나머지 `eol: lf` / `i/lf` (저장소에는 LF로 저장)

#### 9.3.8 J. SFTP 라이브러리 호환성 (점검 결과 재현)

```bash
java -cp libs/jsch-0.1.55.jar docs/verify/JschDefaults.java
java -cp "$(find ~/.gradle/caches -name 'jsch-2.28.8.jar' | head -1)" docs/verify/JschDefaults.java
```
- 기대: 첫 번째(기존)의 `server_host_key`에는 `ssh-rsa,ssh-dss`가 있고, 두 번째(현재)에는 없다 (6.8-J 표와 같음)
- 관측소 서버 확인은 8.4

#### 9.3.9 주석 구분 — commit `ab480ac`

```bash
python3 -I docs/verify/check_comments.py
```
- 기대
  - `[검사 1-a] 표시 없는 추가 주석`: **1건** — `build.gradle`의 `// Java 11 지원하는 안정 버전` (기존 주석을 원래 자리에 복원한 것. `git show b250e18:build.gradle | grep "안정 버전"`으로 원문 확인 가능)
  - `[검사 1-b] 연속 줄`: 여러 줄 Claude 주석의 2번째 줄 이후 (정상)
  - `[검사 2] 사라진 기존 주석`: **0건**
- 눈으로: `git show ab480ac`(기존 주석 복원)와 `git show f277315`(표시 형식 통일)에서 `+` 줄은 `Claude 주석` 표시 또는 기존 주석 복원뿐이고, 코드 줄의 내용은 바뀌지 않았는지

#### 9.3.10 K. 사용자 관리 ADMIN 전용 — commit `8e233cc`

① 눈으로: `git show 8e233cc -- radar-web/src/main/java`
- [ ] `ADMIN_PATHS`가 `/users/**`이고 `.requestMatchers(ADMIN_PATHS).hasAuthority("ADMIN")`가 `anyRequest()` **앞**에 있는가 (순서가 뒤면 적용되지 않음)
- [ ] `hasRole`이 아니라 `hasAuthority`인가 (이유: 6.8-K)
- [ ] `templates/error/403.html`이 `404.html`과 제목·문구만 다른가: `diff radar-web/src/main/resources/templates/error/404.html radar-web/src/main/resources/templates/error/403.html`

② 실행:
```bash
./gradlew :radar-web:test
```
- 기대: 23 tests, 0 failures. 보고서에서 K-01~K-09 확인
- (선택) 대조군: `SecurityConfig.java`의 `.requestMatchers(ADMIN_PATHS).hasAuthority("ADMIN")` 줄 앞에 `//`를 붙이고 테스트 → **K-01, K-02, K-03, K-07 실패**해야 정상 → `git checkout -- radar-web/src/main/java/kr/or/kimsn/radar/web/config/SecurityConfig.java`로 원복

③ 실제 화면: 8.2의 6-1~6-3 (DB 필요), 배포 전 ADMIN 계정 확인 SQL은 8.5

### 9.4 확인 중 문제를 발견했을 때

1. 어떤 명령에서 어떤 결과가 나왔는지(화면 출력 그대로) 기록
2. 해당 commit만 되돌리려면 `git revert <해시>` (이후 commit이 같은 파일을 고쳤다면 충돌할 수 있으니 확인 후 진행)
3. 7장 작업 이력에 "검토 중 발견" 항목으로 기록하고, 다음 작업에서 수정

### 9.5 검토 결과 기록표 (직접 채우기)

| 항목 | 확인일 | 확인자 | 결과 (통과/문제) | 메모 |
|---|---|---|---|---|
| 9.1 `./gradlew build` | | | | |
| 9.3.1 A 빌드 설정 | | | | |
| 9.3.2 B jakarta | | | | |
| 9.3.3 C 보안 (자동 테스트) | | | | |
| 9.3.3-③ C 대조군 (선택) | | | | |
| 9.3.4 E 설정 파일 | | | | |
| 9.3.5 F 운영 스크립트 | | | | |
| 9.3.6 S5 | | | | |
| 9.3.7 G 줄바꿈 | | | | |
| 9.3.8 J jsch 비교 | | | | |
| 9.3.9 주석 구분 | | | | |
| 9.3.10 K ADMIN 권한 | | | | |
| 8.2 화면 확인 (DB 필요) | | | | |
| 8.3 JPA 검증 (DB 필요) | | | | |
| 8.4 SFTP 호환성 (서버 필요) | | | | |
