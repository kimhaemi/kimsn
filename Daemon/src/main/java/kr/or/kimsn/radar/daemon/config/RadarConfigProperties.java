package kr.or.kimsn.radar.daemon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class RadarConfigProperties {

  private final Environment environment;

  public RadarConfigProperties(Environment environment) {
    this.environment = environment;
  }

  public String getSiteInfoPath() { return environment.getProperty("radar.config.site-info-path"); }
  public String getIpInfoPath() { return environment.getProperty("radar.config.ip-info-path"); }
  public String getCronExpression() { return environment.getProperty("radar.config.cron-expression", "0 * * * * *"); }

  /**
   * 💡 신규 고도화: 자바 소스코드 하드코딩 대신 yml의 paths 하위 시스템 타입 경로를 동적으로 리턴합니다.
   */
  public String getDynamicPath(String systemType, String infoType) {
    String key = "radar.config.paths." + systemType + "." + infoType;
    return environment.getProperty(key);
  }
}
