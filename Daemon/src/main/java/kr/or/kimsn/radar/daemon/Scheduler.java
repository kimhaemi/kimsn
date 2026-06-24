package kr.or.kimsn.radar.daemon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kr.or.kimsn.radar.data.dto.StationDto;
import kr.or.kimsn.radar.daemon.service.StepOneService;
import kr.or.kimsn.radar.daemon.service.StepTwoService;
import kr.or.kimsn.radar.daemon.service.QueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class Scheduler implements SchedulingConfigurer { // 💡 무중단 동적 크론 추적 인터페이스 탑재

    private final QueryService queryService;
    private final ConfigurableEnvironment environment; 
    private final StepOneService stepOneService;
    private final StepTwoService stepTwoService;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DateTimeFormatter cycleIdFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 💡 [핵심 인프라] 스프링 고유 스케줄러 레지스트리에 주기를 동적으로 주입합니다.
     * 이 구조는 매 주기가 끝날 때마다 외부 YML 리프레시 메모리에서 실시간 크론식을 재추적합니다.
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
            () -> {
                try {
                    // 비동기 코어 엔진으로 실시간 주행 트리거 토스
                    cronJobSch();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("[🚨 스케줄러 트랙커] 주행 중 인터럽트 예외 발생", e);
                }
            },
            triggerContext -> {
                // 1. 기동 시점 및 런타임 환경변수 실시간 상시 정제
                String rawDaemonType = System.getProperty("daemonLogName");
                if (rawDaemonType == null || "LOCAL_CORE".equals(rawDaemonType) || rawDaemonType.trim().isEmpty()) {
                    rawDaemonType = System.getProperty("DAEMON_TYPE");
                }
                if (rawDaemonType == null || rawDaemonType.trim().isEmpty()) {
                    rawDaemonType = "KMA_RDR";
                }
                String daemonType = rawDaemonType.toUpperCase().replace("-", "_").trim();

                // 2. ⭐ [무중단 변경의 심장] YmlRefreshManager가 갱신해 준 최신 메모리에서 크론식을 실시간 스캔!
                String cronPath = "radar.config.paths." + daemonType + ".cron-expression";
                String cronExpression = environment.getProperty(cronPath, "30 * * * * *");

                // 3. 다음 수집 주행 스케줄 타임라인을 동적으로 재계산하여 이식
                CronTrigger trigger = new CronTrigger(cronExpression);
                return trigger.nextExecutionTime(triggerContext);
            }
        );
    }

    /**
     * 비즈니스 파이프라인 (기존 @Scheduled, @Async 구조 걷어내고 순수 자바 멀티스레드 안정 가동)
     */
    public void cronJobSch() throws InterruptedException {
        String rawDaemonType = System.getProperty("daemonLogName");
        if (rawDaemonType == null || "LOCAL_CORE".equals(rawDaemonType) || rawDaemonType.trim().isEmpty()) {
            rawDaemonType = System.getProperty("DAEMON_TYPE");
        }
        
        // 인프라 식별자 최종 실종 시 데이터 오염 방지를 위해 프로세스 강제 커널 다운 가드 유지
        if (rawDaemonType == null || rawDaemonType.trim().isEmpty()) {
            log.error("[🚨 치명적 런타임 오류] 데몬 구동 시스템 식별자(DAEMON_TYPE) 유실로 프로세스를 전체 강제 종료합니다.");
            System.exit(1);
            return;
        }
        
        // 💡 람다 캡처링 자바 문법 규격을 만족시키기 위해 변수를 완벽히 정제 후 final 상수에 최종 박제
        final String daemonType = rawDaemonType.toUpperCase().replace("-", "_").trim();
        String globalPrefix = "radar.config.global.";
        
        log.info("[🎯 실시간 수집 동기화 주행 기동] 데몬 타입: " + daemonType);
        
        String mode = environment.getProperty("radar.config.global.mode", "real");
        // 💡 [핵심 해결] 현재 가동 중인 daemonType 텍스트에 "MCEE"가 포함되어 있으면 "MCEE", 아니면 "KMA"를 강제 할당!
        // 이렇게 처리하면 application.yml 내부에 별도로 agencyCd 속성을 명시하지 않아도 완벽하게 자동 분기됩니다.
        final String agencyCd = daemonType.contains("MCEE") ? "MCEE" : "KMA";

        log.info("[mode]: "+ mode);
        log.info("[⚙️ 수집 인프라 감지] 현재 주행 대상 기관 코드(agencyCd): {}", agencyCd);

        final int gubun;
        final String gubunStr;
        if (daemonType.contains("SDR")) {
            gubun = 2;
            gubunStr = "소형";
        } else if (daemonType.contains("TDWR")) {
            gubun = 3;
            gubunStr = "공항";
        } else {
            gubun = 1;
            gubunStr = "대형";
        }
        log.info("[데몬 구분] : " + gubunStr);

        String pauseTimeStr = environment.getProperty(globalPrefix + "PauseTime", "20");
        long pauseTime = Long.parseLong(pauseTimeStr);

        int srCnt = 0; 
        List<StationDto> rawSrDto = null;
        
        // 관측소 조회
        rawSrDto = queryService.getStation(gubun, agencyCd, 1);
        if (rawSrDto != null) {
            srCnt = rawSrDto.size();
        }
        
        // 💡 [널 포인터 가드 완비] 잠재적 널 포인터 액세스(Java 536871364) 경고를 원천 제거하기 위한 삼항 연산자 세척
        final List<StationDto> finalSrDto = (rawSrDto != null) ? rawSrDto : java.util.List.of();
        ExecutorService exec = Executors.newCachedThreadPool(); 

        for (int a = 0; a < srCnt; a++) {
            final int cnt = a;
            // 완전히 보정된 불변 상수를 전달하므로 스레드 안전성이 100% 보장됩니다.
            Runnable task = () -> stepOneService.stepOne(mode, gubun, finalSrDto.get(cnt), daemonType);
            exec.submit(task);
            Thread.sleep(500);
        }
        exec.shutdown();

        log.info("[" + pauseTime + "초 후 다음 정산 처리] : " + LocalDateTime.now().format(dtf));
        Thread.sleep(pauseTime * 1000); 
        log.info("[{}초 후 시간]: {}", pauseTime, LocalDateTime.now().format(dtf));
        log.info("[=================== 2번째 프로세스 상태 판정 진입 ===================] " + LocalDateTime.now().format(dtf));
        
        String cycleId = LocalDateTime.now().format(cycleIdFormatter);
        stepTwoService.stepTwo(gubun, gubunStr, cycleId, daemonType);
        
        log.info("[=================== 정산 루프 최종 완료 ===================]");
    }
}
