package kr.or.kimsn.radar.daemon.config;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Component
public class YmlRefreshManager {

  private final ConfigurableEnvironment environment;
  private final String YML_FILE_PATH = "./Daemon/config/application.yml"; // JAR 파일과 동일한 경로의 외부 YML 파일
  private WatchService watchService;
  private Thread watchThread;

  public YmlRefreshManager(ConfigurableEnvironment environment) {
    this.environment = environment;
  }

  @PostConstruct
  public void startWatching() {
    File ymlFile = new File(YML_FILE_PATH);
    if (!ymlFile.exists()) {
      System.out.println("[YmlWatcher] ⚠️ 외부 application.yml 파일이 존재하지 않아 실시간 동적 감지를 생략합니다.");
      return;
    }

    // 별도의 데몬 백그라운드 쓰레드를 생성하여 파일 변동을 감시합니다.
    watchThread = new Thread(() -> {
      try {
        // 자바 순수 커널 이벤트 감시 서비스 생성
        watchService = FileSystems.getDefault().newWatchService();
        Path path = Paths.get(".").toAbsolutePath().normalize(); // 현재 폴더 기준

        // 파일이 수정(ENTRY_MODIFY)되는 이벤트를 등록합니다.
        path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        System.out.println("[YmlWatcher] 🚀 자바 표준 WatchService 기반 외부 YML 실시간 감지 엔진 스타트 완료.");

        while (!Thread.currentThread().isInterrupted()) {
          WatchKey key = watchService.take(); // 파일 변경 이벤트가 올 때까지 쓰레드가 가만히 대기 (CPU 소모 0%)

          for (WatchEvent<?> event : key.pollEvents()) {
            Path changedFile = (Path) event.context();
            // 수정한 파일 이름이 정확히 application.yml 일 때만 트리거 작동
            if ("application.yml".equals(changedFile.toString())) {
              System.out.println("[YmlWatcher] 🔔 외부 application.yml 실시간 변경 감지! 메모리 갱신을 수행합니다.");

              // 파일 쓰기가 완전히 완료될 때까지 미세한 동기화 마진 부여
              Thread.sleep(500);
              reloadYml();
            }
          }

          boolean valid = key.reset();
          if (!valid) break;
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (IOException e) {
        System.err.println("[YmlWatcher] 파일 감시 서비스 실행 중 입출력 오류: " + e.getMessage());
      }
    });

    watchThread.setDaemon(true); // 애플리케이션 종료 시 함께 죽도록 데몬 쓰레드로 설정
    watchThread.setName("Yml-Watcher-Thread");
    watchThread.start();
  }

  /**
   * [핵심] 수정한 외부 YML 파일 내용을 파싱하여 현재 돌아가는 스프링 내부 환경변수 메모리 최상단에 덮어씁니다.
   */
  private synchronized void reloadYml() {
    try {
      Resource resource = new FileSystemResource(YML_FILE_PATH);
      YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

      // 외부 YML 파일 데이터 로드
      List<PropertySource<?>> propertySources = loader.load("external-yml", resource);

      if (!propertySources.isEmpty()) {
        // 스프링의 기동 환경 프로퍼티 소스 목록 중 '가장 첫 번째(addFirst)' 우선순위로 강제 주입
        environment.getPropertySources().addFirst(propertySources.get(0));
        System.out.println("[YmlWatcher] ✅ 스프링 내부 환경 변수 실시간 동적 교체 성공!");
      }
    } catch (Exception e) {
      System.err.println("[YmlWatcher] 외부 YML 실시간 로드 실패: " + e.getMessage());
    }
  }

  @PreDestroy
  public void stopWatching() {
    if (watchThread != null) {
      watchThread.interrupt();
    }
    if (watchService != null) {
      try {
        watchService.close();
      } catch (IOException e) {
        System.err.println("[YmlWatcher] 감시 서비스 닫기 실패: " + e.getMessage());
      }
    }
  }
}
