package kr.or.kimsn.radar.daemon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "kr.or.kimsn")
@EnableJpaRepositories(basePackages = "kr.or.kimsn")
@EntityScan(basePackages = "kr.or.kimsn")
public class DaemonApplication {

  public static void main(String[] args) {
    String systemType = "LOCAL_CORE"; // 디폴트 개발 환경 식별자

    // 💡 [핵심 인프라 훅] 로그 엔진이 초기화되기 전에 실행 Jar 매니페스트 식별자를 추출하여 박제합니다.
    try {
      URL manifestUrl = DaemonApplication.class.getClassLoader().getResource("META-INF/MANIFEST.MF");
      if (manifestUrl != null) {
        Manifest manifest = new Manifest(manifestUrl.openStream());
        Attributes attributes = manifest.getMainAttributes();
        String jarSysType = attributes.getValue("Radar-System-Type");
        if (jarSysType != null && !jarSysType.trim().isEmpty()) {
          systemType = jarSysType.trim();
        }
      }
    } catch (Exception e) {
      System.err.println("[⚙️ 로그 초기화] 시스템 식별자 추출 보류 -> LOCAL 모드로 변수를 할당합니다.");
    }

    // 💥 Log4j2가 읽어갈 시스템 환경 프로퍼티로 할당 (동적 로그 분기선 완성)
    System.setProperty("daemonLogName", systemType);

    // 스프링 부트 최종 구동 가동
    SpringApplication.run(DaemonApplication.class, args);
  }
}
