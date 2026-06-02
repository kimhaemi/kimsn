package kr.or.kimsn.radar.daemon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "radar.config")
public class RadarConfigProperties {

  private String siteInfoPath;
  private String ipInfoPath;
  private String cronExpression; // 크론식 주입용 변수 추가

  public String getSiteInfoPath() { return siteInfoPath; }
  public void setSiteInfoPath(String siteInfoPath) { this.siteInfoPath = siteInfoPath; }
  public String getIpInfoPath() { return ipInfoPath; }
  public void setIpInfoPath(String ipInfoPath) { this.ipInfoPath = ipInfoPath; }
  public String getCronExpression() { return cronExpression; }
  public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
}
