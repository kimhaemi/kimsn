package kr.or.kimsn.radar.daemon.enums;

/**
 * 데이터 종류
 */
public enum DataKindEnum {
  RDR(1, "RDR"),
  SDR(2, "SDR"),
  TDWR(3, "TDWR"),
  UNKNOWN(0, ""); // default에 해당하는 기본값

  private final int gubun;
  private final String description;

  // 생성자
  DataKindEnum(int gubun, String description) {
    this.gubun = gubun;
    this.description = description;
  }

  // Getter
  public int getGubun() { return gubun; }
  public String getDescription() { return description; }

  // 기존 switch 문을 대체하는 static 메소드
  public static String getDescriptionByGubun(int gubun) {
    for (DataKindEnum kind : values()) {
      if (kind.getGubun() == gubun) {
        return kind.getDescription();
      }
    }
    return UNKNOWN.getDescription(); // 일치하는 게 없으면 빈 문자열 반환
  }
}
