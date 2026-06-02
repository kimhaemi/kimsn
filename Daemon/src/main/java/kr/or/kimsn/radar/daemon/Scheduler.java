package kr.or.kimsn.radar.daemon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import kr.or.kimsn.radar.daemon.enums.RadarTypeEnum;
import org.slf4j.MDC;
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

    // 1분 주기가 겹쳤을 때 데이터가 뒤섞이거나 쓰레드가 폭발하는 것을 막는 방어막
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter logDtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * [통합 데몬 스케줄러]
     * 1분마다 정각(또는 30초)에 켜져서 StepOne(병렬 메타조회) -> pauseTime 대기 -> StepTwo(후처리)를 순차 실행합니다.
     */
    @Scheduled(cron = "#{@radarConfigProperties.cronExpression}")
    @Async
    public void cronJobSch() throws InterruptedException {

        // 1. 혹시라도 이전 주기가 1분을 넘겨 처리 중이라면 이번 주기는 통째로 건너뛰어 안전을 보장합니다.
        if (!isRunning.compareAndSet(false, true)) {
            log.warn("[⚠️스킵] 이전 사이클 작업이 1분 내에 끝나지 않아 이번 주기를 건너뜁니다. 레이더 서버 응답을 확인하세요.");
            return;
        }

        // 로그백 SiftingAppender와 연동되어 주기별로 로그 파일을 분리할 고유 식별자 (예: 20260529_110030)
        String cycleId = LocalDateTime.now().format(logDtf);
        MDC.put("cycleId", cycleId);

        ExecutorService exec = null;
        try {
            final long pauseTime = ConfigManager.getLong("PauseTime"); // 예: 20초
            int gubun = ConfigManager.getInt("gubun");
            RadarTypeEnum radarType = RadarTypeEnum.find(gubun);
            String mode = ConfigManager.getString("mode");
            String gubunStr = radarType.getKrName();

            log.info("[🚀 사이클 시작] ID: [{}] | 장비타입: {} - {}", cycleId, gubunStr, LocalDateTime.now().format(dtf));

            List<StationDto> srDto = new ArrayList<>();
            if (!"test".equalsIgnoreCase(mode)) {
                srDto = queryService.getStation(gubun);
            }

            if (srDto.isEmpty()) {
                log.info("[사이클 종료] ID: [{}] - 처리할 레이더 스테이션이 없습니다.", cycleId);
                return;
            }

            // ----------------------------------------------------------------
            // [💥 1단계: StepOne - 레이더 서버 접속 및 메타데이터 수집]
            // ----------------------------------------------------------------
            log.info("[▶️ StepOne 시작] 각 서버 병렬 메타데이터 조회 시작 (ID: [{}])", cycleId);
            StepOneProcess stepone = new StepOneProcess(queryService);

            // 레이더 장비 개수만큼 딱 고정된 크기의 최적화된 쓰레드 풀 생성
            exec = Executors.newFixedThreadPool(srDto.size());

            for (StationDto currentStation : srDto) {
                exec.submit(() -> {
                    MDC.put("cycleId", cycleId); // 비동기 자식 쓰레드에도 로그 격리 식별자 주입
                    try {
                        stepone.stepOne(mode, gubun, currentStation, cycleId, cycleId);
                    } finally {
                        MDC.clear();
                    }
                });
                Thread.sleep(50); // 순간적인 접속 부하 분산 마진
            }

            exec.shutdown();

            // 1분 완주 마지노선을 넘지 않도록 최대 35초만 응답을 기다립니다.
            // 35초가 지나도 대답 없는 불통 서버가 있다면 자바가 대기줄을 깨고 빠져나옵니다.
            boolean allFinished = exec.awaitTermination(35, TimeUnit.SECONDS);

            if (!allFinished) {
                log.warn("[⏰ 타임아웃 발생] ID: [{}] 일부 서버 응답 지연으로 35초 도달하여 대기 강제 해제", cycleId);
            }
            log.info("[🏁 StepOne 완료] 수집 세션 종료 (ID: [{}])", cycleId);

            // ----------------------------------------------------------------
            // [💤 2단계: 비즈니스 요구 규격에 따른 일시정지]
            // ----------------------------------------------------------------
            Thread.sleep(pauseTime * 1000);
            log.info("[{}초 대기 후 다음 단계 진행] - {}", pauseTime, LocalDateTime.now().format(dtf));

            // ----------------------------------------------------------------
            // [📊 3단계: StepTwo - 데이터 최종 가공 및 후처리 DB 적재]
            // ----------------------------------------------------------------
            log.info("[=================== ▶️ StepTwo 프로세스 시작 (ID: [{}]) ===================]", cycleId);
            StepTwoProcess twoProc = new StepTwoProcess();

            // 35초 타임아웃 덕분에 정상적으로 수집 완료가 찍힌 데이터들만 안전하게 마저 정산 처리합니다.
            twoProc.stepTwo(gubunStr, cycleId, cycleId);

            log.info("[=================== 🏁 StepTwo 프로세스 종료 (ID: [{}]) ===================]", cycleId);

        } catch (Exception e) {
            log.error("[❌ 사이클 오류] 스케줄러 실행 중 예상치 못한 치명적 예외 발생 (ID: [{}])", cycleId, e);
        } finally {
            isRunning.set(false); // 무조건 락을 해제하여 다음 1분 주기가 켜질 수 있도록 보장
            log.info("[🏁 사이클 최종 완료] ID: [{}] - {}", cycleId, LocalDateTime.now().format(dtf));
            MDC.clear(); // 메인 쓰레드 로그 컨텍스트 메모리 초기화
        }
    }
}
