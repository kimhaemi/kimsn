package kr.or.kimsn.radar.daemon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "radar.config")
public class RadarConfigProperties {

  private String siteInfo;
  private String ipInfo;
  private String cronExpression = "0 * * * * *";

  // 기본 환경 변수 주입용 Getter & Setter
  public String getSiteInfo() { return siteInfo; }
  public void setSiteInfo(String siteInfo) { this.siteInfo = siteInfo; }
  public String getIpInfo() { return ipInfo; }
  public void setIpInfo(String ipInfo) { this.ipInfo = ipInfo; }
  public String getCronExpression() { return cronExpression; }
  public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

  /**
   * 💡 기존 호출부 유지용 기본 메서드 (공통 기본 경로 반환 시 사용)
   */
  public String getSiteInfoPath() {
    return this.siteInfo;
  }

  public String getIpInfoPath() {
    return this.ipInfo;
  }

  /**
   * 💡 [최종 완결] 상위 폴더의 외부 YML을 직접 파싱하고, 내장된 상대 경로 꼬임까지 자동으로 보정하여 반환합니다.
   *
   * @param systemType 시스템 식별 구분자 (예: KMA_RDR, kma_rdr)
   * @param infoType   정보 타입 식별자 (예: site-info, ip-info)
   * @return 최종 보정된 설정 파일의 물리적 상대 경로
   */
  @SuppressWarnings("unchecked")
  public String getDynamicPath(String systemType, String infoType) {
    if (systemType == null || infoType == null) {
      throw new IllegalArgumentException("요청 인자(systemType 또는 infoType)가 null일 수 없습니다.");
    }

    // 1. Daemon 모듈 바깥(상위) 선상의 config 경로를 최우선 순위로 지정하여 파일 객체 생성
    String[] candidatePaths = {
      "/home/watcher/deamon/config/application.yml",
      "/home/watcher/daemon/config/application.yml",
      "../config/application.yml",  // Daemon 모듈 바깥과 같은 선상의 config (가장 유력)
      "./config/application.yml",   // 최상위 루트 프로젝트 기준 실행 시 대안 경로
      "./Daemon/config/application.yml"
    };

    File ymlFile = null;
    for (String path : candidatePaths) {
      File checkFile = new File(path);
      if (checkFile.exists() && checkFile.isFile()) {
        ymlFile = checkFile;
        break;
      }
    }

    // 예외 방어: 설정 파일 자체가 완전히 실종된 경우
    if (ymlFile == null) {
      throw new IllegalStateException(String.format(
          "지정된 외부 YML 파일을 로드할 수 없습니다. 현재 작업 디렉터리 user.dir=[%s] 기준으로 바깥 config 폴더 위치를 찾지 못했습니다.",
          System.getProperty("user.dir")));
    }

    try (InputStream inputStream = new FileInputStream(ymlFile)) {
      // 2. SnakeYAML 엔진을 사용해 외부 YML 파일을 Raw 중첩 맵(Map) 구조로 다이렉트 파싱
      Yaml yaml = new Yaml();
      Map<String, Object> yamlMap = yaml.load(inputStream);

      // 3. 계층 추적: radar -> config -> paths 순차 진입
      Map<String, Object> radar = (Map<String, Object>) yamlMap.get("radar");
      if (radar != null) {
        Map<String, Object> config = (Map<String, Object>) radar.get("config");
        if (config != null) {
          Map<String, Object> paths = (Map<String, Object>) config.get("paths");
          if (paths != null) {

            // 4. 대소문자 및 기호 변형 구분을 허용하여 요청된 systemType 검색
            for (Map.Entry<String, Object> entry : paths.entrySet()) {
              String key = entry.getKey();

              // KMA_RDR, kma_rdr, kma-rdr 명명 형식을 통일하여 비교 매칭
              if (key.equalsIgnoreCase(systemType) || 
                  key.toUpperCase().replace("-", "_").equalsIgnoreCase(systemType.toUpperCase().replace("-", "_"))) {

                Map<String, Object> sysConfig = (Map<String, Object>) entry.getValue();
                if (sysConfig != null) {
                  Object targetPathValue = sysConfig.get(infoType); // "site-info" 또는 "ip-info"

                  if (targetPathValue != null) {
                    String resultPath = String.valueOf(targetPathValue);

                    // 💡 5. [자동 경로 보정 필터]
                    // YML 내부에 "./config/" 로 하드코딩된 상대 경로는 Daemon 모듈 입장에서 볼 때 
                    // 바깥에 있으므로 "../config/" 구조로 자동 변경해 주어야 파일 탐색 시 에러가 나지 않습니다.
                    if (resultPath.startsWith("./config/")) {
                      resultPath = "../" + resultPath.substring(2);
                    }

                    System.out.println(String.format("🎯 [외부 YML 로드 & 경로 보정 성공] %s -> %s = %s (참조파일: %s)",
                        systemType, infoType, resultPath, ymlFile.getPath()));
                    
                    return resultPath;
                  }
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      System.err.println("❌ 외부 YML 파싱 중 치명적 에러 발생: " + e.getMessage());
    }

    // 파일은 존재하나 내부 트리 구조 내에서 매칭되는 데이터를 찾지 못한 경우 예외 던짐
    throw new IllegalArgumentException(String.format(
        "외부 YML 파일 내에서 지정된 시스템 경로 정보를 찾을 수 없습니다. (SystemType: [%s], InfoType: [%s])", systemType, infoType));
  }
}
