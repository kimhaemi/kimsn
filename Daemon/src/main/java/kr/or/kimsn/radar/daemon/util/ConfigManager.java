package kr.or.kimsn.radar.daemon.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.annotation.PostConstruct;
import kr.or.kimsn.radar.daemon.config.RadarConfigProperties;
import kr.or.kimsn.radar.daemon.enums.ConfigFileEnum;
import org.springframework.stereotype.Component;

@Component
public class ConfigManager {

  private static final Map<String, String> configCache = new ConcurrentHashMap<>();
  private static final Map<String, Long> fileLastModifiedMap = new ConcurrentHashMap<>();

  private static String targetSiteInfoPath;
  private static String targetIpInfoPath;
  private static String currentSystemType = "LOCAL"; // 기본 로컬 테스트 기본값 지정

  private final RadarConfigProperties radarConfigProperties;

  public ConfigManager(RadarConfigProperties radarConfigProperties) {
    this.radarConfigProperties = radarConfigProperties;
  }

  /**
   * 💡 [완벽한 유연성 달성]
   * 자바 소스코드 내의 어떠한 물리 파일 경로도 참조하지 않으며,
   * Jar 배포본 매니페스트 키값과 application.yml의 설정 데이터를 다이렉트로 바인딩 결합합니다.
   */
  @PostConstruct
  public void init() {
    // 1단계: 실행 중인 단독 가동 JAR 내부의 전용 System-Type 정보(KMA_RDR ~ MCEE_SDR) 획득
    try {
      URL resObj = getClass().getClassLoader().getResource("META-INF/MANIFEST.MF");
      if (resObj != null) {
        Manifest mf = new Manifest(resObj.openStream());
        Attributes attr = mf.getMainAttributes();
        String sysTypeStr = attr.getValue("Radar-System-Type");
        if (sysTypeStr != null && !sysTypeStr.trim().isEmpty()) {
          currentSystemType = sysTypeStr.trim();
        }
      }
    } catch (Exception e) {
      System.err.println("[⚙️ 인프라 감지] 매니페스트 연동 보류 -> 디폴트 LOCAL 테스트 모드를 고수합니다.");
    }

    // 2단계: 획득한 시스템 타입을 토대로 application.yml에 빼둔 리눅스 실경로 동적 추출 적용
    if (!"LOCAL".equals(currentSystemType)) {
      targetSiteInfoPath = radarConfigProperties.getDynamicPath(currentSystemType, ConfigFileEnum.SITE_INFO.getInfoType());
      targetIpInfoPath = radarConfigProperties.getDynamicPath(currentSystemType, ConfigFileEnum.IP_INFO.getInfoType());
    } else {
      // 로컬 IDE 시뮬레이션 가동 단계일 경우 yml의 최상단 기본 경로 수용
      targetSiteInfoPath = radarConfigProperties.getSiteInfo();
      targetIpInfoPath = radarConfigProperties.getIpInfo();
    }

    // 3단계: 매핑 완료된 원격지 .conf 파일 실시간 캐싱 파이프라인 가동
    checkAndReload();
  }

  private static synchronized void checkAndReload() {
    String[] paths = {targetSiteInfoPath, targetIpInfoPath};
    for (String path : paths) {
      if (path == null || path.trim().isEmpty()) continue;
      File file = new File(path);
      if (!file.exists()) continue;

      long currentModified = file.lastModified();
      long lastModified = fileLastModifiedMap.getOrDefault(path, -1L);

      if (currentModified > lastModified) {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8)) {

          props.load(isr);
          for (String key : props.stringPropertyNames()) {
            configCache.put(key, props.getProperty(key));
          }
          fileLastModifiedMap.put(path, currentModified);
          System.out.println("[⚙️ ConfigManager] 외부 주입 .conf 파일 실시간 갱신 완료 -> " + path);

        } catch (IOException e) {
          System.err.println("[❌ ConfigManager] 설정 로드 실패: " + path);
        }
      }
    }
  }

  public static String getString(String key) {
    checkAndReload();
    return configCache.getOrDefault(key, "").trim();
  }

  public static int getInt(String key) {
    String val = getString(key);
    return val.isEmpty() ? 0 : Integer.parseInt(val);
  }

  public static long getLong(String key) {
    String val = getString(key);
    return val.isEmpty() ? 0L : Long.parseLong(val);
  }

  public static String getGubunStr() {
    // 💡 개편: 가동 환경 식별 텍스트를 중복 혼선이 없는 시스템 타입 문자열로 깔끔하게 리턴
    return currentSystemType;
  }
}
