package kr.or.kimsn.radar.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// 다른 모듈의 패키지(kr.or.kimsn.radar)까지 모두 스캔하도록 설정
@SpringBootApplication(scanBasePackages = "kr.or.kimsn.radar")
@EnableJpaRepositories(basePackages = {"kr.or.kimsn.radar.data.repository"}) // DB 모듈의 Repository 패키지 경로
@EntityScan(basePackages = {"kr.or.kimsn.radar.data.dto"}) // DB 모듈의 Entity 패키지 경로
public class RadarWebApplication {
  public static void main(String[] args) {
    SpringApplication.run(RadarWebApplication.class, args);
  }
}
