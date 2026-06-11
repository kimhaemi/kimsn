package kr.or.kimsn.radar.daemon.enums;

public enum RadarTypeEnum {
  RDR(1, "대형", "RDR"),
  SDR(2, "소형", "SDR"),
  TDWR(3, "공항", "TDWR"),
  UNKNOWN(0, "알수없음", "");

  private final int gubun;
  private final String krName;  // "대형", "소형", "공항"
  private final String code;    // "RDR", "SDR", "TDWR"

  RadarTypeEnum(int gubun, String krName, String code) {
    this.gubun = gubun;
    this.krName = krName;
    this.code = code;
  }

  public int getGubun() { return gubun; }
  public String getKrName() { return krName; }
  public String getCode() { return code; }

  // ⭐ 핵심: gubun 숫자를 던지면 매칭되는 Enum 객체 자체를 찾아주는 메소드
  public static RadarTypeEnum find(int gubun) {
    for (RadarTypeEnum type : values()) {
      if (type.getGubun() == gubun) {
        return type;
      }
    }
    return UNKNOWN;
  }
}
