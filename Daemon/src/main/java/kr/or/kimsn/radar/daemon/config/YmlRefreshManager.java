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
  private String finalYmlPath = "/home/watcher/deamon/config/application.yml"; // 최종 확정된 물리 경로 Holder
  private WatchService watchService;
  private Thread watchThread;

  public YmlRefreshManager(ConfigurableEnvironment environment) {
    this.environment = environment;
  }

  @PostConstruct
  public void startWatching() {
    // 💡 [방어선 1] 다중 대안 경로 추적 스캔 가동 (Scheduler 구조와 정합성 일치)
    String[] candidatePaths = {
      "/home/watcher/deamon/config/application.yml",
      "/home/watcher/daemon/config/application.yml",
      "../config/application.yml",
      "./config/application.yml",
      "./Daemon/config/application.yml"
    };

    File ymlFile = null;
    for (String path : candidatePaths) {
      File checkFile = new File(path);
      if (checkFile.exists() && checkFile.isFile()) {
        ymlFile = checkFile;
        this.finalYmlPath = path; // 실제 존재하는 경로로 확정 바인딩
        break;
      }
    }

    if (ymlFile == null) {
      System.out.println("[YmlWatcher] ⚠️ 외부 application.yml 파일 실실적 부재로 실시간 동적 감지를 생략합니다.");
      return;
    }

    // 💡 [핵심 해결] 최초 데몬 구동 순간, 백그라운드 스레드가 돌기 전에 외부 YML을 메모리에 최우선으로 강제 1회 구워버림!
    System.out.println("[YmlWatcher] ⚙️ 데몬 최초 초기화 단계 외부 YML 프리로드 동기화 가동: " + finalYmlPath);
    reloadYml();

    // 변동 감시 백그라운드 쓰레드 기동
    watchThread = new Thread(() -> {
      try {
        watchService = FileSystems.getDefault().newWatchService();
        
        // 💡 [방어선 2] 파일이 속한 실제 디렉토리를 추출하여 감시 대상을 정밀 타겟팅함
        Path parentDir = Paths.get(finalYmlPath).getParent().toAbsolutePath().normalize();
        parentDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        System.out.println("[YmlWatcher] 🚀 자바 표준 WatchService 기반 외부 YML 실시간 감지 엔진 스타트 완료. 감시폴더: " + parentDir);

        while (!Thread.currentThread().isInterrupted()) {
          WatchKey key = watchService.take(); 

          for (WatchEvent<?> event : key.pollEvents()) {
            Path changedFile = (Path) event.context();
            if ("application.yml".equals(changedFile.toString())) {
              System.out.println("[YmlWatcher] 🔔 외부 application.yml 실시간 변경 감지! 메모리 갱신을 수행합니다.");
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

    watchThread.setDaemon(true); 
    watchThread.setName("Yml-Watcher-Thread");
    watchThread.start();
  }

  private synchronized void reloadYml() {
    try {
      Resource resource = new FileSystemResource(finalYmlPath);
      YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
      List<PropertySource<?>> propertySources = loader.load("external-yml", resource);

      if (!propertySources.isEmpty()) {
        // 스프링 메모리 최상단 순위에 강제 덮어쓰기 완료
        environment.getPropertySources().addFirst(propertySources.get(0));
        System.out.println("[YmlWatcher] ✅ 외부 YML 데이터를 스프링 런타임 최상단 메모리에 강제 이식 성공!");
      }
    } catch (Exception e) {
      System.err.println("[YmlWatcher] 외부 YML 실시간 로드 실패: " + e.getMessage());
    }
  }

  @PreDestroy
  public void stopWatching() {
    if (watchThread != null) watchThread.interrupt();
    if (watchService != null) {
      try { watchService.close(); } catch (IOException e) { System.err.println(e.getMessage()); }
    }
  }
}
