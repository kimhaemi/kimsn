package kr.or.kimsn.radar.daemon.enums;

public enum ReceiveConditionEnum {
  ORDI("ORDI", "정상"),
  WARN("WARN", "주의/경고 (미수신)"),
  TOTA("TOTA", "전 사이트 네트워크 마비"),
  TORE("TORE", "네트워크 복구중"),
  RETR("RETR", "복구완료");

  private final String code;
  private final String description;

  ReceiveConditionEnum(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getCode() { return code; }
  public String getDescription() { return description; }

  // DB 문자열 안전 변환 유틸리티
  public static ReceiveConditionEnum find(String code) {
    for (ReceiveConditionEnum condition : values()) {
      if (condition.getCode().equalsIgnoreCase(code)) return condition;
    }
    return ORDI; // 기본 방어값
  }
}
