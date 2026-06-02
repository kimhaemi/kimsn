package kr.or.kimsn.radar.daemon.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigManager {

  private static final Map<String, String> configCache = new ConcurrentHashMap<>();
  private static final Map<String, Long> fileLastModifiedMap = new ConcurrentHashMap<>();

  private static String targetSiteInfoPath;
  private static String targetIpInfoPath;
  private static boolean isInitialized = false;

  /**
   * 1. 최초 서버 기동 시 파일 경로 세팅 및 1회 로드
   */
  public static synchronized void init(String initialSiteInfoPath) {
    if (isInitialized) return;

    try {
      // 최초 siteInfo에서 gubun 확인
      Properties initialProps = new Properties();
      try (FileInputStream in = new FileInputStream(initialSiteInfoPath)) {
        initialProps.load(in);
      }
      int gubunValue = Integer.parseInt(initialProps.getProperty("gubun", "0"));

      // gubun에 맞는 고정 경로 획득
      ConfigFile config = ConfigFile.findByGubun(gubunValue);
      targetSiteInfoPath = config.getSiteInfoPath();
      targetIpInfoPath = config.getIpInfoPath();

      // 최초 데이터 로드
      checkAndReload();
      isInitialized = true;
      System.out.println("=== 실시간 환경설정 매니저 초기화 완료 (gubun: " + gubunValue + ") ===");

    } catch (Exception e) {
      System.err.println("환경설정 초기화 실패!");
      e.printStackTrace();
    }
  }

  /**
   * 2. [핵심] 파일의 변경 여부를 체크하고 변동이 있다면 메모리를 갱신하는 메소드
   */
  private static synchronized void checkAndReload() {
    // 읽어야 할 파일 배열
    String[] paths = {targetSiteInfoPath, targetIpInfoPath};

    for (String path : paths) {
      if (path == null) continue;

      File file = new File(path);
      if (!file.exists()) {
        System.err.println("설정 파일이 존재하지 않습니다: " + path);
        continue;
      }

      long currentModified = file.lastModified();
      long lastModified = fileLastModifiedMap.getOrDefault(path, -1L);

      // 파일이 수정되었거나 처음 읽는 경우에만 로드 실행
      if (currentModified > lastModified) {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
          props.load(in);
          for (String key : props.stringPropertyNames()) {
            configCache.put(key, props.getProperty(key));
          }
          // 마지막 수정 시간 업데이트
          fileLastModifiedMap.put(path, currentModified);
          System.out.println("[Config] 파일 변경 감지 및 메모리 갱신 완료: " + path);
        } catch (IOException e) {
          System.err.println("파일 로드 중 오류 발생: " + path);
        }
      }
    }
  }

  /**
   * 3. 외부 비즈니스 로직에서 호출할 데이터 getter 메소드들
   * 값을 요청할 때마다 파일 변경 여부를 가볍게 체크(수 밀리초 소요) 후 전달합니다.
   */
  public static String getString(String key) {
    checkAndReload(); // 호출 시점에 파일이 변했는지 순간 체크
    return configCache.getOrDefault(key, "");
  }

  public static int getInt(String key) {
    return Integer.parseInt(getString(key).trim());
  }

  public static long getLong(String key) {
    return Long.parseLong(getString(key).trim());
  }
}
