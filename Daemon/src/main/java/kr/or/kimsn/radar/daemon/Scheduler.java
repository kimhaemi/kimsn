package kr.or.kimsn.radar.daemon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import kr.or.kimsn.radar.daemon.config.RadarConfigProperties;
import kr.or.kimsn.radar.daemon.enums.DataKindEnum;
import kr.or.kimsn.radar.daemon.util.ConfigManager;
import kr.or.kimsn.radar.daemon.util.DataCommon;
import kr.or.kimsn.radar.data.dto.StationDto;
import kr.or.kimsn.radar.daemon.service.StepOneService;
import kr.or.kimsn.radar.daemon.service.StepTwoService;
import kr.or.kimsn.radar.daemon.service.QueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class Scheduler {

    private final QueryService queryService;
    private final StepOneService stepOneService;
    private final StepTwoService stepTwoService;
    private final RadarConfigProperties radarConfigProperties;

    // 💡 [요구사항 반영]: 이전 사이클의 미종료 여부와 상관없이 매분 강제 실행하기 위해 AtomicBoolean 락을 완전 제거합니다.

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter logDtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * [통합 데몬 스케줄러 - 100% 무조건 강제 실행 모드]
     * 지정된 크론 주기(표준 1분 또는 1분 지연 5분 그리드 타임라인)에 매 세션 무조건 독립 호출 구동됩니다.
     */
    @Scheduled(cron = "#{@radarConfigProperties.cronExpression}")
    @Async
    public void cronJobSch() throws InterruptedException {

        // 💡 각 사이클이 서로 방해받지 않고 로그가 완벽하게 분리 격리 보존되도록 Cycle ID를 메인 스레드에 선제 주입
        String cycleId = LocalDateTime.now().format(logDtf);
        MDC.put("cycleId", cycleId);

        ExecutorService exec = null;
        String siteInfoPath = radarConfigProperties.getSiteInfoPath();
        String ipInfoPath = radarConfigProperties.getIpInfoPath();

        try {
            final long pauseTime = ConfigManager.getLong("PauseTime");
            int gubun = ConfigManager.getInt("gubun");
            String dataKindStr = DataKindEnum.getDescriptionByGubun(gubun);
            String mode = ConfigManager.getString("mode");
            String gubunStr = ConfigManager.getGubunStr();

            log.info("[🚀 강제 사이클 가동 시작] ID: [{}] | 장비타입: {} - {}", cycleId, gubunStr, LocalDateTime.now().format(dtf));

            List<StationDto> srDto = new ArrayList<>();
            if (!"test".equalsIgnoreCase(mode)) {
                srDto = queryService.getStation(gubun);
            }

            if (srDto == null || srDto.isEmpty()) {
                log.info("[사이클 즉시 마감] ID: [{}] - 가동 처리할 타겟 레이더 스테이션 정보가 존재하지 않습니다.", cycleId);
                return;
            }

            // ----------------------------------------------------------------
            // [💥 1단계: StepOne - 레이더 서버 접속 및 메타데이터 수집]
            // ----------------------------------------------------------------
            log.info("[▶️ StepOne 시작] 독립 병렬 수집 세션 풀 가동 (ID: [{}])", cycleId);

            // 각 주기(매분)마다 독립된 크기의 고정 스레드 풀을 동적으로 생성하여 이전 주기 스레드와 병합 경합 차단
            exec = Executors.newFixedThreadPool(srDto.size());

            for (StationDto currentStation : srDto) {
                exec.submit(() -> {
                    MDC.put("cycleId", cycleId); // 비동기 자식 자바 스레드 내부에 고유 cycleId 전파 격리
                    try {
                        stepOneService.stepOne(mode, gubun, currentStation, siteInfoPath, ipInfoPath);
                    } finally {
                        MDC.clear();
                    }
                });
                Thread.sleep(50); // 네트워크 순간 유입 부하 분산 마진
            }

            exec.shutdown();

            // 설정에서 타임아웃 값을 유연하게 탈환하되 실패 시 기본 안전 대기 마지노선 35초 적용
            int connectTimeOut = 35;
            try {
                connectTimeOut = Integer.parseInt(DataCommon.getInfoConf("ipInfo", "connectTimeOut", siteInfoPath, ipInfoPath));
            } catch (Exception e) {
                log.warn("[⚙️ 설정 정보 알림] connectTimeOut 파싱 보류로 기본 마지노선 35초를 대기 규격으로 고수합니다.");
            }

            // ⚠️ 중요: 이전 주기가 끝나지 않았더라도 현재 스레드 풀은 최대지정 초(예: 35초)만 딱 기다리고 강제로 대기를 해제하여 빠져나옵니다.
            boolean allFinished = exec.awaitTermination(connectTimeOut, TimeUnit.SECONDS);
            if (!allFinished) {
                log.warn("[⏰ 타임아웃 강제돌파] ID: [{}] 일부 응답 지연 장비가 존재하나 {}초 도달로 대기를 해제하고 정산으로 진입합니다.", cycleId, connectTimeOut);
            }
            log.info("[🏁 StepOne 완료] 이번 주기 병렬 수집 종료 (ID: [{}])", cycleId);

            // ----------------------------------------------------------------
            // [💤 2단계: 비즈니스 요구 규격에 따른 일시정지]
            // ----------------------------------------------------------------
            log.info("[💤 정산 지연 마진 대기] 지정된 {}초 정지 후 후처리 정산을 시작합니다.", pauseTime);
            Thread.sleep(pauseTime * 1000);

            // ----------------------------------------------------------------
            // [📊 3단계: StepTwo - 데이터 최종 가공 및 후처리 DB 적재]
            // ----------------------------------------------------------------
            log.info("[=================== ▶️ StepTwo 프로세스 시작 (ID: [{}]) ===================]", cycleId);
            // 💡 락이 풀렸으므로 이전 주기 정산 결과에 지장 없이 현재 시점의 누적 3회 수집 이력을 정밀 대조하여 마스터 상태를 갱신합니다.
            stepTwoService.stepTwo(gubunStr, cycleId, cycleId);
            log.info("[=================== 🏁 StepTwo 프로세스 종료 (ID: [{}]) ===================]", cycleId);

        } catch (Exception e) {
            log.error("[❌ 사이클 중단 장애] 스케줄러 처리 도중 예상치 못한 런타임 예외 감지 (ID: [{}])", cycleId, e);
        } finally {
            log.info("[🏁 사이클 최종 완료 완료] ID: [{}] - {}", cycleId, LocalDateTime.now().format(dtf));
            MDC.clear(); // 메인 스레드 메모리 청소
        }
    }
}
