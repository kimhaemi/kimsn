package kr.or.kimsn.radar.daemon.enums;

public enum ConfigFileEnum {
  // local (기본 로컬용 경로 세트)
  LOCAL_SITE_INFO(0, "SITE_INFO", "./conf/siteInfoSetting.conf"),
  LOCAL_IP_INFO(0, "IP_INFO", "./conf/siteIPInfoSetting.conf"),

  // 대형 (gubun: 1)
  RDR_SITE_INFO(1, "SITE_INFO", "/home/watcher/deamon/RDR/conf/siteInfoSetting.conf"),
  RDR_IP_INFO(1, "IP_INFO", "/home/watcher/ipConf/RDR/siteIPInfoSetting.conf"),

  // 소형 (gubun: 2)
  SDR_SITE_INFO(2, "SITE_INFO", "/home/watcher/deamon/SDR/conf/siteInfoSetting.conf"),
  SDR_IP_INFO(2, "IP_INFO", "/home/watcher/ipConf/SDR/siteIPInfoSetting.conf"),

  // 공항 (gubun: 3)
  TDWR_SITE_INFO(3, "SITE_INFO", "/home/watcher/deamon/TDWR/conf/siteInfoSetting.conf"),
  TDWR_IP_INFO(3, "IP_INFO", "/home/watcher/ipConf/TDWR/siteIPInfoSetting.conf");

  private final int gubun;
  private final String infoType;
  private final String fileName;

  // 괄호 안의 3개 파라미터를 모두 받아 초기화하도록 생성자 수정
  ConfigFileEnum(int gubun, String infoType, String fileName) {
    this.gubun = gubun;
    this.infoType = infoType;
    this.fileName = fileName;
  }

  public String getFileName() { return fileName; }

  public static String getPath(int gubun, String infoType) {
    for (ConfigFileEnum file : values()) {
      if (file.getGubun() == gubun && file.getInfoType().equalsIgnoreCase(infoType)) {
        return file.fileName;
      }
    }
    throw new IllegalArgumentException("일치하는 설정 파일 경로가 없습니다: gubun=" + gubun + ", type=" + infoType);
  }

  // Getter 추가 필수
  public int getGubun() { return gubun; }
  public String getInfoType() { return infoType; }
}
