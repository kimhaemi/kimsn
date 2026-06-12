package kr.or.kimsn.radar.daemon.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

  private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter ADJUSTED_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  /**
   * 현재 한국 시각(KST)을 yyyy-MM-dd HH:mm:ss 포맷 문자열로 반환합니다.
   */
  public static String getCurrentKstString() {
    return LocalDateTime.now(ZoneId.of("Asia/Seoul")).format(DEFAULT_FORMATTER);
  }

  /**
   * 현재 한국 시각을 기준으로 세계협정시(UTC) 변환 후 포맷 문자열로 반환합니다.
   */
  public static String getCurrentUtcString() {
    ZonedDateTime kstZone = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    ZonedDateTime utcZone = kstZone.withZoneSameInstant(ZoneId.of("UTC"));
    return utcZone.format(DEFAULT_FORMATTER);
  }

  /**
   * 입력받은 초(second) 이전의 한국 시각을 구한 뒤 yyyyMMddHHmm 형식으로 반환합니다. (파일명 패턴 매칭용)
   */
  public static String getPreviousTimePattern(int second) {
    LocalDateTime targetTime = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusSeconds(second);
    return targetTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
  }

  /**
   * 현재 시각을 마스터 정산 규격에 맞게 5분 단위로 절삭 보정합니다. (yyyy-MM-dd HH:mm)
   */
  public static String getAdjustedCurrentTime(int gubun) {
    LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    if (gubun == 1 || gubun == 3) {
      int adjustedMinute = (now.getMinute() / 5) * 5;
      now = now.withMinute(adjustedMinute).withSecond(0).withNano(0);
    } else if (gubun == 2) {
      now = now.withSecond(0).withNano(0);
    }
    return now.format(ADJUSTED_FORMATTER);
  }

  /**
   * 문자열 시각이 들어오면 분의 끝자리를 5분 단위 하향 절삭하여 반환합니다.
   */
  public static String getAdjustedPreviousTime(String rawPreviousTime) {
    if (rawPreviousTime == null || rawPreviousTime.trim().isEmpty()) {
      return "";
    }
    String cleanTime = rawPreviousTime.trim();
    if (cleanTime.length() > 16) {
      cleanTime = cleanTime.substring(0, 16);
    }
    try {
      LocalDateTime dateTime = LocalDateTime.parse(cleanTime, ADJUSTED_FORMATTER);
      int adjustedMinute = (dateTime.getMinute() / 5) * 5;
      return dateTime.withMinute(adjustedMinute).format(ADJUSTED_FORMATTER);
    } catch (Exception e) {
      int lastDigit = Integer.parseInt(cleanTime.substring(cleanTime.length() - 1));
      return cleanTime.substring(0, cleanTime.length() - 1) + (lastDigit < 5 ? "0" : "5");
    }
  }
}
