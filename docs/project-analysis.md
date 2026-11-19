# radar_new 프로젝트 분석 및 작업 이력

> 이 문서는 소스 분석 결과와 이후 작업 이력을 계속 누적 기록합니다.
> 보안상 비밀번호·IP 계정 등 민감값은 이 문서에 기록하지 않습니다.

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
| S1 | 🔴 | 웹 인증 사실상 비활성화: `/**` 및 `anyRequest()` 모두 `permitAll()`, `authenticated()` 전부 주석. 인터셉터 등록도 주석. 비로그인으로 회원 생성/삭제·문자 발송·설정 변경 가능. CSRF도 disable | `radar-web/.../config/SecurityConfig.java`, `WebMvcConfig.java:16` |
| S2 | 🔴 | 관측소 root 계정 등 SFTP 비밀번호가 평문으로 Git 커밋됨 (파일당 약 35건), 동일 비밀번호 다수 관측소 공유 | `config/application*.yml` (4개) |
| S3 | 🔴 | DB 접속 비밀번호 평문 Git 커밋 | `common-data/src/main/resources/common-data.yml`, `Query/DB 접속 정보.sql` |
| S4 | 🔴 | GitLab 복구 코드, DB 전체 덤프 Git 커밋 | `note/gitlab-recovery-codes.txt`, `Query/dump/` |
| S5 | 🟠 | SFTP 비밀번호를 매 주기 로그에 기록 | `Daemon/.../service/StepOneService.java:105` |
| S6 | 🟠 | `spring-boot-devtools`가 `implementation`으로 운영 jar에 포함 | `radar-web/build.gradle` |
| S7 | 🟡 | `common-data/build.gradle`에 HTTP 저장소(`http://apache.org`, `allowInsecureProtocol`) 설정 | `common-data/build.gradle` |

> S2~S4는 Git 이력에 남아 있으므로 파일 삭제만으로 부족 → **비밀번호/복구코드 교체 우선**, 이후 이력 정리(git filter-repo 등) 검토.

### 4.2 구조/코드

- Daemon `bootJar_*` 태스크: `mainClass = RadarDaemonApplication`(존재하지 않음) vs `Start-Class = DaemonApplication` 불일치
- 외부 설정 경로 하드코딩 (`/home/watcher/deamon/...`, `deamon` 오타)
- 주기마다 `newCachedThreadPool()` 신규 생성, `awaitTermination` 없이 고정 대기 후 StepTwo 진행 → SFTP 지연 시 StepOne 완료 전 판정 가능
- 백업 파일 혼재: `ConfigFileEnum_back.java`, `SmsService.java.back`, `config/application copy.yml`
- `jsch` 이중 존재: `com.jcraft:jsch:0.1.55`(build.gradle 사용, 유지보수 중단) / `libs/jsch-0.2.20.jar`(mwiede 포크)

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
- [ ] `.gitignore`에서 `gradle/`, `gradlew` 제외 규칙 정리 (wrapper를 Git에 포함하려면 필요)
- [ ] 운영 서버 CPU의 AVX-512 VBMI 지원 여부 확인 (참고: 개발 PC CPU는 **지원함** → 영향 대상)
- [ ] 운영 JDK 배포판과 정확한 빌드 확인 (`java -version`)
- [ ] (응급) 현재 JDK 11 배포판 패치 빌드 적용 여부 결정
- [ ] 노출된 비밀번호와 GitLab 복구 코드 교체 (S2~S4)
- [x] Spring Boot 목표 버전 확정 → **4.1.2** (2026-11-19 사용자 결정)
- [x] A. Gradle 설정 전환 (설정 단계와 의존성 다운로드 성공, 컴파일은 B 단계 필요)
- [ ] B. javax → jakarta 전환
- [ ] C. SecurityConfig 재작성 및 S1 해결
- [ ] D. JPA 네이티브 쿼리 검증
- [ ] E. 설정 파일 정리
- [ ] F. 운영 스크립트 수정
- [ ] 기능 검증 및 순차 배포

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

다운로드 확인한 주요 버전: Spring 7.0.10, Security 7.1.2, Tomcat 11.0.27, Hibernate 7.4.13, Log4j 2.25.5, SnakeYAML 2.6, Thymeleaf 3.1.5, Jackson 3.1.7 (+ Jackson 2.21.7, Log4j2용), mysql-connector-j 9.7.0, jsch 2.28.8, Lombok 1.18.48

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
