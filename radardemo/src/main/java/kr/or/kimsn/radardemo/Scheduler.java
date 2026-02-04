package kr.or.kimsn.radardemo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.or.kimsn.radardemo.common.DataCommon;
import kr.or.kimsn.radardemo.dto.StationDto;
import kr.or.kimsn.radardemo.process.StepOneProcess;
import kr.or.kimsn.radardemo.process.StepOneProcessTwo;
import kr.or.kimsn.radardemo.process.StepTwoProcess;
// import kr.or.kimsn.radardemo.process.StepThreeProcess;
import kr.or.kimsn.radardemo.service.QueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class Scheduler {

    private final QueryService queryService;

    private List<StationDto> srDto;

    // * * * * * *
    // 초(0-59) 분(0-59) 시간(0-23) 일(1-31) 월(1-12) 요일(0-7)
    // 대형, 공항
//    @Scheduled(cron = "0 1,6,11,16,21,26,31,36,41,46,51,56 * * * *") // 6분 00초
    // 소형
     @Scheduled(cron = "30 0-59 * * * *") // 매분 30초
    // @Scheduled(fixedDelay = 30000) // 30초마다
    @Async
    public void cronJobSch() throws InterruptedException {

        int PauseTime = Integer.parseInt(DataCommon.getInfoConf("ipInfo", "PauseTime"));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String mode = DataCommon.getInfoConf("siteInfo", "mode");
        int gubun = Integer.parseInt(DataCommon.getInfoConf("siteInfo", "gubun"));

        String gubunStr = ""; // 데몬 구분
        int srCnt = 0; // for문 site 갯수

        if (gubun == 1)
            gubunStr = "대형";
        if (gubun == 2)
            gubunStr = "소형";
        if (gubun == 3)
            gubunStr = "공항";
        log.info("[데몬 구분] : " + gubunStr);

        if (!mode.equals("test")) {
            srDto = queryService.getStation(gubun);
            srCnt = srDto.size();
        }

        StepOneProcessTwo stepone = new StepOneProcessTwo(queryService);

        // Executor exec = Executors.newFixedThreadPool(srCnt);
        ExecutorService exec = Executors.newCachedThreadPool(); // 60초 동안 아무일도 하지 않으면 유휴상태라 판단 쓰레드 제거
        // ExecutorService exec = new ThreadPoolExecutor(3, 200, 120, TimeUnit.SECONDS,
        // new SynchronousQueue<>());

        for (int a = 0; a < srCnt; a++) {
            int cnt = a;
            Runnable task = new Runnable() {
                public void run() {
                    stepone.stepOne(mode, gubun, srDto.get(cnt));

                }
            };
            // exec.execute(task);
            exec.submit(task);
            Thread.sleep(500);
        }
        exec.shutdown();

        Thread.sleep(PauseTime * 1000); // 20초
        log.info("[" + PauseTime + "초 후 다음] : " + LocalDateTime.now().format(dtf));
        log.info("[=================== 2번째 프로세스 ===================] " + LocalDateTime.now().format(dtf));
        StepTwoProcess twoProc = new StepTwoProcess(queryService);
        twoProc.stepTwo(gubunStr);
        twoProc = null;
        log.info("[=================== end ===================]");
    }

}
