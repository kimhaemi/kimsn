package kr.or.kimsn.radar.daemon.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum RadarTypeEnum {
  LARGE(1, "RDR", "대형 레이더"),
  SMALL(2, "SDR", "소형 레이더"),
  AIRPORT(3, "TDWR", "공항 레이더"),
  UNKNOWN(0, "ERR", "미지정 장비");

  private final int gubun;
  private final String code;
  private final String krName;

  public static RadarTypeEnum find(int gubun) {
    return Arrays.stream(values())
        .filter(e -> e.getGubun() == gubun)
        .findFirst()
        .orElse(UNKNOWN);
  }
}