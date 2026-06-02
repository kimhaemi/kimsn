package kr.or.kimsn.radar.daemon.util;

import java.util.Date;
import kr.or.kimsn.radar.data.common.util.DateUtil;

public class TimeUtil {

  /**
   * 현재 시각을 지정된 포맷으로 반환하고 대형/공항 규격에 맞게 0분 또는 5분 단위로 보정합니다.
   */
  public static String getAdjustedCurrentTime(int gubun) {
    String currentTime = DateUtil.formatDate("yyyy-MM-dd HH:mm", new Date());
    if (gubun == 1 || gubun == 3) {
      int lastDigit = Integer.parseInt(currentTime.substring(currentTime.length() - 1));
      currentTime = currentTime.substring(0, currentTime.length() - 1) + (lastDigit <= 5 ? "0" : "5");
    }
    return currentTime;
  }

  /**
   * 특정 초 전의 시각을 구한 뒤 끝자리를 0 또는 5분 단위로 보정하여 반환합니다.
   */
  public static String getAdjustedPreviousTime(String rawPreviousTime) {
    int lastDigit = Integer.parseInt(rawPreviousTime.substring(rawPreviousTime.length() - 1));
    return rawPreviousTime.substring(0, rawPreviousTime.length() - 1) + (lastDigit <= 5 ? "0" : "5");
  }
}
