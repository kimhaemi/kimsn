package kr.or.kimsn.radar.daemon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.or.kimsn.radar.daemon.util.ConfigManager;
import kr.or.kimsn.radar.data.dto.StationDto;
import kr.or.kimsn.radar.daemon.process.StepOneProcess;
import kr.or.kimsn.radar.daemon.process.StepTwoProcess;
import kr.or.kimsn.radar.daemon.service.QueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class Scheduler {

    private final QueryService queryService;

    // StepOne 중복 실행 방지용 (혹시 1분 주기가 왔는데 저번 주기 스레드 풀 할당 조차 안 끝났을 때만 방어)
    private final AtomicBoolean isStepOneRunning = new AtomicBoolean(false);

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * [프로세스 1] 파일 다운로드 및 수집 (매분 0초 또는 30초 정각)
     * 파일을 끊지 않고 끝까지 안전하게 읽어오는 전용 스케줄러입니다.
     */
    @Scheduled(cron = "#{@radarConfigProperties.cronExpression}")
    @Async
    public void runStepOneCollection() throws InterruptedException {
        if (!isStepOneRunning.compareAndSet(false, true)) {
            log.warn("[StepOne] 일부 서버 파일 수집이 아직 진행 중이므로 새 수집 스레드 풀 생성을 제한합니다.");
            return;
        }

        ExecutorService exec = null;
        try {
            int gubun = ConfigManager.getInt("gubun");
            String mode = ConfigManager.getString("mode");

            log.info("[StepOne 시작] 레이더 파일 수집 시작 - {}", LocalDateTime.now().format(dtf));

            List<StationDto> srDto = new ArrayList<>();
            if (!"test".equalsIgnoreCase(mode)) {
                srDto = queryService.getStation(gubun);
            }

            if (srDto.isEmpty()) return;

            StepOneProcess stepone = new StepOneProcess(queryService);
            exec = Executors.newFixedThreadPool(srDto.size());

            for (StationDto currentStation : srDto) {
                // 절대 강제로 종료시키지 않고 백그라운드에서 끝까지 파일을 읽도록 내버려 둡니다.
                exec.submit(() -> stepone.stepOne(mode, gubun, currentStation, "placeholder", "placeholder"));
                Thread.sleep(50);
            }

            exec.shutdown();
            // 넉넉하게 대기 시간을 줍니다. (예: 5분 대기해도 1분 뒤 다음 스케줄러 실행에 지장 없음)
            exec.awaitTermination(5, TimeUnit.MINUTES);

        } finally {
            isStepOneRunning.set(false);
            log.info("[StepOne 종료] 모든 서버의 가능한 파일 수집 세션 종료 - {}", LocalDateTime.now().format(dtf));
        }
    }

    /**
     * [프로세스 2] 데이터 가공 및 후처리 (StepOne과 분리하여 독립 구동)
     * 주기적으로 돌며 수집이 완료된 데이터들을 파싱하고 비즈니스 로직을 수행합니다.
     * 크론식은 yml에서 각 장비 주기에 맞춰 적절한 타이밍(예: 수집 시작 후 40초 뒤 등)으로 설정 가능합니다.
     */
    @Scheduled(cron = "#{@radarConfigProperties.stepTwoCronExpression}") // yml에 추가 정의 필요
    @Async
    public void runStepTwoProcessing() {
        String gubunStr = ConfigManager.getGubunStr();

        log.info("[=================== StepTwo 프로세스 시작 ===================] {}", LocalDateTime.now().format(dtf));
        try {
            StepTwoProcess twoProc = new StepTwoProcess(queryService);
            // 수집이 '이미 완료되어 쌓인' 파일들만 안전하게 마저 처리합니다.
            twoProc.stepTwo(gubunStr, "placeholder", "placeholder");
        } catch (Exception e) {
            log.error("StepTwo 처리 중 에러 발생", e);
        }
        log.info("[=================== StepTwo 프로세스 종료 ===================] {}", LocalDateTime.now().format(dtf));
    }
}
