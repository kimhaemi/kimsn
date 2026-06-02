package kr.or.kimsn.radar.daemon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   //스케줄링 활성화
@EnableAsync
public class RDRApplication {
  public static void main(String[] args) {
    SpringApplication.run(RDRApplication.class, args);
  }
}
