package kr.or.kimsn.radar.daemon.util;

import kr.or.kimsn.radar.daemon.config.RadarConfigProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConfigManager {

  private static final Map<String, String> configCache = new ConcurrentHashMap<>();
  private static final Map<String, Long> fileLastModifiedMap = new ConcurrentHashMap<>();

  private static String targetSiteInfoPath;
  private static String targetIpInfoPath;

  private final RadarConfigProperties radarConfigProperties;

  public ConfigManager(RadarConfigProperties radarConfigProperties) {
    this.radarConfigProperties = radarConfigProperties;
  }

  @PostConstruct
  public void init() {
    targetSiteInfoPath = radarConfigProperties.getSiteInfoPath();
    targetIpInfoPath = radarConfigProperties.getIpInfoPath();
    checkAndReload();
  }

  private static synchronized void checkAndReload() {
    String[] paths = {targetSiteInfoPath, targetIpInfoPath};
    for (String path : paths) {
      if (path == null) continue;
      File file = new File(path);
      if (!file.exists()) continue;

      long currentModified = file.lastModified();
      long lastModified = fileLastModifiedMap.getOrDefault(path, -1L);

      if (currentModified > lastModified) {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
          props.load(in);
          for (String key : props.stringPropertyNames()) {
            configCache.put(key, props.getProperty(key));
          }
          fileLastModifiedMap.put(path, currentModified);
        } catch (IOException e) {
          System.err.println("[ConfigManager] 로드 실패: " + path);
        }
      }
    }
  }

  public static String getString(String key) {
    checkAndReload();
    return configCache.getOrDefault(key, "").trim();
  }

  public static int getInt(String key) {
    return Integer.parseInt(getString(key));
  }

  public static long getLong(String key) {
    return Long.parseLong(getString(key));
  }

  // 장비명 한글 텍스트 편의 반환 메소드 추가
  public static String getGubunStr() {
    int gubun = getInt("gubun");
    if (gubun == 1) return "대형";
    if (gubun == 2) return "소형";
    if (gubun == 3) return "공항";
    return "알수없음";
  }
}
