package kr.or.kimsn.radar.daemon.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kimsn.radar.daemon.enums.*;
import kr.or.kimsn.radar.daemon.util.TimeUtil;
import kr.or.kimsn.radar.data.dto.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsService {

    private final QueryService queryService;
    private final ConfigurableEnvironment environment;

    @Transactional(rollbackFor = Exception.class)
    public void sendSmsNotifications(int gubun, String dataKindStr, int srCnt, List<StationDto> srDto, String gubunStr, String cycleId, String currentTime, String agencyCd, String daemonType) {
        log.info("======================= 문자 전송 =================================");

        String globalPrefix = "radar.config.global.";
        String callFrom = environment.getProperty(globalPrefix + "call_from", "027337365");
        String templateCode = environment.getProperty(globalPrefix + "template_code", "radar_0001");
        AppTemplateCodeDto templateCodeDto = queryService.getTemplateCode(templateCode);
        String smsTitle = templateCodeDto.getHead();
        // log.info("템플릿: {}", templateCodeDto);

        //기후부 소형은 대형 로직임.
        int new_gubun = (gubun == 1 || (gubun == 2 && agencyCd.equals("MCEE"))) ? 1 : (gubun == 2 && agencyCd.equals("KMA")) ? 2 : 3;

        // 전 사이트 장애/복구 메시지는 1번씩만
        List<ReceiveConditionDto> rcDtoAll = queryService.getReceiveConditionList(dataKindStr, "NQC");
        // 지점별 운영상태가 정상일 때만
        List<StationStatusDto> siteStatusDtos = queryService.getStationStatusGubun(new_gubun);

        String dateTime = TimeUtil.getCurrentKstString();

        int recvTota = 0; // 장애
        int recvTore = 0; // 복구
        String recvCode = "";
        String recvCodeDtl = "";
        List<ReceiveConditionDto> rcActiveList = new ArrayList<>();
        for (ReceiveConditionDto rc : rcDtoAll) {
            // 전 지점 문자 발송 설정 on
            if (rc.getSmsSendActivation() > 0) {
                rcActiveList.add(rc);
            }

            // 전 사이트 장애
            if (ReceiveConditionEnum.TOTA.getCode().equals(rc.getRecvCondition())) {
                recvCode = ReceiveConditionEnum.TOTA.getCode();
                recvCodeDtl = ReceiveCodeDtlEnum.NETWORK_NO.getCode();
                recvTota++;
            }
            // 한 사이트 이상 복구
            if (ReceiveConditionEnum.TORE.getCode().equals(rc.getRecvCondition())) {
                recvCode = ReceiveConditionEnum.TORE.getCode();
                recvCodeDtl = ReceiveCodeDtlEnum.NETWORK_OK.getCode();
                recvTore++;
            }
        }
        if(recvCodeDtl != ""){
            log.info("[Code]: {}", recvCode);
            log.info("[CodeDtl]: {}", recvCodeDtl);
            log.info("[전체 장애]: {}", recvTota);
            log.info("[일부 복구]: {}", recvTore);
        }

        if (srDto.size() == recvTota || recvTore > 0) {
            List<SmsSendPatternDto> smsPatternDto = queryService.getSmsSendPattern(1, 1, recvCode, recvCodeDtl);
            String smsPetterns = "";

            log.info("[문자메시지 패턴]", smsPatternDto);

            for (StationStatusDto status : siteStatusDtos) {
                if (status.getStatus() > 0) {
                    for (SmsSendPatternDto spDto : smsPatternDto) {
                        log.info("패턴: {}", spDto);
                        if (status.getSiteStatus().equals(spDto.getMode())) {

                            for (ReceiveConditionDto rcList : rcActiveList) {
                                log.info("최종결과site: {}, 관측소상태siteCd: {}, 최종결과smsSend: {}", rcList.getSite(), status.getSiteCd(), rcList.getSmsSend());
                                if (rcList.getSite().equals(status.getSiteCd()) && rcList.getSmsSend() == 0) {
                                    // 네트워크 장애, 복구 일때 대형,소형,공항 구분
                                    String sitePrefix = (recvCode.equals("TOTA") || recvCode.equals("TORE")) ? (gubunStr + " ") : "";
                                    
                                    smsPetterns = sitePrefix + spDto.getPattern()
                                            .replace("%SITE%", status.getSiteName())
                                            .replace("%TIME%", dateTime);
                                } else {
                                    log.info("[문자 전송 여부]: {}", rcList.getSmsSend() == 1 ? "전송됨" : "전송 안됨");
                                }
                            }
                        }
                    }
                }
            }
            log.info("[전체장애 또는 일부 복구 문자 패턴] :: " + smsPetterns);

            
            // 문자메시지 패턴 정보가 없으면 못보냄.
            if (!smsPetterns.equals("")) {
                String titleAndText = smsTitle.replaceAll("\n", "") + "\n\n"+ smsPetterns;
                String resDate = dateTime.replaceAll("[^0-9]", ""); 
                // if (rccDto.getCriterion() == rdCnt) {
                // 최종 결과의 문자 전송 상태가 0일때만 문자 전송
                // if (rcList.getSms_send() == 0) {

                // site 수신그룹 담당자에게 문자 전송
                List<SmsSendMemberDto> smsMembersDto = queryService.getSmsSendMemberList(dataKindStr, null);
                // log.info("[담당자] : " + smsDto);

                for (SmsSendMemberDto dto : smsMembersDto) {
                    // private int warn; //경고 -- MISS
                    // private int tore; //복구 -- 네트워크 복구
                    // private int sms; //문자 발송 여부
                    // private int tota; //네트워크 오류
                    if (dto.getSms() == 1) { // 문자발송 여부
                        if ((recvCode.equals("TOTA") && dto.getWarn() == 1) // 네트워크오류
                                || (recvCode.equals("TORE") && dto.getRetr() == 1) // 네트워크복구
                                || (recvCode.equals("RETR") && dto.getRetr() == 1)) {
                            String callTo = dto.getPhone_num().replaceAll("-", "");

                            log.info("[🚀 파트A 문자 전송] 수신자: {}, 번호: {}, 내용: {}", dto.getName(), callTo, titleAndText);

                            // 문자 전송 insert
                            // 전화번호가 아니면 안되있는건 보낼 필요가 없지..
                            if (!callTo.equals("") && callTo.matches("[0-9]+")) {
                                queryService.intNuri2Save(resDate, callTo, callFrom, templateCode, smsTitle, titleAndText);
                            } else {
                                log.info("[수신번호 확인] : " + callTo);
                            }
                        }
                    }
                }

                log.info("[문자 전송 여부 update]");
                int second = 60 * 4 + 30;
                String previousTime = queryService.getPreviousTime(second);
                log.info("감시 해야할 시간 이전: " + previousTime);

                if (Integer.parseInt(previousTime.substring(previousTime.length() - 1, previousTime.length())) <= 5)
                    previousTime = previousTime.substring(0, previousTime.length() - 1) + "0";
                if (Integer.parseInt(previousTime.substring(previousTime.length() - 1, previousTime.length())) > 5)
                    previousTime = previousTime.substring(0, previousTime.length() - 1) + "5";

                log.info("감시 해야할 시간 이후: " + previousTime);

                // update
                queryService.updateReceiveConditionSms(1, null, null, dataKindStr, "NQC");
            } else {
                log.error("설정값에 따른 문자메시지 패턴 다시 확인 on/off");
            }
        }
        
        //지점 체크
        if (recvCode.isEmpty()) {
            for (int a = 0; a < srCnt; a++) {
                String site_cd = srDto.get(a).getSiteCd();
                String siteStr = srDto.get(a).getNameKr();

                log.info("[============ {} 정보 ==============]", siteStr);

                // 지점별 운영상태
                StationStatusDto siteStatusDto = queryService.getStationStatus(site_cd);
                // 최종상태조회
                ReceiveConditionDto rcDto = queryService.getReceiveCondition(dataKindStr, "NQC", site_cd);
                // log.info("[최종상태 조회] : " + rcDto);

                if (rcDto.getRecvCondition().equals(ReceiveConditionEnum.ORDI.getCode())) {
                    log.info("[최종 상태 정상]");
                    // 최종상태의 SMS 발송 기능 ON/OFF
                } else if (rcDto.getSmsSendActivation() == 1 && !rcDto.getRecvCondition().equals("ORDI")
                        && !rcDto.getRecvCondition().equals("TOTA") && !rcDto.getRecvCondition().equals("TORE")) {
                    String code = rcDto.getRecvCondition();
                    String codedtl = rcDto.getCodedtl();
                    int smsSend = rcDto.getSmsSend();

                    log.info("[code] : {} | [code-dtl] : {}", code, codedtl);

                    // 경고 기준 (횟수 - criterion)
                    ReceiveConditionCriteriaDto rccDto = queryService.getReceiveConditionCriteria(new_gubun, code, codedtl);
                    // log.info("[경고 기준] : " + rccDto);
                    int criterion = rccDto.getCriterion();
                    log.info("[{} '{}'회 체크]", rccDto.getName(), criterion);

                    // 문자메시지 패턴
                    String smsPettern = "";
                    if (!code.equals("ORDI") && !code.equals("TOTA") && !code.equals("TORE")) {
                        // SmsSendPatternDto smsSendPatternDto = queryService.getSmsSendPattern(1, 1,
                        // "RUN", code, smsCodedtl);
                        List<SmsSendPatternDto> smsSendPatternDto = queryService.getSmsSendPattern(1, 1, code, codedtl);

                        // log.info("[date Time] : " + dateTime);

                        for (SmsSendPatternDto dto : smsSendPatternDto) {
                            if (siteStatusDto.getSiteStatus().equals(dto.getMode())) {
                                smsPettern = dto.getPattern();
                            }
                        }

                        smsPettern = smsPettern.replace("%SITE%", siteStr).replace("%TIME%", dateTime);

                    }
                    log.info("[문자메시지 패턴] : " + smsPettern);

                    // 이력 조회
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr, criterion);
                    // List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr, criterion);
                    int cnt = 0; // 최종이력상태와 결과 값 같은것
                    String recvCon = "";
                    for (ReceiveDataDto rd : rdDto) {
                        // log.info("[이력 조회] : " + rd);
                        // ORDI 정상 0 정상 1 ok
                        // RETR 복구 3 자료 크기가 연속으로 N회 이상 정상일때 1 filesize_ok
                        // RETR 복구 3 비 정상에서 복구되어 정상의 범위에 들 경우 1 file_ok
                        // TORE 네트워크 복구 2 네트워크 장애 상태에서 복구되어 정상의 범위에 들때 1 network_ok
                        // TOTA 네트워크 장애 3 대형 레이더 전 사이트 자료 미수신 일 때 1 network_no
                        // WARN 경고 7 자료 크기가 기준파일크기보다 연속으로 N회 이상 작을때 1 filesize_no
                        // WARN 경고 6 자료가 연속으로 N회 이상 자료 미수신일 때 1 file_no
                        if (rd.getRecv_condition().equals("RECV"))
                            recvCon = "ORDI"; // 정상
                        if (rd.getRecv_condition().equals("MISS"))
                            recvCon = "WARN"; // 경고
                        if (rd.getRecv_condition().equals("RETR"))
                            recvCon = "RETR"; // 복구 - 자료 미수신, 사이즈 복구
                        // if (rd.getRecv_condition().equals("TORE"))
                        // recv_con = "TORE"; // 복구 - 네트워크 복구
                        // if (rd.getRecv_condition().equals("TOTA"))
                        // recv_con = "TOTA"; // 네트워크장애

                        if (recvCon.equals(code) && criterion == rdDto.size()
                                && rccDto.getCodedtl().equals(rd.getCodedtl())) {
                            cnt++;
                        }
                    }
                    log.info("최종 산출 판정 건수 [cnt] : {} / 기준치 [criterion] : {}", cnt, criterion);

                    String titleAndText = smsTitle.replace("\n", "") + "\n\n"+ smsPettern;
                    String resDate = dateTime.replaceAll("[^0-9]", "");

                    // 문자메시지 패턴 정보가 없으면 못보냄.
                    if (!smsPettern.equals("")) {
                        if (rccDto.getCriterion() == cnt) {
                            // 최종 결과의 문자 전송 상태가 0일때만 문자 전송
                            if (smsSend == 0) {
                                // site 수신그룹 담당자에게 문자 전송
                                List<SmsSendMemberDto> smsDto = queryService.getSmsSendMemberList(dataKindStr, site_cd);
                                // log.info("[담당자] : " + smsDto);

                                for (SmsSendMemberDto dto : smsDto) {
                                    // private int warn; //경고 -- MISS
                                    // private int retr; //복구 -- 장애 복구, 네트워크 복구
                                    // private int sms; //문자 발송 여부
                                    // private int tota; //네트워크 오류
                                    if (dto.getWarn() == 1) { // 문자발송 여부
                                        if (code.equals("WARN") // 장애
                                                || code.equals("RETR") // 복구
                                                || code.equals("TOTA") // 네트워크 오류
                                        ) {
                                            String callTo = dto.getPhone_num().replace("-", "");

                                            log.info("[🚀 파트B 문자 전송] 수신자: {}, 번호: {}, 내용: {}", dto.getName(), callTo, titleAndText);

                                            // 문자 전송 insert
                                            // 전화번호가 아니면 안되있는건 보낼 필요가 없지..
                                            if (!callTo.equals("") && callTo.matches("[0-9]+")) {
                                                queryService.intNuri2Save(resDate, callTo, callFrom, templateCode, smsTitle, titleAndText);
                                            } else {
                                                log.info("[수신번호 확인] : " + callTo);
                                            }
                                        }
                                    }
                                }

                                log.info("[문자 전송 여부 update]");
                                smsSend = 1;
                                int second = 60 * 4 + 30;
                                String previousTime = queryService.getPreviousTime(second);
                                log.info("감시 해야할 시간 이전: " + previousTime);

                                if (Integer.parseInt(previousTime.substring(previousTime.length() - 1, previousTime.length())) <= 5)
                                    previousTime = previousTime.substring(0, previousTime.length() - 1) + "0";
                                if (Integer.parseInt(previousTime.substring(previousTime.length() - 1, previousTime.length())) > 5)
                                    previousTime = previousTime.substring(0, previousTime.length() - 1) + "5";
                                log.info("감시 해야할 시간 이후: " + previousTime);

                                queryService.updateReceiveCondition(previousTime + "00", code, codedtl, smsSend, code, site_cd, dataKindStr, "NQC");
                            } else {
                                log.info("[이미 문자 전송했음]");
                            }
                        } else {
                            log.info("[문자 전송 체크 - 기준자료] : " + rccDto.getCriterion());
                            log.info("[문자 전송 체크 - data cnt] : " + cnt);
                            log.info("문자 전송 안해도 됨");
                        }
                    } else {
                        log.info("설정값에 따른 문자메시지 패턴 다시 확인 on/off");
                    }
                } else {
                    log.info("[" + siteStr + " 최종상태 문자 발송 기능 off]");
                }
            }
        }
    }
}
