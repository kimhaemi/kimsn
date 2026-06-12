package kr.or.kimsn.radar.daemon.enums;

import java.util.Arrays;

public enum ReceiveConditionEnum {
  ORDI("ORDI", "정상"),

  // 💡 하나의 상태코드("WARN") 안에서 한글 명칭으로 세분화 분기
  WARN_ATTN("WARN", "주의"),
  WARN_QUAL("WARN", "품질이상"),
  WARN_MISS("WARN", "미수신"),

  RETR("RETR", "정상복구"),
  TOTA("TOTA", "전체장애"),
  TORE("TORE", "전체복구");

  private final String code;
  private final String krName;

  ReceiveConditionEnum(String code, String krName) {
    this.code = code;
    this.krName = krName;
  }

  public String getCode() {
    return this.code;
  }

  public String getKrName() {
    return this.krName;
  }

  /**
   * 기존에 가지고 계신 기본 탐색 메서드 (그대로 유지)
   */
  public static ReceiveConditionEnum find(String code) {
    return Arrays.stream(ReceiveConditionEnum.values())
        .filter(e -> e.getCode().equalsIgnoreCase(code))
        .findFirst()
        .orElse(ORDI);
  }

  /**
   * 💡 새로 하단에 추가할 정밀 탐색 메서드
   * 상태코드("WARN")와 한글 상세 사유를 동시 대조하여 정확한 엔티티 객체를 반환합니다.
   */
  public static ReceiveConditionEnum findDetail(String code, String krName) {
    return Arrays.stream(ReceiveConditionEnum.values())
        .filter(e -> e.getCode().equalsIgnoreCase(code) && e.getKrName().equals(krName))
        .findFirst()
        .orElseGet(() -> find(code)); // 일치하는 한글명이 없으면 기본 find(code) 결과로 방어
  }
}
