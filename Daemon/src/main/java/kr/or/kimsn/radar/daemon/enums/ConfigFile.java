package kr.or.kimsn.radar.daemon.enums;

public enum ConfigFile {
  //local
  LOCAL_SITE_INFO("./conf/siteInfoSetting.conf"),
  LOCAL_IP_INFO("./conf/siteIPInfoSetting.conf"),

  //대형
  RDR_SITE_INFO("/home/watcher/deamon/RDR/conf/siteInfoSetting.conf"),
  RDR_IP_INFO("/home/watcher/ipConf/RDR/siteIPInfoSetting.conf"),

  //소형
  SDR_SITE_INFO("/home/watcher/deamon/SDR/conf/siteInfoSetting.conf"),
  SDR_IP_INFO("/home/watcher/ipConf/SDR/siteIPInfoSetting.conf"),

  //공항
  TDWR_SITE_INFO("/home/watcher/deamon/TDWR/conf/siteInfoSetting.conf"),
  TDWR_IP_INFO("/home/watcher/ipConf/TDWR/siteIPInfoSetting.conf"),
  ;

  private final int gubun;
  private final String infoType; // "SITE_INFO" 또는 "IP_INFO"
  private final String fileName;

  ConfigFile(String fileName) { this.fileName = fileName; }

  public String getFileName() { return fileName; }

  // ⭐ 핵심: gubun과 파일 종류를 주면 매칭되는 경로를 찾아주는 메소드
  public static String getPath(int gubun, String infoType) {
    for (ConfigFile file : values()) {
      if (file.gubun == gubun && file.infoType.equalsIgnoreCase(infoType)) {
        return file.fileName;
      }
    }
    throw new IllegalArgumentException("일치하는 설정 파일 경로가 없습니다: gubun=" + gubun + ", type=" + infoType);
  }
}
}
