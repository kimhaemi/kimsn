# radar_new 프로젝트 분석 및 작업 이력

> 이 문서는 소스 분석 결과와 이후 작업 이력을 계속 누적 기록합니다.
> 보안상 비밀번호·IP·계정 등 민감값은 이 문서에 기록하지 않습니다. 명령 예시에서는 `<개발DB주소>`처럼 꺾쇠 괄호로 표시합니다.

---

## 0. 처음 읽는 분을 위한 안내

### 0.1 이 문서는 무엇인가

**한 줄 요약**: 기상 레이더 감시 시스템(`radar_new`)을 보안 취약점이 없는 최신 Java로 옮기는 작업의 **설명서이자 작업 일지**다.

**이 시스템이 하는 일 (쉽게)**

전국의 기상 레이더 관측소는 몇 분마다 관측 자료 파일을 만든다. 이 시스템은 그 파일이 **제때, 정상 크기로** 만들어지는지 계속 확인하고, 문제가 생기면 담당자에게 **문자(SMS)** 를 보낸다. 담당자는 **관리 웹 화면**에서 관측소 상태를 보고 감시 기준과 문자 대상자를 설정한다.

```
 [관측소 서버 34곳]                 [감시 서버]                               [담당자]
  레이더 자료 파일   ──SFTP 확인──▶  Daemon (감시 프로그램 5종)
                                     │  ① 파일이 있나? 크기는 정상인가?
                                     │  ② 연속으로 실패하면 "장애", 회복되면 "복구"
                                     ▼
                                   MySQL DB  ◀──조회·설정──  radar-web (관리 웹)  ◀── 브라우저
                                     │  (수신 이력, 상태, 문자 대상자, 회원)
                                     ▼
                                   문자 큐 테이블 ──▶ 누리2 에이전트 ──▶ 담당자 휴대폰 문자
```

**왜 작업하나**

2026-11-17에 OpenJDK(Java 실행 환경)에서 원격으로 서버를 장악할 수 있는 심각한 취약점(Java4Shell, CVE-2026-114938)이 공개되었다. 이 시스템은 Java 11과 오래된 Spring Boot 2.7로 만들어져 있었는데, 취약점이 고쳐진 Java 25를 쓰려면 프레임워크도 함께 올려야 한다. 그래서 아래 작업을 진행했다.

| 항목 | 작업 전 | 작업 후 |
|---|---|---|
| Java 실행 환경 | OpenJDK 11 | **OpenJDK 25.0.5.1 이상** (취약점 수정 버전) |
| 프레임워크 | Spring Boot 2.7.12 | **Spring Boot 4.1.2** |
| 빌드 도구 | Gradle (버전 불명, wrapper 없음) | **Gradle 9.9.1** (wrapper 포함) |
| 웹 보안 | 로그인 없이 모든 기능 사용 가능, CSRF 방어 꺼짐 | 로그인 필수, CSRF 방어, 사용자 관리는 관리자만 |
| 그 밖의 보안 문제 | 비밀번호 로그 기록 등 8건(S1~S8) | 4건 해결, 4건은 서버·비밀번호 교체 작업으로 남음 |

이 문서는 "**무엇을, 왜, 어떻게 바꿨고, 어떻게 확인했는지**"를 처음 보는 사람도 따라 할 수 있게 기록한다. 실패한 시도와 실수도 숨기지 않고 적었다.

### 0.2 지금 상태 한눈에 보기 (2026-11-19 기준)

| 단계 | 내용 | 상태 | 자세히 |
|---|---|---|---|
| 준비 | 개발 PC에 JDK 25.0.5.1, Gradle 9.9.1 준비 | ✅ 완료 | 0.5 |
| A | 빌드 설정(Gradle) 전환 | ✅ 완료 | 6.8-A |
| B | `javax` → `jakarta` 패키지 전환 | ✅ 완료 | 6.8-B |
| C | 로그인/보안 설정 재작성 + 인증 복구(S1) | ✅ 완료 — 자동 테스트 14개 + 로컬 DB에서 실제 화면 확인 | 6.8-C, 7.2 |
| D | DB 쿼리(JPA) 동작 검증 | ✅ 로컬 완료 — 웹 화면 전체·검색·저장, Daemon 테스트 모드 확인. 업그레이드로 생긴 화면 오류 1건 발견·수정 | 8.3, 7.2.29 |
| E | 설정 파일(yml) 정리 | ✅ 완료 | 6.8-E |
| F | 운영 실행 스크립트 JDK 25 적용 | ✅ 완료 | 6.8-F |
| K | 사용자 관리(`/users/**`) ADMIN 전용 권한 | ✅ 완료 — 자동 테스트 9개 + 실제 화면 확인 | 6.8-K |
| 웹 경고·오류 | Thymeleaf fragment 경고 정리, 문자 결과 화면 표현식 오류 수정 | ✅ 완료 | 7.2.22, 7.2.29 |
| 보안 | S1, S5, S6, S7 해결 / S2~S4(비밀번호 노출), S8(호스트 키) 남음 | 🔶 일부 | 4.1 |
| 배포 | 운영 서버 JDK 25 설치 → 순차 교체 | ⏳ 미진행 — 운영 서버 필요 | 8.5, 9.6 |

> **현재 브랜치 `ver_java25`에만 반영되어 있고, 원격(origin)에는 아직 push하지 않았다.**
> 아직 남은 테스트는 **9.6**에 한 표로 모아 두었다. 로컬에서 할 수 있는 테스트는 2026-11-19에 모두 진행했다 (7.2.29).

### 0.3 읽는 순서 — 목적별

| 나는… | 이 순서로 읽기 |
|---|---|
| **처음 이 프로젝트를 본다** | 0.1 → 1장(개요) → 2장(폴더 구조) → 3장(동작 흐름, 주요 테이블) → 0.4(용어) |
| **무엇이 바뀌었는지 알고 싶다** | 0.2 → 6.2(목표 버전) → 6.8(단계별 변경 내역) → 0.7(commit 목록) |
| **변경 내용을 직접 검증하려 한다** | 0.5(환경 준비) → 0.6(빌드·테스트) → 9장(검증 가이드) → 9.5(결과 기록표) |
| **남은 일을 이어서 하려 한다** | 9.6(남은 테스트 목록) → 8장(절차서) → 8.6(결정 필요 사항) |
| **운영 서버에 배포하려 한다** | 4.1(보안 문제) → 8.5(배포·롤백 절차) → 9.6 #8~#13 |
| **왜 이렇게 했는지 경과가 궁금하다** | 7장(작업 이력: 요약 표 → 상세 기록) |
| **소스의 주석이 누구 것인지 헷갈린다** | 0.8(주석 구분 규칙) |

### 0.4 용어 설명

**Java·빌드 관련**

| 용어 | 뜻 (이 프로젝트에서) |
|---|---|
| JDK / OpenJDK | Java 프로그램을 만들고(컴파일) 실행하는 도구 모음. `java`(실행), `javac`(컴파일) 명령이 들어 있다 |
| LTS | 장기 지원 버전. Java 25는 LTS라 보안 패치를 오래 받을 수 있다 (Java 27은 단기 버전) |
| CVE | 공개된 보안 취약점에 붙는 고유 번호. 예: CVE-2026-114938 |
| RCE | 원격 코드 실행(Remote Code Execution). 공격자가 네트워크로 서버에서 원하는 명령을 실행할 수 있는, 가장 위험한 종류의 취약점 |
| CVSS | 취약점 심각도 점수(0~10). 9점 이상은 Critical(긴급) |
| Gradle | 소스 컴파일, 라이브러리 다운로드, jar 생성을 자동으로 해 주는 빌드 도구 |
| Gradle wrapper (`gradlew`) | 프로젝트에 정해진 Gradle 버전을 자동으로 내려받아 실행하는 스크립트. **Gradle을 따로 설치하지 않아도 된다** |
| jar | Java 프로그램 묶음 파일. `java -jar RADAR_WEB.jar`처럼 실행한다 |
| 의존성(라이브러리) | 프로젝트가 가져다 쓰는 외부 프로그램 조각. 오래된 의존성에 취약점이 많이 있었다 (5장) |
| BOM | Spring Boot가 "서로 호환되는 라이브러리 버전 목록"을 정해 둔 것. 그래서 `build.gradle`에 버전을 일일이 적지 않는다 |
| 멀티모듈 | 한 저장소 안에 여러 프로그램(모듈)이 같이 있는 구조. 여기서는 `common-data`, `Daemon`, `radar-web` 3개 |

**Spring·웹 관련**

| 용어 | 뜻 (이 프로젝트에서) |
|---|---|
| Spring Boot | 이 프로젝트가 사용하는 Java 웹/서버 프레임워크 |
| `javax` → `jakarta` | Java EE가 Jakarta EE로 이름이 바뀌면서 패키지 이름도 바뀜. Spring Boot 3 이상은 `jakarta.*`만 지원 |
| Spring Security | 로그인, 접근 권한, CSRF 방어를 담당하는 라이브러리 |
| CSRF | 사용자가 로그인한 상태를 악용해 다른 사이트가 몰래 요청을 보내는 공격. 막기 위해 서버가 발급한 **토큰**을 요청마다 함께 보내야 한다 |
| Thymeleaf | 웹 화면(HTML)을 서버에서 만들어 주는 템플릿 엔진. `templates/` 폴더의 HTML 파일 |
| fragment | Thymeleaf에서 여러 화면이 같이 쓰는 조각(머리글, 메뉴 등). `th:replace="~{layout/header :: headerFragment}"`처럼 불러온다 |
| Ajax | 화면을 새로 고치지 않고 JavaScript로 서버에 요청을 보내는 방식. 이 프로젝트의 저장 버튼 대부분이 Ajax(`fnCall`)를 쓴다 |
| HTTP 상태 코드 | 200 성공, 302 다른 주소로 이동(예: 로그인 화면), 403 권한 없음/토큰 없음, 404 없는 주소, 500 서버 오류 |
| ADMIN / USER | 회원 권한. DB `watchdog_member.roles` 값. 사용자 관리 화면은 ADMIN만 쓸 수 있다 |
| MockMvc / @WebMvcTest | 실제 서버를 띄우지 않고 웹 요청을 흉내 내서 테스트하는 방법. DB 없이 보안 설정을 검증할 때 사용 |

**DB·운영 관련**

| 용어 | 뜻 (이 프로젝트에서) |
|---|---|
| JPA / Hibernate | Java 객체와 DB 테이블을 연결하는 기술. 이 프로젝트는 대부분 직접 쓴 SQL(native query)을 사용 |
| 엔티티(Entity) | DB 테이블 한 개에 대응하는 Java 클래스. 이 프로젝트에서는 `common-data/.../dto/*Dto.java`가 엔티티 역할 |
| 스키마 | DB 안의 테이블 묶음. 이 프로젝트는 `watchdog`(본 데이터), `nuri`, `nuri2`(문자 발송) 3개를 쓴다 |
| 덤프(dump) | DB 내용을 SQL 파일로 통째로 저장한 것. `Query/dump/`에 있다 |
| Hikari | DB 연결을 미리 만들어 두고 재사용하는 연결 풀(pool) 라이브러리 |
| 환경 변수 | 프로그램 실행 전에 터미널에서 지정하는 값(`export 이름=값`). DB 비밀번호를 파일에 쓰지 않고 넘길 때 사용 |
| SFTP / JSch | 관측소 서버에 접속해 파일을 확인하는 방식(SFTP)과 그 Java 라이브러리(JSch) |
| 누리2(Nuri2) | 문자 발송 에이전트. DB 테이블(`nuri2.NURI2_NRMSG_DATA`)에 행을 넣으면 문자가 발송된다 |
| STOPSMS | `watchdog.sms_send_onoff` 테이블의 문자 발송 스위치. **이름과 달리 `value=1`이면 문자 발송 켜짐, `0`이면 꺼짐** (`StepTwoService.java`의 `onoffDto.getValue() == 1` 조건) |
| 기상청A / 기상청B | 관측소 소속 기관. 기상청A = `KMA`(기상청), 기상청B = `MCEE`(기후부). DB의 `agency_cd` 값 |
| 대형 / 소형 / 공항 | 레이더 구분. DB의 `gubun` 값 1 / 2 / 3. 자료 종류는 대형 `RDR`, 소형 `SDR`, 공항 `TDWR` |
| Daemon 5종 | 레이더 종류·기관별 감시 프로그램: RDR(기상청A 대형), SDR(기상청A 소형), TDWR(공항), RDR_B(기상청B 대형), SDR_B(기상청B 소형) |
| RUN / MRO | 관측소 운영 상태. RUN = 정상 운영, MRO = 점검 중 (관리 화면의 체크박스) |
| WSL | Windows 안에서 리눅스를 쓰는 기능. 이번 작업의 개발 PC 환경. **WSL 안의 `localhost`는 Windows가 아니다** (8.1-④) |

**Git 관련**

| 용어 | 뜻 |
|---|---|
| commit / commit 해시 | Git 변경 기록 한 건과 그 고유 번호(예: `ade302c`). `git show ade302c`로 내용을 볼 수 있다 |
| 브랜치 | 작업 갈래. 이번 작업은 `ver_java25` 브랜치에서 했다 |
| push | 내 PC의 commit을 원격 저장소(GitHub 등)에 올리는 것. **아직 하지 않았다** |
| `b250e18` | 작업 시작 전 마지막 commit. 모든 "작업 전/후" 비교의 기준점 |

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

**commit이란**: 변경 한 묶음에 붙은 기록이다. 단계마다 따로 commit했기 때문에 필요하면 한 단계만 골라 보거나 되돌릴 수 있다. 각 commit은 `git show <해시>`로 변경 내용을 볼 수 있다. 문제가 생기면 `git revert <해시>`로 해당 변경만 되돌릴 수 있다.

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
| `0f0b82e` | 문서 | K 단계 결과(6.8-K), 새 주석 형식(0.8), 검증 가이드·배포 전 ADMIN 확인 |
| `cbf543d` | 문서 | 로컬 DB 웹 테스트 ① 접속·기동·비로그인 보안 확인 |
| `eedd6bf` | 문서·검증 | 로컬 DB 웹 테스트 ② 로그인, 로컬 DB 구조 맞춤 SQL(`docs/verify/localdb_schema_sync.sql`) |
| `d9356a7` | 웹 | Thymeleaf fragment 표현식을 `~{...}` 형식으로 (경고 제거) |
| `2d494fb` | 문서 | 로컬 DB 웹 테스트 ③ 경고 해소·관리자 권한 확인 |
| `853ba0e` | 문서 | 로컬 DB 웹 테스트 ④ 저장 기능(CSRF) 확인 |
| `7963afa` | 문서 | 검토 결과 기록표(9.5), 남은 테스트 목록(9.6) |
| `0e82caa` | 문서 | 처음 보는 사람 기준 문서 전면 보강 |
| `baab8f7` | 문서 | Git 이력 정리(민감값 제거)에 따른 commit 번호 갱신 |
| `83e48c5` | 문서 | reflog 정리 결과 기록 |
| `270d892` | 문서 | 작업 보고서 이미지(PNG)와 원본 HTML |
| `d15ed4c` | 웹 | 문자 결과 화면 2개의 Thymeleaf 3.1 표현식 오류 수정 |

> 목록 확인: `git log --oneline --reverse b250e18..HEAD` 결과와 이 표가 같아야 한다 (이 표를 갱신한 문서 commit은 표를 쓴 뒤에 만들어지므로 맨 마지막에 1개 더 있다).

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

### 0.9 자주 묻는 질문

**Q. 지금 바로 운영 서버에 배포해도 되나요?**
아니요. 개발 PC와 로컬 DB에서 확인할 수 있는 것은 대부분 확인했지만, 운영 환경에서만 확인할 수 있는 항목이 남아 있다. 특히 ① 관측소 서버가 새 SFTP 라이브러리의 암호 방식을 지원하는지(8.4), ② 운영 DB에 ADMIN 계정이 있는지(8.5)를 먼저 확인해야 한다. 남은 항목은 9.6 #8~#13.

**Q. Java만 25로 바꾸고 프레임워크는 그대로 두면 안 되나요?**
안 된다. Spring Boot 2.7과 옛 Lombok은 Java 25로 만든 클래스 파일을 다루지 못해 빌드부터 실패한다 (6.2 마지막 설명).

**Q. DB가 없어도 확인할 수 있는 것은?**
`./gradlew build` 한 번으로 컴파일, 보안·권한 자동 테스트 23개, jar 6종 생성이 확인된다. 설정 파일·운영 스크립트·SFTP 라이브러리 비교는 `docs/verify/`의 스크립트로 DB 없이 확인할 수 있다 (9.3).

**Q. 웹에서 저장 버튼을 눌렀더니 403이 나와요.**
요청에 CSRF 토큰이 빠진 경우다. 그 화면이 공통 틀(`layout/default_layout`)과 `common.js`를 쓰는지 확인한다. 사용자 관리 화면이라면 ADMIN 계정이 아니어서 403이 정상이다 (0.6 오류 표, 6.8-K).

**Q. 로그인하자마자 `Unknown column ... agency_cd` 오류(500)가 나요.**
코드 문제가 아니라 DB가 기후부 기능 이전의 옛 구조인 경우다. 덤프로 다시 복원하거나 `docs/verify/localdb_schema_sync.sql`을 **개발 DB에서** 실행한다 (8.1-①, 7.2.21).

**Q. 테스트하다가 실제 문자가 나가지 않게 하려면?**
① 개발 DB에는 누리2 에이전트를 연결하지 않고, ② `STOPSMS` 행을 `value=0`(꺼짐)으로 둔다. 이름과 달리 `value=1`이 "발송 켜짐"이다 (0.4, 8.1-②). 웹 테스트만 할 때는 문자 발송 화면(`/manage/sms_send_nuri2`)의 저장 버튼만 누르지 않으면 된다.

**Q. 문제가 생기면 어떻게 되돌리나요?**
소스는 `git revert <commit>`으로 단계별로 되돌릴 수 있다 (0.7). 운영 서버는 백업해 둔 예전 jar와 예전 스크립트로 돌아간다 (8.5-⑤). 로컬 DB 변경은 `localdb_schema_sync.sql` 맨 아래의 되돌리기 SQL을 쓴다.

**Q. `Claude 주석`이 붙은 주석은 지워도 되나요?**
실행에는 영향이 없으므로 지워도 동작은 같다. 다만 누가 무엇을 바꿨는지 추적하는 표시이므로, 검토가 끝나기 전까지는 두는 것을 권장한다 (0.8).

---

## 1. 개요

| 항목 | 내용 |
|---|---|
| 목적 | 기상 레이더 관측소 자료 수신 감시 + 장애/복구 SMS 알림 |
| 구성 | Gradle 멀티모듈 3개: `common-data`(공통 DB 계층), `Daemon`(감시 프로그램), `radar-web`(관리 웹) |
| 규모 | Java 소스 105개, 약 8,200줄. Repository(DB 조회 인터페이스) 24개, 직접 쓴 SQL `@Query` 43개 |
| DB | MySQL 8. 스키마 `watchdog`(본 데이터), `nuri`·`nuri2`(문자 발송) |
| 감시 대상 | 레이더 관측소 34곳: 기상청A(KMA) 대형 11·소형 3·공항 2, 기상청B(MCEE) 대형 7·소형 11 |
| Git 브랜치 | `ver_java25` (원격 브랜치: master, dev, backup, radar_new, ver_java25) |

**기술 스택 — 작업 전과 후**

| 구분 | 작업 전 (`b250e18`) | 작업 후 (현재) |
|---|---|---|
| Java | 11 (`sourceCompatibility = '11'`) | 25 (Gradle toolchain) |
| Spring Boot | 2.7.12 | 4.1.2 |
| Spring Framework / Security | 5.3.27 / 5.7.8 | 7.0.10 / 7.1.2 |
| JPA 구현체 (Hibernate) | 5.6.15 | 7.4.13 |
| 웹 서버 (Tomcat) | 9.0.75 | 11.0.27 |
| 화면 템플릿 (Thymeleaf) | 3.0.15 | 3.1.5 |
| 로그 (Log4j2) | 2.17.2 | 2.25.5 |
| SFTP (JSch) | com.jcraft 0.1.55 (유지보수 중단) | com.github.mwiede 2.28.8 |
| DB 드라이버 | mysql-connector-j 8.0.33 | 9.7.0 |

> 참고: 브랜치 이름은 처음부터 `ver_java25`였지만, 작업 전 빌드 설정은 Java 11이었다.

## 2. 디렉터리 구조

**쉽게 말하면**: `common-data`는 DB를 읽고 쓰는 공통 부품이고, `Daemon`과 `radar-web`이 이 부품을 가져다 쓴다. Daemon은 화면 없이 계속 돌면서 감시하고, radar-web은 사람이 브라우저로 보는 관리 화면이다.

```
radar_new/
├─ common-data/   공통 데이터 계층 (일반 jar — 단독 실행 안 함, 다른 두 모듈이 가져다 씀)
│   ├─ config/     DataSourceConfig (DB 연결, Hikari pool=1), YamlPropertySourceFactory (yml 읽기)
│   ├─ dto/        JPA 엔티티 역할 (*Dto = 테이블 1개) + 복합키 (pkColumn/)
│   ├─ repository/ Spring Data JPA 24개 (SmsSendRepository 592줄이 가장 큼)
│   └─ resources/common-data.yml   DB 접속 정보 (⚠️ 비밀번호 평문 — 보안 S3)
│
├─ Daemon/        감시 프로그램 (같은 소스로 jar 5종: RDR, SDR, TDWR, RDR_B, SDR_B)
│   ├─ DaemonApplication   jar 안 표시(Manifest의 Radar-System-Type)로 로그 파일 이름을 나눔
│   ├─ Scheduler           정해진 주기(크론)마다 관측소별 스레드 실행: StepOne → 20초 대기 → StepTwo
│   ├─ service/
│   │   ├─ StepOneService  SFTP로 관측소 접속 → 파일이 있는지·크기 확인 → receive_data에 기록
│   │   ├─ StepTwoService  연속 실패/회복 횟수로 장애·복구 판정 → receive_condition 갱신
│   │   ├─ SmsService      장애/복구 문자 → 누리2 문자 큐(NURI2_NRMSG_DATA)에 INSERT
│   │   └─ QueryService    Repository 호출 창구
│   ├─ config/
│   │   ├─ YmlRefreshManager     외부 application.yml이 바뀌면 자동으로 다시 읽음 (재시작 불필요)
│   │   └─ RadarConfigProperties 외부 yml 직접 읽기 (SnakeYAML)
│   └─ util/ SftpUtil (SFTP 접속), TimeUtil
│
├─ radar-web/     관리 웹 (Thymeleaf 화면 + Spring Security 로그인, RADAR_WEB.jar)
│   ├─ controller/  화면 8개: 메인·관측소(Menu, Station), 통계(Statistics), 관리(Manage),
│   │               문자(Sms, SmsGroup), 사용자 관리(Members), 로그인(Login)
│   ├─ restApi/     PostController: 설정 변경·회원 생성/수정/삭제·문자 발송 등 POST 21개, GetController
│   ├─ config/      SecurityConfig(로그인·권한), WebMvcConfig, InterceptorConfig, auth/(로그인 처리)
│   └─ resources/templates/  화면 HTML (layout/ 공통 틀, views/ 화면별, error/ 오류 화면)
│
├─ config/   운영 설정: 외부 application*.yml (관측소별 SFTP 접속 정보 — ⚠️ 비밀번호 평문, S2),
│            *_process.sh 운영 실행 스크립트 (start/stop/list/version)
├─ Query/    운영 SQL 모음, DB 전체 덤프 7개 (dump/)
├─ note/     누리2 SDK 압축본/문서, 참고 SQL
├─ libs/     로컬 jar (nuri2 등 Maven에 없는 라이브러리)
└─ docs/     이 문서와 검증 스크립트(verify/) — 이번 작업에서 추가
```

## 3. 동작 흐름

### 3.1 Daemon (감시 프로그램)

1. **시작**: 운영 스크립트가 `java -DDAEMON_TYPE=KMA_RDR -jar RDR.jar`처럼 실행한다. `DAEMON_TYPE`으로 기관(KMA / 이름에 MCEE가 있으면 MCEE)과 레이더 구분(1 대형 / 2 소형 / 3 공항)이 정해진다
2. **주기 실행**: 외부 yml의 `radar.config.paths.<TYPE>.cron-expression` 주기(기본값 `30 * * * * *` = 매분 30초)마다 아래를 반복한다
3. **StepOne — 확인**: DB에서 감시할 관측소 목록을 읽고, 관측소마다 스레드를 0.5초 간격으로 띄워 SFTP로 접속한다. 그 시각의 자료 파일이 있는지, 크기가 기준 범위인지 확인해 `receive_data`에 기록한다
4. **StepTwo — 판정**: 경고 기준(`receive_condition_criteria`)과 비교해 상태를 정한다. 예: 자료가 연속 N회 미수신이면 `WARN`(경고), 다시 정상이 N회 이어지면 `RETR`(복구), 전 지점이 미수신이면 `TOTA`(네트워크 장애). 결과는 `receive_condition`에 저장
5. **문자**: 상태가 바뀌었고 문자 발송 스위치(`STOPSMS` 행의 `value=1`)가 켜져 있으면, 문자 대상 그룹의 수신자에게 보낼 문자를 `nuri2.NURI2_NRMSG_DATA`에 넣는다. 누리2 에이전트가 이 테이블을 읽어 실제 문자를 보낸다

외부 설정 파일 탐색 순서: `/home/watcher/deamon/config/application.yml` → `../config/` → `./config/` → `./Daemon/config/`

### 3.2 radar-web (관리 웹)

1. 브라우저로 접속하면 **로그인 화면**이 먼저 나온다 (C 단계에서 복구한 동작. 작업 전에는 로그인 없이 전부 열렸다)
2. 로그인하면 **메인 화면**에 관측소 지도와 상태가 나온다. 메인·관측소 화면은 30초마다 자동으로 새로 고친다
3. 관리 메뉴에서 경고 기준, 수신 설정, 관측소 운영 상태(RUN/MRO), 문자 대상 그룹·수신자, 문자 템플릿, 문자 발송 ON/OFF를 바꾼다. 저장 버튼은 Ajax로 `PostController`에 요청한다 (CSRF 토큰 자동 첨부)
4. **사용자 관리**(`/users/**`)는 ADMIN 권한 회원만 쓸 수 있다 (K 단계). USER가 열면 403 화면이 나온다

### 3.3 주요 DB 테이블 (`watchdog` 스키마)

설명은 덤프 파일에 적힌 테이블 설명(COMMENT)과 소스 사용처를 기준으로 했다.

| 테이블 | 하는 일 | 주로 쓰는 곳 |
|---|---|---|
| `station_rdr` | 레이더 지점 정보 (지점 코드, 이름, 구분 `gubun`, 기관 `agency_cd`, 지도 위치 `style_attr`) | 메인 화면 지도·메뉴, Daemon 관측소 목록 |
| `station_status` | 지점의 현재 운영 상태 (RUN 정상 운영 / MRO 점검) | 관리 > 지점 운영 상태 |
| `receive_data` | 자료 수신 확인 기록 (몇 시 자료가 왔는지, 크기) — 가장 큰 테이블 | Daemon StepOne 기록, 관측소·통계 화면 |
| `receive_condition` | 지점·자료 종류별 현재 수신 상태 (정상/경고/복구 등) | Daemon StepTwo 판정 결과, 메인 화면 |
| `receive_condition_criteria` | 경고 기준 (연속 몇 회면 경고·복구인지) | Daemon 판정, 관리 > 경고 기준 설정 |
| `receive_setting` | 자료 수신 처리 설정 | 관측소 화면 (`StationController`) |
| `sms_target_group` / `sms_target_member` | 문자 수신자 그룹 / 수신자 | 관리 > 문자 대상 |
| `sms_target_group_link` / `sms_target_member_link` | 어느 지점 장애를 어느 그룹에 보낼지 / 수신자가 어느 그룹에 속하는지 | Daemon 문자 발송 대상 조회 |
| `sms_send_pattern` | 문자 메시지 형식 | 문자 내용 만들기 |
| `sms_send_onoff` | 문자 발송 스위치 (`STOPSMS`, `value=1`이면 발송) | Daemon, 관리 > 문자 발송 ON/OFF |
| `watchdog_member` | 웹 회원 (아이디, BCrypt 비밀번호, 권한 `roles`) | 로그인, 사용자 관리 |
| `menu` | 웹 메뉴 목록 | 화면 상단 메뉴 |

문자 발송용 `nuri2.NURI2_NRMSG_DATA`(문자 큐)에 행이 들어가면 누리2 에이전트가 실제로 문자를 보낸다. **개발·테스트 DB에서는 누리2 에이전트를 연결하지 않는다** (8.1).

## 4. 발견된 문제점

### 4.1 보안 (긴급)

> 등급: 🔴 긴급(아직 남음) / 🟡 주의(남음) / ✅ 해결. S2~S4는 코드 수정이 아니라 **비밀번호 교체**가 필요한 문제라 개발 작업만으로는 해결되지 않는다.

| # | 등급 | 내용 | 위치 |
|---|---|---|---|
| S1 | ✅ 해결 (`b75f60c`, 자동 테스트 + 로컬 DB 화면 확인 완료) | 웹 인증 사실상 비활성화: `/**` 및 `anyRequest()` 모두 `permitAll()`, `authenticated()` 전부 주석. 인터셉터 등록도 주석. 비로그인으로 회원 생성/삭제·문자 발송·설정 변경 가능. CSRF도 disable | `radar-web/.../config/SecurityConfig.java`, `WebMvcConfig.java:16` |
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

- 방법: Spring Boot 2.7.12 BOM 관리 버전 기준으로 OSV.dev 조회 (점검 당시 개발 PC에 Java/Gradle이 없어 실제 의존성 트리는 확인하지 못함)
- **이 표는 작업 전 점검 결과**다. 오른쪽 끝 열에 작업 후 실제로 들어간 버전을 적었다 (`./gradlew :radar-web:dependencies`, `:Daemon:dependencies`로 확인). **작업 후 버전으로 CVE를 다시 조회하지는 않았다** → 9.6 #14
- 결과: 고유 CVE 약 123건 (Critical 16 / High 45 / Moderate 52 / Low 11)

| 라이브러리 (작업 전) | 주요 이슈 | 조치 버전 | 작업 후 적용 버전 |
|---|---|---|---|
| Tomcat embed 9.0.75 | 42건 (Critical 9, CVE-2025-24813 RCE 등) | 9.0.121 | 11.0.27 |
| Thymeleaf 3.0.15 | Critical 3건 (표현식 샌드박스 우회) | 3.1.5 | 3.1.5 |
| Spring Security 5.7.8 / crypto 5.7.12 | CVE-2023-34034 등 | 5.7.16+ (OSS 지원 종료) | 7.1.2 |
| Spring Framework 5.3.27 | 30건+, 다수 6.2.x에서만 수정 | Java 17 + Boot 3.x 필요 | 7.0.10 |
| SnakeYAML 1.30 | CVE-2022-1471 (RCE) | 2.x | 2.6 |
| Jackson 2.13.5 | 14건 | 2.18.11 | 3.1.7 (Spring용) + 2.21.7 (Log4j2 YAML용) |
| mysql-connector-j 8.0.33 | CVE-2023-22102 | 9.2.0 | 9.7.0 |
| org.json 20230618 | CVE-2023-5072 | 20260719 | 20260814 |
| Log4j2 2.17.2 | Moderate 5건 | 2.25.5 | 2.25.5 |
| commons-lang3 3.14.0 | CVE-2025-48924 | 3.18.0 | 3.20.0 |

- 근본 해결(점검 당시 결론): **Java 17/21 + Spring Boot 3.5.x 이상** 이전 (javax → jakarta 전환 포함) → 실제로는 Java 25 + Spring Boot 4.1.2로 진행
- 작업 후에도 남은 오래된 라이브러리: `json-simple` 1.1.1 (2012년 이후 유지보수 중단, 사용처 1곳 — 6.3-G)
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

> 작업 전 계획 단계의 표다. 실제 적용된 버전은 1장 "기술 스택 — 작업 전과 후"와 6.8-A에 있다.

| 항목 | 작업 전 | 목표 | 비고 |
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
| JPA 네이티브 쿼리 검증 | Repository 24개 | 중 (실행 테스트 필요) |
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
- [x] C 화면 확인: 로컬 DB에서 로그인·화면 조회·저장 기능 확인 (2026-11-19 사용자 확인, 서버 로그 ERROR 0 — 7장 ②~④). 로그아웃·세션 만료(8.2 #6·#7)는 9.6 참고
- [x] S5. SFTP 비밀번호 로그 제거 (`3ad5473`)
- [x] `.gitattributes` 추가 (`7bb787f`)
- [ ] SFTP 접속 호환성 확인: 관측소 서버가 새 jsch의 알고리즘을 지원하는지 (8.4)
- [x] K. 사용자 관리 ADMIN 전용 권한 (`8e233cc`, 테스트 23/23, 대조군 확인)
- [x] K 화면 확인: USER/ADMIN 계정으로 사용자 관리 접근, 403 화면 표시 (2026-11-19 사용자 확인 — 7장 ③)
- [ ] 운영 DB에 ADMIN 계정이 1명 이상 있는지, `roles`가 NULL인 회원이 없는지 확인 (8.5)
- [x] D. JPA 네이티브 쿼리 검증 — 로컬 DB에서 웹 화면 31개·검색 폼·저장, Daemon 테스트 모드 확인 (7.2.29). 문자 결과 화면 템플릿 오류 1건 수정(`d15ed4c`)
- [x] E. 설정 파일 정리 (`1f1ae08`, 검증 완료 → 6.8-E)
- [ ] (D 단계에서) `spring.jpa.open-in-view` 실제 적용 여부 결정 (현재 잘못된 위치에 있어 효과 없음) — `false`로 실행해도 화면 오류 0건 확인(7.2.29), 적용은 결정 대기 (8.6-3)
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

#### C. Spring Security 7 및 S1 인증 복구 (2026-11-19 완료 — 자동 테스트 + 로컬 DB 화면 확인)

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
- **자동 테스트로 확인할 수 없는 것** → 8.2에서 실제 화면으로 확인 필요 (2026-11-19 로컬 DB에서 로그인·저장·조회 확인 완료, 7.2.20~7.2.24. 세션 만료·검색 폼은 9.6)
  - 각 화면의 Ajax(`fnCall`) 저장/삭제가 `common.js`를 통해 토큰을 제대로 보내는지 (브라우저의 JavaScript 동작)
  - 검색 폼(통계, 이력, 문자 결과)이 토큰과 함께 제출되는지
  - 실제 DB 회원 계정으로 로그인
  - 세션 만료 후 Ajax 요청 시 화면 동작 (로그인 페이지 HTML이 돌아와 오류처럼 보일 수 있음)

**참고 / 후속 검토**
- (작성 당시) 권한(Role) 구분은 아직 없음. 로그인한 사용자는 모두 회원 관리(`/users/**`) 등 전체 기능 사용 가능 → **이후 K 단계(`8e233cc`)에서 사용자 관리를 ADMIN 전용으로 적용함** (6.8-K)
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

> 7.1은 한 줄 요약, 7.2는 같은 순서의 상세 기록이다. 새 작업은 두 곳에 모두 추가한다.
> 모든 작업은 2026-11-19에 진행했다. 실패와 실수도 그대로 적었다 (같은 실수를 반복하지 않기 위해).

### 7.1 요약 (진행 순서)

| # | 작업 | 결과 | commit |
|---|---|---|---|
| 1 | 현재 Java 버전·취약점 확인 | 개발 PC에 JDK 없음, 프로젝트는 Java 11 | — |
| 2 | 의존성 CVE 전체 점검 | 약 123건 발견 (5장) | — |
| 3 | 소스 구조 파악, 분석 문서 작성 | 1~4장 작성, 보안 문제 S1~S7 발견 | `3f363cf` |
| 4 | Java 25 업그레이드 계획 | 목표: JDK 25.0.5.1, Spring Boot 4.1.x (6장) | `3f363cf` |
| 5 | 개발 PC에 OpenJDK 25 설치 | 25.0.5.1+2, Java4Shell 패치 포함 확인 | — |
| 6 | Gradle 9.9.1 설치, wrapper 생성 | 성공. 기존 설정으로는 빌드 실패(예상대로) | `ade302c` |
| 7 | A. 빌드 설정 전환 | 설정·다운로드 성공, 컴파일은 B 필요 | `ade302c` |
| 8 | B. javax → jakarta | common-data·Daemon 컴파일 성공 | `da91928` `a0543da` `a3b58b1` |
| 9 | C. 보안 설정 재작성 + 인증 복구(S1) | 전체 빌드 성공 | `b75f60c` `b7e0a7d` `47d1b27` |
| 10 | E. 설정 파일 정리 | 기동 실패 오류 1건 발견·수정 | `1f1ae08` |
| 11 | F. 운영 스크립트 JDK 25 적용 | 6개 모두 통과 (1차에 결함 → 수정) | `83e3e1a` `01cf88b` |
| 12 | S5. 비밀번호 로그 기록 제거 | 해결 | `3ad5473` |
| 13 | C 자동 테스트 14개 | 14/14 통과, 대조군 확인 | `e5d048f` |
| 14 | `.gitattributes` 추가 | 줄바꿈 규칙 고정 | `7bb787f` |
| 15 | jsch 알고리즘 호환성 점검 | 오래된 SSH 서버 접속 실패 가능성 발견, S8 추가 | — |
| 16 | 문서 보강 (0장·8장) | 처음 보는 사람용 안내, 남은 작업 절차서 | `779cdc6` |
| 17 | 주석 구분 표시, 기존 주석 복원 | 실행 코드 변경 0건 | `ab480ac` |
| 18 | 검증 가이드(9장)·검증 스크립트 | 스크립트 3개 추가 | `1aa2ce5` |
| 19 | 주석 표시 형식 변경 (`/* Claude 주석 */`) | 완료 (검사·commit 한 명령 실수) | `f277315` |
| 20 | K. 사용자 관리 ADMIN 전용 | 테스트 23/23, 대조군 확인 | `8e233cc` `0f0b82e` |
| 21 | 로컬 DB 웹 테스트 ① 접속·기동 | 실제 DB로 웹 서버 첫 기동 성공, 비로그인 보안 확인 | `cbf543d` |
| 22 | 로컬 DB 웹 테스트 ② 로그인·DB 구조 맞추기 | 로그인 성공. 로컬 DB가 옛 구조여서 맞춤 | `eedd6bf` |
| 23 | Thymeleaf fragment 경고 정리 | 경고 0건 | `d9356a7` |
| 24 | 로컬 DB 웹 테스트 ③ 경고·관리자 권한 확인 | 통과 | `2d494fb` |
| 25 | 로컬 DB 웹 테스트 ④ 저장 기능 확인 | 통과, 문자 발송 없음 확인 | `853ba0e` |
| 26 | 검토 결과 기록표(9.5), 남은 테스트 목록(9.6) | 확인 명령 재실행, 모두 기대대로 | `7963afa` |
| 27 | 문서 전면 보강 (처음 보는 사람 기준) | 0~3장·7장 다시 작성, 민감값 제거 | `0e82caa` |
| 28 | Git 이력 정리 (민감값 제거) | commit 7개 다시 작성, reflog까지 정리 | `baab8f7` `83e48c5` |
| 29 | 작업 보고서 이미지 작성 | 한 장짜리 보고서 PNG + 원본 HTML | `270d892` |
| 30 | 남은 로컬 테스트 전부 진행 | 8건 완료. 업그레이드로 생긴 화면 오류 1건 발견·수정, 문서의 위험한 예시 수정 | `d15ed4c` (이 commit) |

### 7.2 상세 기록

#### 7.2.1 현재 Java 버전 확인 (#1)
- 개발 PC(WSL)에는 JDK가 설치되어 있지 않았다. 프로젝트 빌드 설정은 Java 11을 대상으로 했다
- Java 11의 취약 버전을 조사했다 (5장 마지막, 6.1)

#### 7.2.2 의존성 CVE 점검 (#2)
- Spring Boot 2.7.12가 관리하는 라이브러리 버전을 기준으로 OSV.dev에서 조회했다
- 고유 CVE 약 123건 (Critical 16 / High 45 / Moderate 52 / Low 11). 이때는 수정하지 않았다 (5장)

#### 7.2.3 소스 구조 파악과 문서 작성 (#3, #4)
- 2~4장(구조, 동작 흐름, 문제점)을 작성하고 보안 문제 S1~S7을 찾았다
- Java4Shell(CVE-2026-114938)을 조사해 목표를 JDK 25.0.5.1 + Spring Boot 4.1.x로 정하고 소스 수정 범위를 정리했다 (6장). 진행 체크리스트(6.6)와 참고 자료(6.7)도 추가
- 문서 commit: `3f363cf`

#### 7.2.4 개발 PC에 OpenJDK 25 설치 (#5)
- Ubuntu 26.04 보안 저장소의 `openjdk-25-jdk-headless 25.0.5.1+2-1~26.04.1`을 설치했다. 관리자 비밀번호가 필요해 사용자가 직접 터미널에서 실행했다
- 패키지 변경 이력에 CVE-2026-114938 수정이 있는 것을 확인했다. `java`/`javac` 25.0.5.1 동작 확인
- 개발 PC CPU가 AVX-512 VBMI를 지원해 Java4Shell 영향 대상이었다
- `JAVA_HOME`을 `~/.bashrc`에 자동으로 넣는 것은 작업 환경의 권한 정책으로 보류했다 (사용자가 직접 설정, 0.5-③)

#### 7.2.5 Gradle 9.9.1 설치와 wrapper 생성 (#6)
- 배포본의 SHA-256을 검증하고 `~/opt/gradle-9.9.1`에 설치했다. 빈 임시 프로젝트에서 wrapper를 만든 뒤 프로젝트에 복사했다 (`gradlew.bat` 교체, `gradlew`·`gradle/wrapper/` 새로 추가)
- `./gradlew --version` → Gradle 9.9.1 / JVM 25.0.5.1
- 기존 `build.gradle`로 `./gradlew help`를 실행하면 **실패(예상한 결과)**: `org/gradle/api/plugins/JavaPluginConvention` 오류. Spring Boot 2.7 플러그인이 Gradle 9에서 삭제된 기능을 쓰기 때문 → A 단계 필요

#### 7.2.6 A. 빌드 설정 전환 (#7, `ade302c`)
- 변경 내용은 6.8-A. `./gradlew help` 성공, 3개 모듈 라이브러리 다운로드 실패 0건
- 컴파일은 예상대로 실패: `package javax.persistence does not exist` 등 → 다음 단계 B
- `.gitignore`에서 `gradle/`, `gradlew` 제외 규칙을 지우고 wrapper를 Git에 포함했다

#### 7.2.7 B. javax → jakarta 전환 (#8)
- 모듈별 commit: common-data `da91928`, Daemon `a0543da`, radar-web `a3b58b1`
- common-data·Daemon 컴파일 성공. radar-web은 SecurityConfig(C 단계)만 남았다
- 같이 고친 Boot 4/Spring 6 변경 2건: `@EntityScan` 패키지 위치, 스케줄러 Trigger 반환형 (6.8-B)

#### 7.2.8 C. 보안 설정 재작성 + 인증 복구 (#9)
- commit `b75f60c`(SecurityConfig), `b7e0a7d`(화면에서 CSRF 토큰 보내기), `47d1b27`(Daemon jar 생성 오류 보완)
- 전체 `assemble`과 Daemon 5종 jar 빌드 성공. 운영 jar에 개발 도구(devtools)가 빠진 것 확인 (S6)
- 이때는 DB가 없어 실제 화면 확인을 못 했다 → 자동 테스트(#13)와 로컬 DB 테스트(#21~#25)로 확인

#### 7.2.9 E. 설정 파일 정리 (#10, `1f1ae08`)
- 지운 것: `use-legacy-processing`(2곳), Daemon의 `server.servlet.encoding.*`·devtools 설정. `common-data.yml`의 효과 없는 jpa 설정에는 주석만 달았다
- **기동 테스트로 실제 오류 1건 발견·수정**: Daemon의 `spring.config.import`에 쓴 `classpath*:` 때문에 Boot 4에서 Daemon이 아예 시작되지 않았다 → `classpath:`로 변경
- 확인: T1 정적 검사(Daemon 문제 키 0건), T2 설정 로딩 53/53 통과, T3 가짜 DB 기동 테스트 통과. 검증 스크립트는 `docs/verify/`에 보관 (6.8-E)

#### 7.2.10 F. 운영 스크립트 JDK 25 적용 (#11)
- commit `83e3e1a`: 6개 스크립트에 `JAVA_HOME` 기본값, `$JAVA_BIN`으로 실행, `check_java`(25.0.5.1 이상인지 검사) 추가
- **실수**: Web 스크립트에 함수 정의가 빠진 채 commit됐다. 테스트 결과를 확인하기 전에 commit을 같은 명령으로 실행했기 때문 → `01cf88b`에서 수정하고, 이후로는 테스트 확인 후 commit하도록 단계를 나눴다
- 최종: 6개 모두 낮은 Java 차단(F1~F3), 정상 기동·`DAEMON_TYPE` 전달(F6) 통과
- 기존 `stop` 로직은 WSL에서 동작하지 않는다 (부모 프로세스 ID 조건). 운영 서버에서 확인 필요 (6.8-F)

#### 7.2.11 S5. 비밀번호 로그 기록 제거 (#12, `3ad5473`)
- 관측소 SFTP 비밀번호를 로그에 쓰던 코드를 `******`로 바꿨다. 다른 곳에서 비밀번호를 출력하는 코드는 없었다
- 운영 서버에 이미 쌓인 로그 정리와 비밀번호 교체는 서버 작업으로 남았다

#### 7.2.12 C 단계 자동 테스트 (#13, `e5d048f`)
- `@WebMvcTest`로 DB 없이 실행되는 보안 테스트 14개 → 14/14 통과
- **대조군**: 보안 설정을 예전처럼 풀면 7개가 실패하는 것을 확인하고 원복 → 테스트가 회귀를 잡아낸다
- `./gradlew build` 전체(컴파일 + 테스트 + jar 6종)가 처음으로 성공

#### 7.2.13 `.gitattributes` 추가 (#14, `7bb787f`)
- `gradlew.bat`은 CRLF, `*.sh`·`gradlew`는 LF, `*.jar`는 바이너리. 다시 정규화(renormalize)해도 바뀐 파일 0개

#### 7.2.14 jsch 알고리즘 호환성 점검 (#15)
- jsch 0.1.55와 2.28.8의 기본 알고리즘을 비교했다. `ssh-rsa`(SHA-1), `ssh-dss`, sha1 키 교환, CBC 암호화가 새 버전 기본값에서 빠졌다
- 오래된 관측소 서버는 접속이 실패할 수 있다. 서버 확인 절차와 대응 코드를 8.4에 정리했다
- 보안 문제 S8(SFTP 서버 신원 확인 생략)을 추가했다

#### 7.2.15 문서 보강 — 0장·8장 (#16, `779cdc6`)
- 처음 보는 사람을 위한 0장(현재 상태, 용어, 환경 준비, 빌드·테스트, 자주 나는 오류, commit 목록)과 8장(DB·서버가 필요한 남은 작업 절차서)을 추가했다. 6.8-C/F/S5/G/J 상세 기록

#### 7.2.16 주석 구분 표시와 기존 주석 복원 (#17, `ab480ac`)
- 사용자 요청: "파일 수정 시 내가 작성한 주석과 구분해서 작성해줘"
- Claude가 쓴 주석 전체에 표시를 붙였다(여러 줄은 시작/끝 표시, 새 파일은 머리말)
- 작업 중 바뀌거나 지워졌던 기존 주석을 원문대로 복원: 주석 처리된 javax import 3줄, SecurityConfig 주석 약 15줄("기존 코드 원문 보존" 블록), build.gradle 계열 6곳, 운영 스크립트 6개의 `#JAVA_HOME` 줄, login.html 1줄
- 실행 코드 변경 0건(diff 분석). 다시 확인: 빌드·테스트 14/14, 스크립트 6개, ConfigLoadTest 53/53, check_props 문제 0, 사라진 기존 주석 0건

#### 7.2.17 검증 가이드(9장)와 검증 스크립트 (#18, `1aa2ce5`)
- 사용자 요청: "내가 어떻게 확인하고 검증해야 하는지도 기재해줘"
- 단계별로 눈으로 볼 곳, 실행 명령, 기대 결과, 기록표를 만들었다
- 손으로 하던 검증을 스크립트로 정리: `test_process_scripts.sh`(운영 스크립트 6개 × 7항목, FAIL 0), `JschDefaults.java`, `check_comments.py`
- 9장의 모든 명령을 문서에 적힌 그대로 실행해 기대 결과와 같음을 확인했다 (이 환경에 `unzip`이 없어 대체 명령도 확인)

#### 7.2.18 주석 표시 형식 변경 (#19, `f277315`)
- 사용자 요청: "명확하게 내가 아닌 것 확인할 수 있게. 예: `/* Claude 주석 */`"
- 모든 Claude 주석을 `/* Claude 주석 (2026-11-19): ... */`로 바꿨다 (yml·sh는 `# /* ... */`, HTML은 `<!-- /* ... */ -->`). `check_comments.py`도 새 표시를 인식하게 수정
- 확인: 빌드·테스트 14/14, 스크립트 FAIL 0, 사라진 기존 주석 0건
- **실수**: 코드 변경 여부 검사와 commit을 한 명령으로 실행해서, 검사가 "코드 변경 파일 3개"를 보고했는데도 commit됐다. 직후 확인하니 2개는 여러 줄 주석의 둘째 줄을 검사가 코드로 오인한 것이고, 1개는 의도한 검사 로직 수정이라 실제 문제는 없었다. 이후 검사와 commit을 분리했다

#### 7.2.19 K. 사용자 관리 ADMIN 전용 (#20, `8e233cc`, 문서 `0f0b82e`)
- `/users/**`를 ADMIN만 쓰게 하고(`hasAuthority("ADMIN")`), 권한 없음 화면 `error/403.html`을 추가했다
- **발견한 문제**: 회원 생성 화면이 권한 값을 브라우저에서 보내서, USER도 스스로 관리자 계정을 만들 수 있었다 → 이번 변경으로 차단
- 덤프 기준 ADMIN 1명·USER 1명 확인(개수만 집계)
- 테스트 23/23(권한 9개 추가). K-09는 첫 시도에 실패(테스트 환경에 오류 화면 연결 Bean이 없음) → 템플릿 직접 렌더링으로 변경
- 대조군: ADMIN 규칙을 빼면 4개 실패 확인 후 원복

#### 7.2.20 로컬 DB 웹 테스트 ① 접속·기동·비로그인 확인 (#21, `cbf543d`)
- **환경**: 사용자 Windows PC의 MySQL 8.0.41 (포트 3308, 스키마 watchdog/nuri/nuri2). 웹만 테스트하고 Daemon은 실행하지 않았다
- **접속 문제와 해결**
  - WSL 안의 `localhost`는 Windows가 아니라서 접속되지 않았다
  - Windows 주소로 바꾸자 MySQL의 `root` 계정이 "로컬에서만 접속 가능"으로 설정돼 있어 거부됐다
  - 사용자 허락을 받아, 사용자가 만들어 둔 **WSL 대역 전용 테스트 계정**(3개 스키마 권한)의 비밀번호를 재설정해 사용했다
  - 접속 정보는 저장소 밖의 파일(본인만 읽기, 권한 600)에 두고 환경 변수로 넘겼다. 사용자가 수정한 `common-data.yml`(로컬 비밀번호 포함)은 commit하지 않았다
  - 방법은 8.1-④에 정리 (실제 주소·계정 이름은 문서에 적지 않음)
- **사전 점검 (조회만)**: 회원 7명(ADMIN 1, USER 6, `roles` 빈 값 0), 비밀번호 7명 모두 BCrypt 형식, 문자 발송 스위치 `STOPSMS` value=1(켜짐 — Daemon을 돌리지 않으므로 웹 테스트에는 영향 없음)
- **웹 서버 기동 성공**: Java 25.0.5.1, DB 연결 풀과 Hibernate 초기화 정상, ERROR/WARN 0
- **비로그인 HTTP 확인 R-01~R-09 모두 기대대로**: 로그인 화면 + CSRF 토큰, 보호 화면·`/users`는 로그인 화면으로 이동, 토큰 없는 POST 403, 토큰 있어도 비로그인이면 로그인 화면으로, 정적 파일 200, 없는 아이디 로그인 → `error=true`(실제 DB 회원 조회 실행), 토큰 없는 로그인 403
- R-10(로그인 안 한 상태의 404)은 테스트 설계 실수: 로그인 전에는 로그인 화면으로 먼저 보내는 것이 맞다
- Windows 브라우저에서 `http://localhost:8080` 접속 확인

#### 7.2.21 로컬 DB 웹 테스트 ② 로그인과 로컬 DB 구조 맞추기 (#22, `eedd6bf`)
- **로그인 성공**: 사용자가 브라우저로 로그인 → Security 7 + 실제 DB 회원 조회 + BCrypt 확인이 실제로 동작
- **문제**: 로그인 직후 메인 화면에서 500 오류 `Unknown column 'sd1_0.agency_cd'` (`MenuService.getStationList` ← `MenuController.main`)
- **원인**: 로컬 DB가 기후부(MCEE) 기능을 넣기 전 구조였다. 엔티티와 비교하니 컬럼 8개가 없었다
  - `station_rdr`: `agency_cd`, `style_attr`, `status`
  - `receive_condition`, `station_status`, `sms_target_group_link`: `agency_cd`
  - `sms_target_group`: `agency_cd`, `gubun`
  - 업그레이드 전 코드(`b250e18`)도 같은 컬럼을 쓰므로 **Java 25/Boot 4 문제가 아니다**
- **정정**: 처음에 "로컬 DB = 2026-07-16 덤프"라고 잘못 추정했다. 덤프를 직접 확인하니 8개 컬럼과 기후부 데이터가 이미 들어 있었다(덤프 구조 ↔ 엔티티 누락 0건) → 로컬 DB는 덤프보다 이전 상태
- **로컬 DB 수정 (사용자 허락 후, 접속 주소가 로컬 DB가 아니면 멈추는 실행기 사용)**
  1. 컬럼 8개 추가 — 저장소의 `Query/기후부 등록 query.sql`, `note/그룹 감시자료 설정.sql`에 있는 ALTER 문과 같은 정의
  2. 기존 행 채우기 — `agency_cd='KMA'`(기존 지점은 모두 기상청), 문자 그룹 `gubun`(지점 구분값, 묶음 그룹은 0), 지도 좌표 `style_attr`
  3. 사용자 요청으로 덤프에서 **기상청B(MCEE) 18개 지점**(station_rdr 18, station_status 10, receive_condition 18, sms_target_group 10, link 3행)과 **기상청A 제주공항 JIA**(공항 gubun 3, TDWR — 각 1행, link 2행) 추가
  - 모두 "넣었다가 되돌리기"로 건수를 먼저 확인한 뒤 한 트랜잭션으로 반영
  - 회원·수신 기록·문자 수신자는 개인정보/운영 기록이라 가져오지 않았다
- 전부 `docs/verify/localdb_schema_sync.sql`로 정리했다 (`INSERT IGNORE`라 다시 실행해도 0건 — 재실행으로 확인, 되돌리기 SQL 포함)
- 수정 후 엔티티 ↔ 로컬 DB 누락 컬럼 0건. 로그: 이후 ERROR/Exception 0건, 메인·관측소 화면 30초 자동 갱신 정상
- 수신 표가 빈 것은 로컬 DB에 오늘 수신 기록이 없어서(Daemon 미실행)이며 정상
- 로그에서 발견: Thymeleaf `Deprecated unwrapped fragment expression` 경고(화면마다 2건) → #23에서 정리
- 보안 발견: `note/그룹 감시자료 설정.sql` 1~2행에 DB root 비밀번호가 평문으로 있고 Git이 추적 중 → S3에 추가

#### 7.2.22 Thymeleaf fragment 경고 정리 (#23, `d9356a7`)
- 경고 위치는 `layout/default_layout.html` 10·17행 두 곳뿐이었다 (전체 템플릿 검색으로 확인)
- `th:replace="layout/config :: configFragment"` → `th:replace="~{layout/config :: configFragment}"`, header도 같은 방식. Thymeleaf 3.1 권장 형식이며 동작은 같다. 기존 📌 주석은 그대로 두고 아래에 Claude 주석을 달았다
- 확인: `./gradlew build` 성공(radar-web 테스트 23/23), 사라진 기존 주석 0건, 새 jar에 수정이 들어간 것 확인 후 웹 서버 재시작(10.0초, ERROR 0)
- 직접 확인하는 법: 로그인 후 아무 화면이나 열고 서버 로그에서 `Deprecated unwrapped`를 검색 → 0건이면 통과

#### 7.2.23 로컬 DB 웹 테스트 ③ 경고 해소·관리자 권한 확인 (#24, `2d494fb`)
- 사용자가 다시 로그인해 메인·수신 기준·통계 화면을 열었다 → `Deprecated unwrapped` **0건**(수정 전에는 화면마다 2건), ERROR/Exception 0건. WARN은 서버 시작 때 `open-in-view` 1건뿐(8.6-3)
- **관리자 권한(K)은 사용자가 브라우저에서 직접 확인 완료**. 같은 시간 서버 로그 ERROR/Exception 0건
- 참고: 권한 없음(403) 거부는 Spring Security가 기본으로 로그를 남기지 않아 로그로는 따로 보이지 않는다

#### 7.2.24 로컬 DB 웹 테스트 ④ 저장 기능(CSRF) 확인 (#25, `853ba0e`)
- **사용자가 브라우저에서 저장 기능 확인 완료** (문자 발송 제외)
- 서버 로그: ERROR/Exception 0건, fragment 경고 0건, CSRF 거부(403)로 저장이 막힌 흔적 없음
- 저장 처리는 애플리케이션이 로그를 남기지 않아 로그로는 건별 확인이 안 된다 → DB 조회로 보조 확인: `station_status`의 점검(MRO) 지점 6곳 중 KSN·MIL·YIT는 덤프에 없는 값(로컬에서 저장된 것으로 보이나, 테스트 전 값을 기록하지 않아 단정하지 않음)
- 문자 큐 `nuri2.NURI2_NRMSG_DATA` 1526행 그대로(문자 발송 없음), `STOPSMS` 변동 없음
- 로그인 실패 6회(관리자 확인 중) — 실패 처리 정상

#### 7.2.25 검토 결과 기록표(9.5)와 남은 테스트 목록(9.6) (#26, `7963afa`)
- 9.5를 채우려고 DB 없이 가능한 확인을 다시 실행: 9.3.1 의존성 버전, 9.3.2 javax 0건, 9.3.4 check_props·ConfigLoadTest 53/0·기동 테스트, 9.3.5 스크립트 FAIL 0, 9.3.6, 9.3.7, 9.3.8 → 모두 기대대로
- 빌드·테스트(23/23)·주석 검사는 같은 날 `d9356a7` 기준 결과, 대조군(9.3.3-③)은 앞선 실행 결과를 사용했다. 화면 확인은 사용자 확인 결과로 기록(확인자 구분)
- 9.3.6 검사 명령 보완: Claude 주석 줄(옛 코드 원문 포함)이 함께 잡혀 2줄이 나오던 것 → `grep -v "Claude 주석"` 추가
- 6.6 체크리스트의 C·K 화면 확인 완료 처리. 9.6에 로컬 7개, 운영 6개 남은 테스트 정리

#### 7.2.26 문서 전면 보강 (#27)
- 사용자 요청: "구체적으로 자세하게 처음 보는 사람도 이해할 수 있게"
- 바꾼 곳
  - 0.1 시스템 그림, 작업 전/후 비교
  - 0.2 최신 상태, 0.3 목적별 읽는 순서
  - 0.4 용어 추가 (STOPSMS, 기상청A/B, RUN/MRO, WSL 등)
  - 0.7 commit 목록 최신화, 0.9 자주 묻는 질문
  - 1~3장: 작업 전/후 기술 스택, 모듈 설명, 웹 동작, 주요 DB 테이블 표
  - 5장 현재 적용 버전 열, 6.2 적용 결과 열
  - 7장을 요약 표와 상세 기록으로 분리
  - 8.1-④ WSL에서 Windows MySQL 접속 방법, 8.2 진행 상황 표시
  - 4.1 등급 설명, 6.8-C 지난 표현 정리
  - 9.6 #14 추가: 작업 후 버전으로 의존성 CVE 다시 조회 (문서를 다시 보다가, 작업 후 재점검을 하지 않은 것을 발견)
- **바로잡은 것**
  - 7장에 적혀 있던 테스트용 주소·계정 이름을 일반 표현으로 바꿨다 (이 문서의 민감값 기록 금지 원칙)
  - Repository 개수: 22개 → 24개 (다시 세어 보니 24개, 2장·6.5)
  - 지난 시점의 표현 갱신: "런타임 검증 대기", "권한 구분은 아직 없음"
- 사실 확인: 기술 스택 버전, 지점 수(덤프 기준 34곳), 테이블 설명(덤프의 COMMENT)을 다시 확인한 뒤 적었다

#### 7.2.27 Git 이력 정리 — 민감값 제거 (#28)
- **왜**: 예전 7장(로컬 DB 웹 테스트 ①)에 테스트용 DB 주소와 계정 이름이 적혀 있었다. #27에서 문서에서는 지웠지만, Git 이력(commit 6개)에는 그 값이 남아 있었다. 아직 push하기 전이라 이력에서도 지웠다
- **방법**
  1. 작업 전 확인: 지울 값은 `docs/project-analysis.md`의 한 줄뿐이고, commit 메시지에는 없음. 원격 브랜치에는 해당 commit이 없음(`git branch -r --contains` 결과 없음)
  2. 백업 브랜치 `backup/before-history-rewrite` 생성, 사용자의 미commit `common-data.yml` 수정은 `git stash`로 보관
  3. `git filter-branch --tree-filter`로 로컬 DB 웹 테스트 ① commit부터 끝까지 다시 작성. 그 한 줄의 주소·계정 이름만 일반 표현("Windows 호스트 주소", "WSL 대역 전용 테스트 계정")으로 바꿈
- **확인**
  - 작업 commit 전체에서 해당 값 0건
  - 다시 쓴 commit마다 바뀐 것은 그 한 줄뿐
  - 마지막 commit의 파일 내용은 다시 쓰기 전과 같음 (`git diff` 차이 0)
  - 그보다 앞의 commit(`0f0b82e`까지)은 번호가 그대로임
- **commit 번호가 바뀜** — 이 문서의 0.7·7장 번호를 새 번호로 고쳤다
  - `0570f9d`→`cbf543d`, `440a9bf`→`eedd6bf`, `90118a5`→`d9356a7`
  - `ffc5f5e`→`2d494fb`, `6434409`→`853ba0e`, `b52705a`→`7963afa`, `d6dc7a0`→`0e82caa`
  - 예전 번호는 이제 브랜치에 없다
- 참고: 예전 commit 안의 문서 본문에는 그 시점의 예전 번호가 그대로 남아 있다 (기록이라 고치지 않음)
- **reflog 정리 (사용자 요청)**
  - 예전 commit은 Git의 복구 기록(reflog)에만 남아 있었다
  - `git reflog expire --expire=now --expire-unreachable=now --all` + `git gc --prune=now`로 완전히 삭제했다 (되돌릴 수 없음)
  - 확인: 예전 commit 7개 모두 `git cat-file -e` 결과 없음. 저장소 전체 파일 객체에서 해당 값 검색 0건. `git fsck` 오류 없음

#### 7.2.28 작업 보고서 이미지 작성 (#29)
- **사용자 요청**: "보고서 형태로 이미지 파일로 만들어줘"
- **파일**
  - `docs/report/upgrade-report-2026-11-19.png`: 2480×4300, 2배 해상도
  - `docs/report/upgrade-report-2026-11-19.html`: 원본. 내용을 고칠 때는 이 파일을 수정한다
- **내용**
  - 상단 요약 카드: Java·Spring Boot 버전, 자동 테스트 23/23, 보안 8건 중 4건 해결
  - 본문 8개 절: 배경과 결론, 단계별 진행 상태, 구성 요소 버전 작업 전·후, 보안 문제 처리 현황, 검증 결과, 발견·조치한 문제, 운영 배포 전 필수 작업, 남은 테스트 14건
  - 숫자는 이 문서의 0.2, 5장, 9.5, 9.6과 같다. 비밀번호·IP·계정 같은 민감값은 넣지 않았다
- **만드는 방법**: WSL에는 이미지 도구가 없어서 Windows의 Chrome을 화면 없이 실행(headless)해 HTML을 PNG로 저장했다. 글꼴은 맑은 고딕
  ```bash
  # HTML을 Windows 폴더로 복사한 뒤 (WSL에서 실행)
  "/mnt/c/Program Files/Google/Chrome/Application/chrome.exe" --headless=new --hide-scrollbars \
    --force-device-scale-factor=2 --user-data-dir="<Windows 임시 폴더>\profile" \
    --window-size=1240,2150 --screenshot="<Windows 임시 폴더>\report.png" "file:///<Windows 경로>/report.html"
  ```
  - `--user-data-dir`로 임시 프로필을 써서 사용자의 Chrome 설정은 건드리지 않았다
  - 작업 후 Windows 임시 폴더는 삭제했다
- **확인**
  - 생성된 이미지를 직접 열어 한글 글꼴, 표, 상태 표시가 깨지지 않는 것을 확인했다
  - 아래 여백만 조정해 다시 생성했다
- 참고: 보고서의 "32개 commit"은 작성 시점 기준이다 (이후 이 commit 등이 더해짐)

#### 7.2.29 남은 로컬 테스트 전부 진행 (#30, `d15ed4c`)
- **사용자 요청**: "남은 테스트 중 로컬에서 할 수 있는 것 진행해줘"
- **진행 방식 (사용자 결정)**
  - 로그인: 임시 회원 2명(USER·ADMIN)을 추가해 자동 확인 후 삭제
  - Daemon: 문자 발송을 끄고(STOPSMS=0) 테스트 후 원래대로 복원
  - 문자 발송 화면: 누리2 에이전트가 없는 것을 확인하고 DB 저장과 로그만 확인
- **준비와 안전 조치**
  - 누리2 프로세스가 WSL·Windows 어디에도 없음을 다시 확인
  - DB를 바꾸는 프로그램은 모두 접속 주소가 로컬 DB가 아니면 멈추게 했다
  - 임시 회원 비밀번호는 무작위로 만들어 본인만 읽을 수 있는 파일에만 두고, 끝난 뒤 파일째 삭제
  - 참고: 이때 회원은 ADMIN 2명, USER 5명이었다 (7.2.20 시점과 다름 — 관리자 권한 확인 중 사용자가 바꾼 것으로 보임)

**결과 요약**

| 9.6 # | 테스트 | 결과 |
|---|---|---|
| 1 | 로그아웃 | 통과: `POST /logout` → `/login?logout`, 이후 관리 화면은 로그인 화면으로, `userId` 쿠키 삭제 |
| 2 | 세션 만료 후 저장 | 동작 확인: 세션 만료를 1분으로 줄여 130초 뒤 Ajax 저장 → **403** (CSRF 토큰이 세션과 함께 사라짐). 데이터는 바뀌지 않음. 화면 이동은 로그인 화면으로. `fnCall`은 오류 문자열만 돌려줘 사용자에게 안내가 없음 → 8.6-9 |
| 3 | 화면 전체 | 31개 화면(관측소: 기상청A 대형·소형·공항, 기상청B 포함) + 404·403 화면. **문자 결과 화면 2개에서 오류 발견 → 수정** (아래) |
| 3 | CSRF meta 태그 | 로그인 후 화면에 3개 |
| 4 | 검색 폼 | 통계, 관측 이력, 문자 결과 2종 8건 통과 (데이터가 있는 달 포함). 토큰 없는 검색은 403 |
| 5 | 문자 발송 화면 저장 | 통과: 가짜 번호(010-0000-0000)로 저장 → 문자 큐에 전송 대기 1행 추가, 로그 정상 → 바로 삭제해 1526행으로 원복 |
| 6 | Daemon 테스트 모드 | 통과: 기상청A 대형 11곳 × 7주기(미수신 5 — 설정 변경이 반영되지 않은 2주기 포함, 수신 2), ERROR 0. 수집 기록, 판정("최종상태 update: 성공"), 복구 이력(RETR) 기록, `[장애시 문자 발송 기능 off]` 확인. 끝난 뒤 테스트 수신 기록 77행 삭제, 수신 상태 72행·STOPSMS=1 원래 값으로 복원 |
| 7 | `open-in-view=false` | 통과: 화면 전체를 다시 열어 `LazyInitializationException` 0건 |
| 14 | 의존성 CVE 재점검 | 실제 빌드에 들어간 111개를 OSV.dev에 조회 → **1건**: `ognl` 3.3.4 (CVE-2026-113730, Moderate, 3.4.13에서 수정) → 8.6-10 |

**발견하고 고친 것 — 업그레이드로 생긴 문제**
- `/manage/sms_send_result`, `/manage/sms_send_result_nuri2` 화면이 그려지다 중간에 끊겼다. 응답 코드는 200이라 눈에 띄지 않았다
- 원인: 템플릿의 `${smsRsultList.content.size == 0}`. Thymeleaf 3.1은 보안을 위해 JDK 내부 클래스(여기서는 수정 불가 리스트)의 멤버 접근을 막는다 (`EL1021E ... Accessing member 'size' is forbidden`). Thymeleaf 3.0에서는 동작했다
- 수정(`d15ed4c`): `${#lists.isEmpty(smsRsultList.content)}`로 바꿨다 (의미 동일). 비슷한 표현식을 전체 템플릿에서 찾았고, 나머지(`links.size`, `tempCodeList.size`, `smsRsultList.size`)는 일반 리스트나 Spring 클래스라 문제없음을 화면으로 확인했다
- 확인: 빌드·테스트 23/23, 사라진 기존 주석 0건, 수정 후 두 화면 GET·POST 모두 오류 0

**발견했지만 고치지 않은 것 — 업그레이드 전부터 있던 문제 (8.6에 결정 사항으로)**
- `/users/joinForm`: 템플릿 파일이 없어 500 (8.6-11)
- 문자 결과(누리2)에서 월별 로그 테이블이 없는 달(로컬 DB의 2026년 1월)을 검색하면 500 (8.6-12)
- Daemon 설정 파일 자동 다시 읽기가 파일 수정 이벤트만 감지 — `sed -i`로 고쳤을 때 반영되지 않음을 확인, 제자리 쓰기로는 즉시 반영 (8.6-13)

**문서에서 바로잡은 것**
- 8.3-②의 예시가 `target_site: "KWK"`였는데, 이렇게 하면 **나머지 관측소는 실제 SFTP로 운영 관측소에 접속한다** (코드 확인). 예시를 빈 값으로 바꾸고 경고와 최소 설정 사용법을 추가했다
- 이번 테스트에 쓴 Daemon 설정(관측소 접속 정보 없음):
  ```yaml
  radar:
    config:
      global: { mode: "test", target_site: "", recv_condition: "MISS", codedtl: "file_no",
                call_from: "0", template_code: "radar_0001", sessionTimeOut: 5, connectTimeOut: 5, PauseTime: 10 }
      paths:
        KMA_RDR: { cron-expression: "5 * * * * *" }
  ```

**테스트 방법에서 배운 것**
- 응답 코드만 보면 화면이 중간에 끊긴 오류를 놓친다 → 화면마다 **서버 로그의 ERROR 증가 여부**와 **`</html>`까지 그려졌는지**를 함께 확인하도록 테스트를 고쳐 다시 돌렸다
- curl은 브라우저와 달리 HTML을 요청하지 않아서, 403·404가 HTML 화면 대신 짧은 JSON으로 왔다. 브라우저처럼 `Accept: text/html`을 보내니 `error/403.html`, 404 화면이 정상으로 나왔다
- OSV.dev 조회는 Python 3.14의 엄격한 인증서 검사(`Missing Authority Key Identifier`)에 막혀, 검증을 끄지 않고 curl로 조회했다
- 서버를 끌 때 `pkill -f`로 이름 패턴을 쓰면 실행 중인 셸까지 종료된다 → PID를 찾아 종료

**정리 확인**
- 임시 회원 0명, 문자 큐 1526행, 수신 기록 행 수 테스트 전과 같음, STOPSMS=1, 실행 중인 Java 프로세스 없음

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

**④ WSL(리눅스)에서 Windows에 설치한 MySQL로 접속할 때** (2026-11-19 이 방법으로 접속 확인)

개발 PC가 Windows + WSL이고 MySQL이 Windows 쪽에 설치돼 있으면, 아래 세 가지 때문에 그냥은 접속되지 않는다.

| 막히는 이유 | 증상 | 해결 |
|---|---|---|
| WSL 안의 `localhost`는 Windows가 아니라 WSL 자신이다 | `Communications link failure` | DB 주소를 **Windows 호스트 주소**로 쓴다 (아래 명령으로 확인) |
| MySQL 계정이 "로컬에서만" 접속 가능하게 만들어져 있다 (예: `root@localhost`) | `Access denied for user ...` | WSL 대역에서 접속할 수 있는 **테스트 전용 계정**을 만든다 (root를 열지 않는다) |
| Windows 방화벽이 MySQL 포트를 막는다 | 접속 시간 초과 | Windows 방화벽에서 해당 포트의 인바운드 허용 (사설망 한정) |

```bash
# 1) WSL에서 Windows 호스트 주소 확인 (WSL 기본 NAT 모드 기준)
ip route show default | awk '{print $3}'
```
```sql
-- 2) Windows의 MySQL Workbench 등에서 (개발 DB에서만!) 테스트 계정 만들기
--    <WSL대역>은 1)에서 나온 주소의 앞 두 자리 + '.%'  (예: 주소가 A.B.C.D 이면 'A.B.%')
CREATE USER '<테스트계정>'@'<WSL대역>' IDENTIFIED BY '<새 비밀번호>';
GRANT ALL PRIVILEGES ON watchdog.* TO '<테스트계정>'@'<WSL대역>';
GRANT ALL PRIVILEGES ON nuri.*     TO '<테스트계정>'@'<WSL대역>';
GRANT ALL PRIVILEGES ON nuri2.*    TO '<테스트계정>'@'<WSL대역>';
```
```bash
# 3) 접속 정보를 저장소 밖 파일에 두고(본인만 읽기), 환경 변수로 넘겨 웹 서버 실행
cat > ~/.radar-localdb.env <<'EOF'
CONFIG_DATASOURCE_URL='jdbc:mysql://<Windows호스트주소>:<포트>/watchdog'
CONFIG_DATASOURCE_USERNAME='<테스트계정>'
CONFIG_DATASOURCE_PASSWORD='<새 비밀번호>'
EOF
chmod 600 ~/.radar-localdb.env

./gradlew :radar-web:bootJar
mkdir -p ~/radar-webrun && cd ~/radar-webrun          # 로그(logs/)가 여기에 생긴다
(set -a; . ~/.radar-localdb.env; set +a; java -jar <프로젝트>/radar-web/build/libs/RADAR_WEB.jar --server.port=8080)
```
- 로그에 `Started RadarWebApplication ... using Java 25.0.5.1`이 나오면 **Windows 브라우저에서 `http://localhost:8080`** 으로 접속한다 (WSL이 Windows의 localhost로 포트를 넘겨 준다)
- 종료: 실행한 터미널에서 `Ctrl+C`
- WSL 네트워크가 mirrored 모드이면 1)의 주소 대신 `localhost`로도 접속될 수 있다
- 이 파일 이름(`.radar-localdb.env`)이 저장소 안에 생기지 않도록 반드시 홈 폴더(`~`)에 만든다. `common-data.yml`을 직접 고쳤다면 그 파일은 commit하지 않는다

### 8.2 C 단계 화면 확인 (로그인·CSRF)

> **진행 상황 (2026-11-19, 로컬 DB)**: 전 항목 확인 완료. #1·#2·#4·#6-1~#6-3은 사용자 브라우저 확인(7.2.20~7.2.24), #3·#5·#6·#7은 자동 확인(7.2.29). #7 세션 만료 후 저장은 403으로 거부됨 — 데이터는 바뀌지 않지만 화면에 안내가 없음 (8.6-9)

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
        target_site: ""             # ⚠️ 반드시 비워 둔다 (아래 경고 참고)
        recv_condition: "MISS"      # RECV(수신) 또는 MISS(미수신)
        codedtl: "file_no"          # ok, file_no, filesize_no, network_no, network_ok, file_ok, filesize_ok
  ```
- ⚠️ **(2026-11-19 확인) `target_site`에 관측소를 지정하면 나머지 관측소는 실제 SFTP 접속을 시도한다.** 코드가 "테스트 모드이면서 대상 관측소일 때만" 가상 결과를 넣고, 그 외 관측소는 운영과 똑같이 접속하기 때문이다 (`StepOneService.java` 55행 조건). 테스트 PC에서 운영 관측소에 접속하지 않도록 **`target_site`는 비워 두고**, 복사한 설정 파일에서 **`stations:` 아래 관측소 접속 정보(IP/ID/PASSWORD)를 지운 최소 설정**을 쓴다 (이번 테스트에 쓴 설정은 7.2.29)
- 자료 시각은 5분 단위로 맞춰지므로(`TimeUtil.getAdjustedCurrentTime`), 1분마다 돌리면 같은 시각 행이 여러 번 쌓인다. 경고(WARN)는 연속 6회(대형 기준)라 상태 변화까지 보려면 30분 이상 걸린다
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
  - ⚠️ 자동 다시 읽기는 **파일 수정(MODIFY) 이벤트만** 감지한다. `sed -i`처럼 새 파일로 바꿔치기하는 방식으로 고치면 반영되지 않는다 (2026-11-19 확인). 반영 여부는 `boot.log`의 `[YmlWatcher] 🔔 ... 변경 감지` 줄로 확인한다
  - 끝나면 테스트 행 정리: `DELETE FROM watchdog.receive_data WHERE file_name LIKE 'TEST\_INJECTED\_%';` 그리고 `receive_condition`·`STOPSMS`를 테스트 전 값으로 되돌린다 (시작 전에 값을 저장해 둘 것)
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
| 9 | 세션 만료 후 저장 안내 (2026-11-19 추가) | 그대로 둠 / `common.js`의 `fnCall` 오류 처리에서 403·로그인 화면 응답이면 "로그인이 만료되었습니다" 안내 후 로그인 화면으로 이동 | 지금은 저장이 조용히 실패함(데이터는 안전) |
| 10 | `ognl` 3.3.4 취약점 CVE-2026-113730 (Moderate) | 그대로 둠 / `build.gradle`에서 `ognl:ognl:3.4.13`으로 강제 후 화면 전체 재확인 | Thymeleaf가 가져오는 라이브러리. Spring 환경에서는 표현식을 SpEL로 처리해 OGNL을 쓰지 않으므로 실제 위험 낮음. 다음 Thymeleaf 업데이트로 해결될 수 있음 |
| 11 | `/users/joinForm` 주소 | 컨트롤러 메서드 삭제 / 템플릿 추가 | 템플릿 파일이 없어 ADMIN이 열면 500 (업그레이드 전부터 있던 문제, 메뉴에서 연결되지 않음) |
| 12 | 월별 문자 로그 테이블이 없는 달 조회 | 그대로 둠 / 테이블이 없으면 "자료 없음" 처리 | `NURI2_NRMSG_LOG_YYYYMM` 테이블이 없는 달을 문자 결과(누리2) 화면에서 검색하면 500. 운영에서는 누리2 에이전트가 월별 테이블을 만든다 |
| 13 | Daemon 설정 파일 자동 다시 읽기 | 그대로 둠(운영 안내) / `ENTRY_CREATE`도 감지하도록 수정 | 파일을 바꿔치기하는 편집 방식이면 반영 안 됨 (8.3-②) |

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
grep -rnE "log\.[a-z]+\(.*(sitePwd|[Pp]assword)" --include=*.java . | grep -v "^\S*:\s*//" | grep -v "Claude 주석"
```
- 기대: `StepOneService.java`의 `"******"` 처리된 줄 1개만
- (2026-11-19 보완) 마지막 `grep -v "Claude 주석"`은 기존 코드를 설명한 Claude 주석 줄(105행, 옛 코드 원문 포함)을 빼기 위한 것. 빼지 않으면 그 주석 줄까지 2줄이 나온다
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

### 9.5 검토 결과 기록표

> 2026-11-19 정리. **확인자**: "Claude" = Claude가 이 문서의 명령을 실행해 확인(개발 PC WSL, JDK 25.0.5.1), "사용자" = 사용자가 브라우저 등에서 직접 확인.
> 다른 검토자가 다시 확인하면 아래에 행을 추가하거나 확인자·확인일을 덧붙인다. 아직 못 한 항목은 9.6에 있다.

| 항목 | 확인일 | 확인자 | 결과 | 메모 |
|---|---|---|---|---|
| 9.1 `./gradlew build` | 2026-11-19 | Claude | 통과 | 전체 빌드 성공 (`d9356a7` 기준) |
| 9.3.1 A 빌드 설정 | 2026-11-19 | Claude | 통과 | Spring Core 7.0.10, Security 7.1.2, Tomcat 11.0.27, Thymeleaf 3.1.5, SnakeYAML 2.6, Log4j 2.25.5, jsch 2.28.8 |
| 9.3.2 B jakarta | 2026-11-19 | Claude | 통과 | `import javax.*`(javax.sql 제외) 0건 |
| 9.3.3 C 보안 (자동 테스트) | 2026-11-19 | Claude | 통과 | radar-web 테스트 23/23 (C-01~C-14 포함) |
| 9.3.3-③ C 대조군 (선택) | 2026-11-19 | Claude | 통과 | 보안 설정을 약하게 바꾸면 7개 실패, ADMIN 규칙 제거 시 4개 실패 → 테스트가 회귀를 잡음 (확인 후 원복) |
| 9.3.4 E 설정 파일 | 2026-11-19 | Claude | 통과 | check_props: Daemon 전부 ok, Web은 `UNKNOWN spring.devtools.livereload.enabled` 1줄(정상). ConfigLoadTest PASS 53/FAIL 0. 기동 테스트: Java 25.0.5.1, `local-daemon`, JPA 24개, 가짜 DB라 `Communications link failure`(정상), wildcards 오류 없음. ① 눈으로 확인 시 주의: 작업 폴더의 `common-data.yml`에는 사용자의 로컬 DB용 수정(commit 안 함)이 있으므로 `git diff b250e18 HEAD -- ...`로 commit된 내용끼리 비교 |
| 9.3.5 F 운영 스크립트 | 2026-11-19 | Claude | 통과 | `test_process_scripts.sh` 결과 FAIL 0 (6개 스크립트 × 7개 확인) |
| 9.3.6 S5 | 2026-11-19 | Claude | 통과 | `******` 처리된 1줄만. 검사 명령에 Claude 주석 줄 제외 조건 보완 |
| 9.3.7 G 줄바꿈 | 2026-11-19 | Claude | 통과 | `gradlew.bat` crlf, `gradlew`·`config/*.sh` lf |
| 9.3.8 J jsch 비교 | 2026-11-19 | Claude | 확인(위험 남음) | 0.1.55는 ssh-rsa·sha1 kex 허용, 2.28.8은 기본값에서 제외 → 오래된 관측소 서버 접속 실패 가능. 실제 접속 확인은 8.4 (9.6) |
| 9.3.9 주석 구분 | 2026-11-19 | Claude | 통과 | 사라진 기존 주석 0건. 표시 없는 추가 주석 3건 중 1건은 `build.gradle`의 기존 주석이 붙은 줄(버전 값만 변경, 주석은 원문), 2건은 사용자의 미commit `common-data.yml` 수정 |
| 9.3.10 K ADMIN 권한 | 2026-11-19 | Claude, 사용자 | 통과 | 자동 테스트 K-01~K-09 통과. 로컬 DB에서 USER 403·ADMIN 접근을 사용자가 브라우저로 확인 |
| 8.2 화면 확인 (DB 필요) | 2026-11-19 | 사용자, Claude | 통과 | 사용자: 로그인·화면·관리자 권한·저장. Claude(임시 회원으로 자동): meta 태그 3개, 로그아웃, 세션 만료(저장 403 거부), 404·403 화면, 문자 발송 화면 저장(가짜 번호, 저장 후 삭제) (7.2.29) |
| 8.3 JPA 검증 (DB 필요) | 2026-11-19 | 사용자, Claude | 통과 (오류 1건 수정) | ① Web 화면 31개 + 검색 폼 8개 + 저장: 문자 결과 화면 2개의 Thymeleaf 3.1 표현식 오류 발견 → `d15ed4c` 수정 후 오류 0. ② Daemon 테스트 모드 7주기 오류 0, 판정·문자 차단 정상. ③ `open-in-view=false`에서도 오류 0 (7.2.29) |
| 8.4 SFTP 호환성 (서버 필요) | | | 미진행 | 운영 서버에서만 가능 (9.6) |
| (추가) 로컬 DB 구조 맞추기 | 2026-11-19 | Claude | 통과 | `docs/verify/localdb_schema_sync.sql` 적용, 엔티티 대비 누락 컬럼 0건, 재실행 시 0건(중복 없음) |

### 9.6 남은 테스트 목록 (2026-11-19 기준, 로컬 테스트 진행 후 갱신)

> 위에서부터 할 수 있는 순서 (#14는 언제든 가능). "어디서"가 **로컬**이면 지금 개발 PC와 로컬 DB로 가능, **운영**이면 운영 서버나 운영 DB가 있어야 한다.

| # | 테스트 | 어디서 | 방법 (절) | 통과 기준 | 주의 | 상태 |
|---|---|---|---|---|---|---|
| 1 | 로그아웃 | 로컬 | 8.2 #6 | 로그인 화면으로 이동, 뒤로 가기 후 관리 화면 접근 시 다시 로그인 요구 | | ✅ 완료 (7.2.29) |
| 2 | 세션 만료 후 저장 | 로컬 | 8.2 #7 | 현재 동작 기록(로그인 화면 HTML이 응답으로 옴) → 개선 여부 결정 | 30분 이상 대기 | ✅ 완료 — 403 거부, 안내 없음 (8.6-9) |
| 3 | 나머지 화면 전부 열기 | 로컬 | 8.3-① 화면 목록 (이력 `/station/hist/{site}`, 문자 결과 2개, 문자 템플릿·대상자·그룹 등) | 서버 로그 ERROR/Exception 0 | 연 화면을 이 표나 7장에 적어 둔다 | ✅ 완료 — 31개, 오류 1건 수정 |
| 4 | 검색 폼 제출 | 로컬 | 8.2 #5 | 통계·이력·문자 결과 검색 정상 | | ✅ 완료 — 8건 |
| 5 | 문자 발송 화면 저장 | 로컬 (누리2 에이전트 없는 DB만) | 8.2 목록의 `/manage/sms_send_save_nuri2` | `nuri2.NURI2_NRMSG_DATA`에 행 추가, 오류 없음 | ⚠️ 에이전트가 연결된 DB면 **실제 문자 발송**. 로컬 DB에 에이전트 없음 확인 후 진행, 끝나면 추가된 행 삭제 | ✅ 완료 — 저장 후 삭제 |
| 6 | Daemon 테스트 모드 | 로컬 | 8.3-② | 판정·DB 기록 흐름 로그, ERROR 0 | Daemon은 `receive_data`·`receive_condition`에 기록함. `STOPSMS`가 1이면 문자 큐에 행이 들어가므로 먼저 0으로 바꾸거나 에이전트 없음 확인 | ✅ 완료 — 7주기, 원복 |
| 7 | `open-in-view` 끄고 화면 확인 | 로컬 | 8.3-③ | `LazyInitializationException` 없음 → 설정 위치 수정 여부 결정 | | ✅ 완료 — 오류 0 |
| 8 | 관측소 SSH 버전 확인 | 운영 | 8.4-① | 관측소마다 OpenSSH 7.2 이상 | 7.2 미만이면 8.4-③ 대응 필요 | ⏳ 운영 필요 |
| 9 | SFTP 실제 접속 | 운영 | 8.4-② | 로그 `접속 유무: true` | 관측소 1곳만 활성화하고 시작 | ⏳ 운영 필요 |
| 10 | 운영 사전 확인 | 운영 | 8.5-① | CPU `avx512vbmi` 여부 기록, 현재 `java -version` 기록, 운영 DB ADMIN 1명 이상·`roles` NULL 0 | ADMIN이 없으면 배포 후 아무도 사용자 관리 불가 | ⏳ 운영 필요 |
| 11 | 운영 JDK 25 설치·경로 | 운영 | 8.5-② | `25.0.5.1` 이상, 변경 이력에 CVE-2026-114938, 경로가 스크립트 기본값과 같음 | JDK 11은 지우지 않음(롤백용) | ⏳ 운영 필요 |
| 12 | 순차 배포 | 운영 | 8.5-④ | TDWR → SDR → SDR_B → RDR → RDR_B → Web 순, 단계마다 한 주기 이상 정상 | 문제 시 8.5-⑤로 되돌리기 | ⏳ 운영 필요 |
| 13 | 배포 후 Web 재확인 | 운영 | 8.2 #1·#2·#4·#6 | 정상 | | ⏳ 운영 필요 |
| 14 | 작업 후 의존성 CVE 재점검 | 로컬 (인터넷) | 5장 방법으로 `./gradlew :radar-web:dependencies`, `:Daemon:dependencies` 결과의 버전을 OSV.dev에서 다시 조회 | 남은 Critical/High 없음, 있으면 조치 버전 기록 | 5장 표는 작업 전 버전 기준이다 | ✅ 완료 — 111개 중 1건 (8.6-10) |

**테스트는 아니지만 배포 전에 할 일** (8.6)
- 노출된 비밀번호 교체 (S2~S4, `note/그룹 감시자료 설정.sql`의 DB root 비밀번호 포함) — 가장 시급
- 결정 대기: 사용자 관리 메뉴를 USER에게 숨길지, ADMIN 전용으로 더 막을 경로, `spring.thymeleaf.cache`, 정리 대상 파일 삭제, push 방식
