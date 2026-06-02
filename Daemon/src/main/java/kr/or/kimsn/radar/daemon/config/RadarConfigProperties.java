package kr.or.kimsn.radar.daemon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class RadarConfigProperties {

  private final Environment environment;

  public RadarConfigProperties(Environment environment) {
    this.environment = environment;
  }

  // environment.getProperty를 통해 호출 시점에 가장 최신의 실시간 YML 값을 반환합니다.
  public String getSiteInfoPath() { return environment.getProperty("radar.config.site-info-path"); }
  public String getIpInfoPath() { return environment.getProperty("radar.config.ip-info-path"); }
  public String getCronExpression() { return environment.getProperty("radar.config.cron-expression", "0 * * * * *"); }
  public String getLogName() { return environment.getProperty("radar.config.log-name", "radarCommon"); }
}
