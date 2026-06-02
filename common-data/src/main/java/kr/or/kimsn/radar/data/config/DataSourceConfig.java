package kr.or.kimsn.radar.data.config; // 변경하신 패키지명에 맞게 확인하세요.

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import javax.sql.DataSource;

@Configuration
// common-data.yml 파일을 강제로 로드합니다. (YamlPropertySourceFactory 기존 코드 유지)
@PropertySource(value = "classpath:common-data.yml", factory = YamlPropertySourceFactory.class)
public class DataSourceConfig {

  // 💡 common-data.yml에 작성하신 config.datasource.url 값을 직접 변수로 가져옵니다.
  @Value("${config.datasource.url}")
  private String dbUrl;

  @Value("${config.datasource.driver-class-name}")
  private String driverClassName;

  @Value("${config.datasource.username}")
  private String username;

  @Value("${config.datasource.password}")
  private String password;

  @Bean
  public DataSource dataSource() {
    HikariConfig hikariConfig = new HikariConfig();

    // 💡 꼬여있던 표준 규격을 코드 레벨에서 강제로 매핑합니다.
    hikariConfig.setDriverClassName(driverClassName);
    hikariConfig.setJdbcUrl(dbUrl); //  이 부분이 핵심입니다. (url을 jdbcUrl로 주입)
    hikariConfig.setUsername(username);
    hikariConfig.setPassword(password);

    // 추가로 yml에 적어두신 커넥션 옵션들도 명시적으로 바인딩 가능합니다.
    hikariConfig.setMaximumPoolSize(1);
    hikariConfig.setMaxLifetime(870000);
    hikariConfig.setConnectionTimeout(15000);
    hikariConfig.setValidationTimeout(5000);

    return new HikariDataSource(hikariConfig);
  }
}
