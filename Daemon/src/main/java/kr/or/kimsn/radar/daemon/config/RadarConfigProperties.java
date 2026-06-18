package kr.or.kimsn.radar.daemon.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "radar.config")
public class RadarConfigProperties {

  private final Environment environment;

  public RadarConfigProperties(Environment environment) {
    this.environment = environment;
  }

  private String siteInfo;
  private String ipInfo;
  private String cronExpression = "0 * * * * *";

  public String getSiteInfo() { return siteInfo; }
  public void setSiteInfo(String siteInfo) { this.siteInfo = siteInfo; }
  public String getIpInfo() { return ipInfo; }
  public void setIpInfo(String ipInfo) { this.ipInfo = ipInfo; }
  public String getCronExpression() { return cronExpression; }
  public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

  /**
   * 💡 [최종 방어선] 완화된 바인딩 엔진의 결함을 파괴하고, Environment 컨텍스트의 원시 키 배열을 역추적합니다.
   */
  public String getDynamicPath(String systemType, String infoType) {
    if (systemType == null || infoType == null) {
      throw new IllegalArgumentException("요청 인자(systemType 또는 infoType)가 null일 수 없습니다.");
    }

    Map<String, PathConfig> currentPaths = new HashMap<>();

    // 1. 스프링 Environment 인터페이스를 확장 구현체로 캐스팅하여 모든 프로퍼티 소스에 직접 접근
    if (environment instanceof ConfigurableEnvironment) {
      ConfigurableEnvironment configEnv = (ConfigurableEnvironment) environment;

      // 로드된 모든 설정 파편(YML, 시스템 변수, 커스텀 Watcher 변수 등) 순회 스캔
      for (PropertySource<?> propertySource : configEnv.getPropertySources()) {
        if (propertySource instanceof EnumerablePropertySource) {
          EnumerablePropertySource<?> enumerableSource = (EnumerablePropertySource<?>) propertySource;

          for (String key : enumerableSource.getPropertyNames()) {
            // 우리가 찾는 베이스 프리픽스 패턴 탐색 (예: radar.config.paths.kma-rdr.site-info)
            if (key.startsWith("radar.config.paths.")) {
              String rawValue = String.valueOf(enumerableSource.getProperty(key));

              // 키를 분해하여 시스템 타입 구조 분석
              // 패턴: radar.config.paths.[시스템타입].[정보타입]
              String subKey = key.substring("radar.config.paths.".length()); // ex) "kma-rdr.site-info" 또는 "KMA_RDR.site-info"

              int lastDotIdx = subKey.lastIndexOf('.');
              if (lastDotIdx > 0) {
                String parsedSysType = subKey.substring(0, lastDotIdx).toUpperCase().replace("-", "_"); // 대문자_언더바 포맷 통합
                String parsedInfoType = subKey.substring(lastDotIdx + 1); // "site-info" 또는 "ip-info"

                // 해당 시스템 타입의 객체 바인딩 생성 및 적재
                PathConfig config = currentPaths.computeIfAbsent(parsedSysType, k -> new PathConfig());

                if ("site-info".equalsIgnoreCase(parsedInfoType)) {
                  config.setSiteInfo(rawValue);
                } else if ("ip-info".equalsIgnoreCase(parsedInfoType)) {
                  config.setIpInfo(rawValue);
                } else if ("cron-expression".equalsIgnoreCase(parsedInfoType)) {
                  config.setCronExpression(rawValue);
                }
              }
            }
          }
        }
      }
    }

    // 외부 감시 엔진(YmlWatcher) 작동 직후 수집된 실제 매핑 매트릭스 출력
    printExternalYmlLog();
    System.out.println("--- [실시간 조회 디버깅] ---");
    System.out.println("요청 SystemType: " + systemType);
    System.out.println("컨텍스트 원시 역추적 매핑 결과: " + currentPaths);
    traceActualExternalConfig();
    System.out.println("----------------------------");

    // 2. 가공된 메모리 맵에서 요청값 매칭 및 최종 예외 방어
    String targetUpperSys = systemType.toUpperCase().replace("-", "_");
    PathConfig finalConfig = currentPaths.get(targetUpperSys);

    if (finalConfig == null) {
      throw new IllegalArgumentException(String.format(
          "[YmlWatcher] 가 외부 설정을 로드했으나, 컨텍스트 내부에서 매칭되는 대상을 찾지 못했습니다. (요청: %s)", systemType));
    }

    return finalConfig.getPathByInfoType(infoType);
  }

  /**
   * 하위 설정 매핑 정적 클래스
   */
  public static class PathConfig {
    private String cronExpression = "0 * * * * *";
    private String siteInfo;
    private String ipInfo;

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public String getSiteInfo() { return siteInfo; }
    public void setSiteInfo(String siteInfo) { this.siteInfo = siteInfo; }
    public String getIpInfo() { return ipInfo; }
    public void setIpInfo(String ipInfo) { this.ipInfo = ipInfo; }

    public String getPathByInfoType(String infoType) {
      switch (infoType) {
        case "site-info": return siteInfo;
        case "ip-info": return ipInfo;
        default: throw new IllegalArgumentException("지원하지 않는 정보 타입: " + infoType);
      }
    }

    @Override
    public String toString() {
      return String.format("{siteInfo='%s', ipInfo='%s', cron='%s'}", siteInfo, ipInfo, cronExpression);
    }
  }

  public void printExternalYmlLog() {
    System.out.println("\n========== [외부 YML 파일 추적 시작] ==========");

    // 💡 데몬 인프라 환경에서 주로 사용하는 외부 YML 예상 경로 목록
    String[] targetPaths = {
        "./config/application.yml",
        "./conf/application.yml",
        "./application.yml",
        "../config/application.yml"
    };

    boolean fileFound = false;

    for (String path : targetPaths) {
      File ymlFile = new File(path);
      System.out.println(String.format("🔍 파일 탐색 중... 경로: [%s] -> 존재여부: %b",
          ymlFile.getAbsolutePath(), ymlFile.exists()));

      if (ymlFile.exists() && ymlFile.isFile()) {
        fileFound = true;
        System.out.println(String.format("🟢 외부 YML 발견! 원문 출력을 시작합니다. -> (%s)", path));
        System.out.println("--------------------------------------------------");

        // 파일 내용 한 줄씩 읽어서 로그로 출력
        try (BufferedReader br = new BufferedReader(new FileReader(ymlFile))) {
          String line;
          while ((line = br.readLine()) != null) {
            System.out.println("[YML 원문] " + line);
          }
        } catch (IOException e) {
          System.err.println("❌ 파일을 읽는 중 오류 발생: " + e.getMessage());
        }

        System.out.println("--------------------------------------------------");
        break; // 파일 하나를 찾아서 출력했으므로 루프 종료
      }
    }

    if (!fileFound) {
      System.err.println("❌ 시스템이 지정한 표준 외부 경로에서 application.yml 파일을 찾을 수 없습니다.");
      System.err.println("💡 실행 스크립트(sh/bat)나 java -jar 명령어 뒤에 '--spring.config.location=' 옵션이 있는지 확인해보세요.");
    }
    System.out.println("========== [외부 YML 파일 추적 종료] ==========\n");
  }

  public void traceActualExternalConfig() {
    System.out.println("\n==================================================");
    System.out.println("🛰️ [데몬 실행 환경 및 파일 시스템 전수 조사 시작]");
    System.out.println("==================================================");

    // 1. 현재 자바 프로세스가 실행된 가동 절대 경로(Working Directory) 확인
    String currentDir = System.getProperty("user.dir");
    System.out.println("📍 [Current Working Dir] " + currentDir);

    // 2. java -jar 실행 시 주입된 모든 시스템 파라미터(-D 옵션 및 VM 아규먼트) 출력
    System.out.println("\n⚙️ [JVM Arguments 디버깅]");
    RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    List<String> arguments = runtimeMxBean.getInputArguments();
    for (String arg : arguments) {
      System.out.println("  -> System Argument: " + arg);
    }

    // 3. 현재 실행 디렉터리 하위의 모든 YML / CONF 파일 목록 전수 조사
    System.out.println("\n📂 [현재 실행 디렉터리 내부 파일 스캔]");
    File dir = new File(currentDir);
    scanDirectoryFiles(dir, 0);

    System.out.println("==================================================");
    System.out.println("🛰️ [데몬 실행 환경 및 파일 시스템 전수 조사 종료]");
    System.out.println("==================================================\n");
  }

  // 재귀적으로 폴더 내의 yml, conf 설정 파일을 찾아내고 내용을 출력하는 헬퍼 메서드
  private void scanDirectoryFiles(File folder, int depth) {
    File[] files = folder.listFiles();
    if (files == null) return;

    // 무한 루프 방지를 위해 너무 깊은 depth는 제한 (기본 실행 구조 폴더 위주)
    if (depth > 2) return;

    for (File file : files) {
      if (file.isDirectory()) {
        // 특정 불필요 폴더 스킵 (.git, build, .gradle 등)
        if (file.getName().startsWith(".") || file.getName().equals("build") || file.getName().equals("gradle")) {
          continue;
        }
        scanDirectoryFiles(file, depth + 1);
      } else if (file.isFile()) {
        String name = file.getName().toLowerCase();
        // 확장자가 yml, yaml, conf인 설정 파일 식별
        if (name.endsWith(".yml") || name.endsWith(".yaml") || name.endsWith(".conf")) {
          System.out.println(String.format("   📄 발견된 설정 파일: [%s]", file.getAbsolutePath()));

          // 해당 파일 내부에 'paths:' 혹은 'KMA_RDR' 단어가 들었는지 간략 확인 및 원문 출력
          try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean hasRadarConfig = false;
            StringBuilder fileContent = new StringBuilder();

            while ((line = br.readLine()) != null) {
              fileContent.append(line).append("\n");
              if (line.contains("radar") || line.contains("paths") || line.contains("KMA_RDR")) {
                hasRadarConfig = true;
              }
            }

            if (hasRadarConfig) {
              System.out.println("   🔥 [확인] 이 파일 내부에 레이더 동적 경로 설정이 감지되었습니다!");
              System.out.println("   --- [파일 내용 시작] ---");
              System.out.print(fileContent.toString());
              System.out.println("   --- [파일 내용 끝] ---");
            }
          } catch (Exception e) {
            System.err.println("   ❌ 파일 읽기 실패: " + e.getMessage());
          }
        }
      }
    }
  }

}

