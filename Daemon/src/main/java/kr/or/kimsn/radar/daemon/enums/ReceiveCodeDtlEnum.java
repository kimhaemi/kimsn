package kr.or.kimsn.radar.daemon.enums;

public enum ReceiveCodeDtlEnum {
  OK("ok"),
  FILE_NO("file_no"),
  FILE_SIZE_NO("filesize_no"),
  FILE_OK("file_ok"),
  FILE_SIZE_OK("filesize_ok"),
  NETWORK_NO("network_no"),
  NETWORK_OK("network_ok"),
  UNKNOWN("");

  private final String code;

  ReceiveCodeDtlEnum(String code) { this.code = code; }
  public String getCode() { return code; }

  public static ReceiveCodeDtlEnum find(String code) {
    for (ReceiveCodeDtlEnum dtl : values()) {
      if (dtl.getCode().equalsIgnoreCase(code)) return dtl;
    }
    return UNKNOWN;
  }
}
