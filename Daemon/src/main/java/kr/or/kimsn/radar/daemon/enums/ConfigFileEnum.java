package kr.or.kimsn.radar.daemon.enums;

public enum ConfigFileEnum {
  SITE_INFO("site-info"),
  IP_INFO("ip-info");

  private final String infoType;

  ConfigFileEnum(String infoType) {
    this.infoType = infoType;
  }

  public String getInfoType() {
    return infoType;
  }
}
