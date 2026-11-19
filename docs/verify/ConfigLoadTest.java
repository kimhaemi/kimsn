import java.io.File;
import java.util.*;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.scheduling.support.CronExpression;

import kr.or.kimsn.radar.data.config.YamlPropertySourceFactory;

/* Claude 주석 (2026-11-19): 새로 작성한 파일 (이 파일의 모든 주석은 Claude 작성)
 *
 * E 단계 설정 로딩 검증 (Spring Boot 4.1.2 / SnakeYAML 2.6 / Log4j 2.25.5 런타임 라이브러리 사용).
 * 비밀번호·IP 등 값은 출력하지 않고 존재 여부/타입 변환만 확인한다.
 *
 * args[0] = 프로젝트 루트
 */
public class ConfigLoadTest {
    static int pass = 0, fail = 0;

    static void check(boolean ok, String msg) {
        if (ok) { pass++; System.out.println("  PASS " + msg); }
        else    { fail++; System.out.println("  FAIL " + msg); }
    }

    public static void main(String[] args) throws Exception {
        String root = args[0];

        // ---------- T2-1. 외부 운영 설정: YmlRefreshManager.reloadYml()과 같은 방식 ----------
        for (String f : List.of("config/application.yml", "config/application_real.yml")) {
            System.out.println("\n[T2-1] " + f + " (YamlPropertySourceLoader → Environment 최상단, Scheduler/StepOneService 조회 키)");
            List<PropertySource<?>> ps = new YamlPropertySourceLoader().load("external-yml", new FileSystemResource(new File(root, f)));
            check(!ps.isEmpty(), "PropertySource 생성 (" + ps.size() + "개 문서)");
            StandardEnvironment env = new StandardEnvironment();
            env.getPropertySources().addFirst(ps.get(0));

            String g = "radar.config.global.";
            check(env.getProperty(g + "mode") != null, "global.mode 존재 (값=" + env.getProperty(g + "mode") + ")");
            check(env.getProperty(g + "PauseTime", Long.class) != null, "global.PauseTime → Long 변환 (" + env.getProperty(g + "PauseTime") + ")");
            check(env.getProperty(g + "sessionTimeOut", Integer.class) != null, "global.sessionTimeOut → Integer 변환");
            check(env.getProperty(g + "connectTimeOut", Integer.class) != null, "global.connectTimeOut → Integer 변환");
            check(env.getProperty(g + "call_from") != null && env.getProperty(g + "template_code") != null, "global.call_from / template_code 존재 (SmsService)");

            for (String type : List.of("KMA_RDR", "KMA_SDR", "KMA_TDWR", "MCEE_RDR", "MCEE_SDR")) {
                String base = "radar.config.paths." + type + ".";
                String cron = env.getProperty(base + "cron-expression");
                boolean cronOk = false;
                try { CronExpression.parse(cron); cronOk = true; } catch (Exception e) { }
                check(cronOk, type + " cron-expression 파싱 (Spring 7 CronExpression) [" + cron + "]");

                // 관측소 키 목록: PropertySource의 키 이름에서 추출
                Set<String> sites = new TreeSet<>();
                String sp = base + "stations.";
                for (String name : ((org.springframework.core.env.EnumerablePropertySource<?>) ps.get(0)).getPropertyNames()) {
                    if (name.startsWith(sp)) sites.add(name.substring(sp.length()).split("\\.")[0]);
                }
                int okCnt = 0;
                for (String site : sites) {
                    String p = sp + site + ".";
                    boolean ok = !env.getProperty(p + "IP", "").isEmpty()
                        && env.getProperty(p + "PORT", Integer.class) != null
                        && !env.getProperty(p + "ID", "").isEmpty()
                        && !env.getProperty(p + "PASSWORD", "").isEmpty()
                        && !env.getProperty(p + "PATH", "").isEmpty()
                        && !env.getProperty(p + "FILE", "").isEmpty()
                        && env.getProperty(p + "file_size_min", Long.class) != null
                        && env.getProperty(p + "file_size_max", Long.class) != null;
                    if (ok) okCnt++; else System.out.println("    누락/변환 실패 관측소: " + site);
                }
                check(okCnt == sites.size() && !sites.isEmpty(),
                    type + " 관측소 " + sites.size() + "곳 모두 IP/PORT(Integer)/ID/PASSWORD/PATH/FILE/file_size_min·max(Long) 조회");
            }
        }

        // ---------- T2-2. common-data.yml: DataSourceConfig의 @PropertySource(YamlPropertySourceFactory) ----------
        System.out.println("\n[T2-2] common-data.yml (YamlPropertySourceFactory → DataSourceConfig @Value 키)");
        PropertySource<?> cd = new YamlPropertySourceFactory().createPropertySource("common-data",
            new EncodedResource(new FileSystemResource(new File(root, "common-data/src/main/resources/common-data.yml"))));
        StandardEnvironment env2 = new StandardEnvironment();
        env2.getPropertySources().addFirst(cd);
        check("com.mysql.cj.jdbc.Driver".equals(env2.getProperty("config.datasource.driver-class-name")), "driver-class-name = com.mysql.cj.jdbc.Driver");
        check(env2.getProperty("config.datasource.url", "").startsWith("jdbc:mysql://"), "url 형식 jdbc:mysql://...");
        check(!env2.getProperty("config.datasource.username", "").isEmpty(), "username 존재");
        check(env2.getProperty("config.datasource.password") != null, "password 존재");
        try { Class.forName(env2.getProperty("config.datasource.driver-class-name")); check(true, "MySQL 드라이버 클래스 로드 (mysql-connector-j 9.7.0)"); }
        catch (ClassNotFoundException e) { check(false, "MySQL 드라이버 클래스 로드"); }

        // ---------- T2-3. 모듈 application.yml: 제거한 키가 없고 필수 키가 남아 있는지 ----------
        for (String f : List.of("Daemon/src/main/resources/application.yml", "radar-web/src/main/resources/application.yml")) {
            System.out.println("\n[T2-3] " + f);
            List<PropertySource<?>> ps = new YamlPropertySourceLoader().load("app", new FileSystemResource(new File(root, f)));
            StandardEnvironment env3 = new StandardEnvironment();
            env3.getPropertySources().addFirst(ps.get(0));
            check(env3.getProperty("spring.config.use-legacy-processing") == null, "spring.config.use-legacy-processing 제거됨");
            check(env3.getProperty("server.servlet.encoding.charset") == null, "server.servlet.encoding.* 없음");
            check(env3.getProperty("spring.config.import[0]", "").contains("common-data.yml"), "spring.config.import → common-data.yml 유지");
            check("classpath:log4j2.yml".equals(env3.getProperty("logging.config")), "logging.config = classpath:log4j2.yml 유지");
            check(env3.getProperty("spring.application.name") != null, "spring.application.name 유지 (" + env3.getProperty("spring.application.name") + ")");
        }

        // ---------- T2-4. log4j2.yml: Log4j 2.25.5 YAML 파서로 구성 생성 ----------
        for (String f : List.of("Daemon/src/main/resources/log4j2.yml", "radar-web/src/main/resources/log4j2.yml")) {
            System.out.println("\n[T2-4] " + f + " (Log4j 2.25.5 ConfigurationFactory)");
            ConfigurationSource src = ConfigurationSource.fromUri(new File(root, f).toURI());
            Configuration c = ConfigurationFactory.getInstance().getConfiguration(new LoggerContext("test"), src);
            check(c != null && c.getClass().getSimpleName().equals("YamlConfiguration"), "YamlConfiguration 생성 (" + (c == null ? "null" : c.getClass().getSimpleName()) + ")");
            c.initialize(); // 플러그인/appender 정의 해석 (start 하지 않음 → 로그 파일 생성 안 함)
            check(!c.getAppenders().isEmpty(), "appender 정의 해석: " + c.getAppenders().keySet());
            check(c.getLoggers().size() > 0, "logger 정의 해석: " + c.getLoggers().keySet());
        }

        System.out.println("\n결과: PASS " + pass + " / FAIL " + fail);
        System.exit(fail == 0 ? 0 : 1);
    }
}
