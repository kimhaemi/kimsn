package kr.or.kimsn.radar.daemon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import java.util.Objects;

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
   * 💡 검증 보완: 대소문자 방어 코드 및 필수값 검증 추가
   */
  public String getDynamicPath(String systemType, String infoType) {
    // 1. 대문자 변환을 통해 'kma_rdr' 호출 실수 방어
    String formattedSystemType = Objects.requireNonNull(systemType).toUpperCase();
    
    String key = "radar.config.paths." + formattedSystemType + "." + infoType;
    String value = environment.getProperty(key);
    
    // 2. 운영 환경에서 yml 수정 실수로 값이 누락되었을 때의 방어 로직
    if (value == null) {
        throw new IllegalArgumentException(String.format("지정된 프로퍼티를 찾을 수 없습니다. Key: [%s]", key));
    }
    
    return value;
  }
}
