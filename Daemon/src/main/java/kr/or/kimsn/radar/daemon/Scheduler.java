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
import org.springframework.beans.factory.annotation.Value;
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
    private final RadarConfigProperties radarConfigProperties;
    
    // 💡 핵심 해결: 수동 'new' 생성을 제거하고, 스프링이 주입해 준 빈을 사용합니다.
    // 이렇게 해야 StepTwoProcess 내부의 @Transactional과 SmsService가 정상 작동하며 컴파일 에러가 해결됩니다.
    private final StepOneService stepOneService;
    private final StepTwoService stepTwoService;

//    @Value("${DAEMON_TYPE}")
//    private String daemonType;

    private List<StationDto> srDto;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DateTimeFormatter cycleIdFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

//    @Scheduled(cron = "#{@radarConfigProperties.getDynamicPath(environment.getProperty('KMA_RDR'), 'cron-expression')}")
    @Scheduled(cron= "* * * * * *")
    @Async
    public void cronJobSch() throws InterruptedException {
        String daemonType = "KMA_RDR";

        String siteInfoPath = radarConfigProperties.getDynamicPath(daemonType, "site-info");
        String ipInfoPath = radarConfigProperties.getDynamicPath(daemonType, "ip-info");

        String pauseTimeStr = DataCommon.getInfoConf("ipInfo", "PauseTime", siteInfoPath, ipInfoPath);
        final Long pauseTime = (pauseTimeStr != null && !pauseTimeStr.isEmpty()) ? Long.parseLong(pauseTimeStr) : 20L; // 기본값 20초

        String mode = DataCommon.getInfoConf("siteInfo", "mode", siteInfoPath, ipInfoPath);
        String agencyCd = DataCommon.getInfoConf("siteInfo", "agencyCd", siteInfoPath, ipInfoPath);

        String gubunStr = DataCommon.getInfoConf("siteInfo", "gubun", siteInfoPath, ipInfoPath);
        int gubun = (gubunStr != null && !gubunStr.isEmpty()) ? Integer.parseInt(gubunStr) : 1; // 기본값 1(대형)

        log.info("[현재 실행 데몬 타입(YML 기준)] : " + daemonType);

        if (gubun == 1) gubunStr = "대형";
        if (gubun == 2) gubunStr = "소형";
        if (gubun == 3) gubunStr = "공항";
        log.info("[데몬 구분] : " + gubunStr);

        int srCnt = 0; 
        if (!mode.equals("test")) {
            srDto = queryService.getStation(gubun, agencyCd, 1);
            if (srDto != null) {
                srCnt = srDto.size();
            }
        }

        ExecutorService exec = Executors.newCachedThreadPool(); 

        for (int a = 0; a < srCnt; a++) {
            int cnt = a;
            Runnable task = () -> stepOneService.stepOne(mode, gubun, srDto.get(cnt), siteInfoPath, ipInfoPath);
            exec.submit(task);
            Thread.sleep(500);
        }
        exec.shutdown();

        Thread.sleep(pauseTime * 1000); 
        log.info("[" + pauseTime + "초 후 다음] : " + LocalDateTime.now().format(dtf));
        log.info("[=================== 2번째 프로세스 ===================] " + LocalDateTime.now().format(dtf));
        
        String cycleId = LocalDateTime.now().format(cycleIdFormatter);
        String placeholder = ""; 
        
        // 💡 핵심 해결: 주입받은 스프링 빈 인스턴스의 메서드를 직접 호출합니다.
        stepTwoService.stepTwo(gubunStr, cycleId, placeholder);
        
        log.info("[=================== end ===================]");
    }
}
