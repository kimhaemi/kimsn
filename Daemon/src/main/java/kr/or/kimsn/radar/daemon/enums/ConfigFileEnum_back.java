package kr.or.kimsn.radar.daemon.enums;

public enum ConfigFileEnum_back {
  SITE_INFO("site-info"),
  IP_INFO("ip-info");

  private final String infoType;

  ConfigFileEnum_back(String infoType) {
    this.infoType = infoType;
  }

  public String getInfoType() {
    return infoType;
  }
}
