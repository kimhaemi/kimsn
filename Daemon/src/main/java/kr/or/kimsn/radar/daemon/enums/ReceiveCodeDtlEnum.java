package kr.or.kimsn.radar.daemon.enums;

import java.util.Arrays;

public enum ReceiveCodeDtlEnum {
  OK("ok", "정상 수신"),
  FILE_NO("file_no", "자료 미수신"),
  FILE_SIZE_NO("filesize_no", "파일 품질 이상"),
  NETWORK_NO("network_no", "네트워크 장애"),
  NETWORK_OK("network_ok", "네트워크 복구"),
  FILE_OK("file_ok", "파일 미수신 복구"),
  FILE_SIZE_OK("filesize_ok", "파일 품질 복구");

  private final String code;
  private final String desc;

  ReceiveCodeDtlEnum(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public String getCode() { return this.code; }
  public String getDesc() { return this.desc; }

  public static ReceiveCodeDtlEnum find(String code) {
    return Arrays.stream(ReceiveCodeDtlEnum.values())
        .filter(e -> e.getCode().equalsIgnoreCase(code))
        .findFirst()
        .orElse(OK);
  }
}
