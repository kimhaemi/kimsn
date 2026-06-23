package kr.or.kimsn.radar.daemon.service;

import java.util.List;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

import kr.or.kimsn.radar.daemon.util.TimeUtil;
import kr.or.kimsn.radar.data.dto.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class StepTwoService {

    private final QueryService queryService;
    private final ConfigurableEnvironment environment;
    private final SmsService smsService; 

    private List<StationDto> srDto;

    @Transactional
    public void stepTwo(int gubun, String gubunStr, String cycleId, String daemonType) {
        String mode = environment.getProperty("radar.config.global.mode", "real");

        // MCEE 및 KMA 소속별 관측소 인프라 자동 분기 매핑
        final String agencyCd = daemonType.contains("MCEE") ? "MCEE" : "KMA";

        // TimeUtil 표준 0/5분 단위 절삭 보정 날짜 추출
        String currentTime = TimeUtil.getAdjustedCurrentTime(gubun, agencyCd);
        String dataKindStr = gubun == 1 ? "RDR" : gubun == 2 ? "SDR" : "TDWR";
        
        srDto = queryService.getStation(gubun, agencyCd, 1);
        int srCnt = (srDto != null) ? srDto.size() : 0;

        log.info("gubunL {}, : agencyCd: {}, srDto.size: {}", gubun, agencyCd, srDto.size());

        //기후부 소형은 대형 로직임.
        int new_gubun = (gubun == 1 || (gubun == 2 && agencyCd.equals("MCEE"))) ? 1 : (gubun == 2 && agencyCd.equals("KMA")) ? 2 : 3;

        log.info("new_gubun: {}", new_gubun);

        int all_site_network_no = 0;
        List<ReceiveConditionCriteriaDto> rccDtoList = queryService.getReceiveConditionCriteriaList(new_gubun);

        // 전 사이트 미수신 대규모 차단 상태 선제 판정 (공항 제외)
        if (!"test".equalsIgnoreCase(mode)) { 
            // if (gubun != 3) {
                for (int a = 0; a < srCnt; a++) {
                    String site_cd = srDto.get(a).getSiteCd();
                    List<ReceiveDataDto> rdDtoSingle = queryService.getReceiveDataList(site_cd, dataKindStr, 1);
                    for (ReceiveDataDto rd : rdDtoSingle) {
                        if ("MISS".equals(rd.getRecv_condition()) && "file_no".equals(rd.getCodedtl())) {
                            all_site_network_no++;
                        }
                    }
                // }
            }
        }

        // 관측소별 최종 마스터 상태 테이블(ReceiveCondition) 갱신 정산 루프
        for (int a = 0; a < srCnt; a++) {
            String new_recv_condition = "";
            String recv_con_dtl = "";
            String old_recv_condition = "";
            String apply_time = "";

            String site_cd = srDto.get(a).getSiteCd();
            String siteStr = srDto.get(a).getNameKr();

            log.info("[============ " + siteStr + " 정보 정산 진입 ==============]");

            if (all_site_network_no == srCnt) {
                log.info("[####### 전 사이트 네트워크 대규모 장애 감지 이력 update ########]");
                queryService.updateReceiveData("TOTA", "network_no", site_cd, dataKindStr, "NQC", "MISS", currentTime + ":00");
            }

            ReceiveConditionDto rcDto = queryService.getReceiveCondition(dataKindStr, "NQC", site_cd);
            log.info(">>> {} 관측소 최종 상태 : {}", siteStr, rcDto.getRecvCondition());

            old_recv_condition = rcDto.getRecvCondition();

            // 최종 상태가 TOTA 이면 네트워크 복구 이력으로 update
            if (rcDto.getRecvCondition().equals("TOTA")) {
                // new_recv_condition = "TORE";
                // recv_con_dtl = "network_ok";

                log.info(">>>>> {} 관측소 이력 [네트워크 복구]: TORE, network_ok", siteStr);
                // 이력 update
                queryService.updateReceiveData("TORE", "network_ok", site_cd, dataKindStr, "NQC", "RECV", currentTime);
            }

            // 최종 상태가 WARN 에서
            if (rcDto.getRecvCondition().equals("WARN")) {
                // 이력 update
                // 자료가 수신되었을 때
                if (rcDto.getCodedtl().equals("file_no")) {
                    // new_recv_condition = "RETR";
                    // recv_con_dtl = "file_ok";
                    log.info(">>>>> {} 관측소 이력 [파일 정상 수신]: RETR, file_ok", siteStr);
                    queryService.updateReceiveData("RETR", "file_ok", site_cd, dataKindStr, "NQC", "RECV", currentTime);
                }

                // 파일크기가 정상 수신되었을때
                if (rcDto.getCodedtl().equals("filesize_no")) {
                    // new_recv_condition = "RETR";
                    // recv_con_dtl = "filesize_ok";
                    log.info(">>>>> {} 관측소 이력 [파일 크기 정상 수신]: RETR, filesize_ok", siteStr);
                    queryService.updateReceiveData("RETR", "filesize_ok", site_cd, dataKindStr,"NQC", "RECV", currentTime);
                }

            }
            // DB 임계 조건 횟수 대조 루프 스캔 (상태 전이 모델)
            for (ReceiveConditionCriteriaDto rcc : rccDtoList) {

                // log.debug(">>>>>> rcc.getCode(): {}", rcc.getCode());
                // log.debug(">>>>>> rcDto.getRecvCondition(): {}", rcDto.getRecvCondition());
                // log.debug(">>>>>> site_cd: {}, dataKindStr: {}, rcc.getCriterion(): {}", site_cd, dataKindStr, rcc.getCriterion());

                // 복구상태에서 > 정상
                if (rcc.getCode().equals("TORE") &&
                        (rcDto.getRecvCondition().equals("RETR") || rcDto.getRecvCondition().equals("TORE"))) {
                    // 정상(ok)
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr, rcc.getCriterion());
                    log.info(">> {} 관측소 [복구 > 정상] 경고 기준 : {}, 이력 data: {}", siteStr, rcc.getCriterion(), rdDto.size());
                    for (ReceiveDataDto rd : rdDto) {
                        if (rd.getRecv_condition().equals("RECV") && rd.getCodedtl().equals("ok")) {
                            new_recv_condition = "ORDI";
                            recv_con_dtl = "ok";
                        }
                    }
                }

                // 복구(file_ok)
                if (rcc.getCodedtl().equals("file_ok")) {
                    // 최신 이력조회
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr, rcc.getCriterion());
                    int cnt = 0;
                    for (ReceiveDataDto rd : rdDto) {
                        if (rd.getCodedtl().equals(rcc.getCodedtl())) {
                            cnt++;
                        }
                    }

                    log.info(">> {} 관측소 [복구 file_ok] 경고 기준 : {}/cnt : {}/이력data: {}", siteStr, rcc.getCriterion(), cnt, rdDto.size());

                    // 이력 비교
                    if (rcc.getCriterion() == cnt) {
                        new_recv_condition = "RETR";
                        recv_con_dtl = "file_ok";
                        // recv_con_dtl = rcc.getCodedtl();
                    }
                }
                // 복구(filesize_ok)
                if (rcc.getCodedtl().equals("filesize_ok")) {
                    // 최신 이력조회
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr, rcc.getCriterion());
                    int cnt = 0;
                    for (ReceiveDataDto rd : rdDto) {
                        if (rd.getCodedtl().equals(rcc.getCodedtl())) {
                            cnt++;
                        }
                    }

                    log.info(">> {} 관측소 [복구 filesize_ok] 경고 기준 : {}/cnt : {}/이력data: {}", siteStr, rcc.getCriterion(), cnt, rdDto.size());

                    // 이력 비교
                    if (rcc.getCriterion() == cnt) {
                        new_recv_condition = "RETR";
                        recv_con_dtl = "filesize_ok";
                        // recv_con_dtl = rcc.getCodedtl();
                    }
                }

                if ("TORE".equals(rcc.getCode())) {
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr, rcc.getCriterion());
                    int cnt = 0; int misscnt = 0;
                    for (ReceiveDataDto rd : rdDto) {
                        if (rd.getRecv_condition().equals(rcc.getCode())) cnt++;
                        if ("MISS".equals(rd.getRecv_condition())) misscnt++;
                    }
                    if (rcc.getCriterion() == cnt) {
                        new_recv_condition = "TORE"; recv_con_dtl = "network_ok";
                    }
                    if ("TOTA".equals(rcDto.getRecvCondition()) && rcc.getCriterion() == misscnt) {
                        new_recv_condition = "WARN"; recv_con_dtl = "file_no";
                    }

                    log.info(">> {} 관측소 [전체복구] 경고 기준 : {}/cnt : {}/이력data: {}", siteStr, rcc.getCriterion(), cnt, rdDto.size());
                }

                if ("TOTA".equals(rcc.getCode())) {
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr, rcc.getCriterion());
                    int cnt = 0;
                    for (ReceiveDataDto rd : rdDto) {
                        if (rd.getRecv_condition().equals(rcc.getCode())) cnt++;
                    }
                    if (rcc.getCriterion() == cnt) {
                        new_recv_condition = "TOTA"; recv_con_dtl = "network_no";
                    }

                    log.info(">> {} 관측소 [전체장애] 경고 기준 : {}/cnt : {}/이력data: {}", siteStr, rcc.getCriterion(), cnt, rdDto.size());
                }

                // 장애
                if (rcc.getCode().equals("WARN")) {
                    // 이력조회
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr, rcc.getCriterion());

                    // 장애(file_no)
                    if (rcc.getCodedtl().equals("file_no")) {
                        int cnt = 0;
                        for (ReceiveDataDto rd : rdDto) {
                            if (rd.getCodedtl().equals(rcc.getCodedtl())) {
                                cnt++;
                            }
                        }
                        log.info(">> {} 관측소 [장애 file_no] 경고 기준 : {}/cnt : {}/이력data: {}", siteStr, rcc.getCriterion(), cnt, rdDto.size());

                        // 이력 비교
                        if (rcc.getCriterion() == cnt) {
                            new_recv_condition = "WARN";
                            recv_con_dtl = "file_no";
                        }
                    }
                    // 장애(filesize_no)
                    if (rcc.getCodedtl().equals("filesize_no")) {
                        int cnt = 0;
                        for (ReceiveDataDto rd : rdDto) {
                            if (rd.getCodedtl().equals(rcc.getCodedtl())) {
                                cnt++;
                            }
                        }

                        log.info(">> {} 관측소 [장애 filesize_no] 경고 기준 : {}/cnt : {}/이력data: {}", siteStr, rcc.getCriterion(), cnt, rdDto.size());

                        // 이력 비교
                        if (rcc.getCriterion() == cnt) {
                            new_recv_condition = "WARN";
                            recv_con_dtl = "filesize_no";
                        }
                    }
                }
            }
            log.info("[실시간 런타임 결과] : " + new_recv_condition + " | 상세사유: " + recv_con_dtl);

            if (!new_recv_condition.equals("")) {
                int smsSend = rcDto.getSmsSend();
                if (rcDto.getRecvCondition().equals(new_recv_condition)) {
                    apply_time = rcDto.getApplyTime();
                } else {
                    smsSend = 0; apply_time = currentTime + ":00";
                }
                queryService.updateReceiveCondition(apply_time, new_recv_condition, recv_con_dtl, smsSend, old_recv_condition, site_cd, dataKindStr, "NQC");
            } else {
                queryService.updateReceiveCondition(rcDto.getApplyTime(), rcDto.getRecvCondition(), rcDto.getCodedtl(), rcDto.getSmsSend(), old_recv_condition, site_cd, dataKindStr, "NQC");
            }
        }

        // =========================================================================
        // 💡 3단계 문자발송 분기 처리 레이어 연동
        // =========================================================================
        SmsSendOnOffDto onoffDto = queryService.getSmsSendOnOffData();
        if (onoffDto.getValue() == 1) {
            String currentFullTime = TimeUtil.getCurrentKstString();
            smsService.sendSmsNotifications(gubun, dataKindStr, srCnt, srDto, gubunStr, cycleId, currentFullTime, agencyCd, daemonType);
        } else {
            log.info("[장애시 문자 발송 기능 off]");
        }
    }
}
