package kr.or.kimsn.radar.daemon.util;

import lombok.extern.slf4j.Slf4j;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Slf4j
public class DataCommon {

    public static String getInfoConf(String infoStr, String tp, String siteInfo, String ipInfo) {
        String confInfo = "";
        String targetFilePath = "siteInfo".equals(infoStr) ? siteInfo : ipInfo;

        if (targetFilePath == null || targetFilePath.isEmpty()) {
            log.error("[⚙️ 인프라] 설정 파일 경로 지정이 유효하지 않습니다.");
            return confInfo;
        }

        // try-with-resources 구조 적용으로 open file leak 완전 차단
        try (FileInputStream fis = new FileInputStream(targetFilePath);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {

            Properties props = new Properties();
            props.load(isr);
            confInfo = props.getProperty(tp);

        } catch (Exception e) {
            log.error("[❌ 설정 실패] 파일: {}, 키: {}, 원인: {}", targetFilePath, tp, e.getMessage());
        }

        return confInfo != null ? confInfo.trim() : "";
    }
}
