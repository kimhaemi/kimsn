package kr.or.kimsn.radardemo.process;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import kr.or.kimsn.radardemo.common.DataCommon;
import kr.or.kimsn.radardemo.common.FormatDateUtil;
import kr.or.kimsn.radardemo.dto.ReceiveConditionCriteriaDto;
import kr.or.kimsn.radardemo.dto.ReceiveConditionDto;
import kr.or.kimsn.radardemo.dto.ReceiveDataDto;
import kr.or.kimsn.radardemo.dto.SmsSendMemberDto;
import kr.or.kimsn.radardemo.dto.SmsSendOnOffDto;
import kr.or.kimsn.radardemo.dto.SmsSendPatternDto;
import kr.or.kimsn.radardemo.dto.StationDto;
import kr.or.kimsn.radardemo.dto.StationStatusDto;
import kr.or.kimsn.radardemo.service.QueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StepThreeProcess {

    private final QueryService queryService;

    private List<StationDto> srDto;

    @Transactional
    public void stepThree() {
        // 문자 메시지 발송 상태가 on 일때만
        SmsSendOnOffDto onoffDto = queryService.getSmsSendOnOffData();
        // System.out.println("onoffDto ::: " + onoffDto);
        if (onoffDto.getValue() == 1) {
            // String gubunStr = "";
            String dataKindStr = "";
            int srCnt = 0;

            int gubun = Integer.parseInt(DataCommon.getInfoConf("siteInfo", "gubun"));
            // System.out.println("[데몬 구분 int] : " + gubun);

            // if (gubun == 1)
            // gubunStr = "대형";
            // if (gubun == 2)
            // gubunStr = "소형";
            // if (gubun == 3)
            // gubunStr = "공항";
            // System.out.println("[데몬 구분] : " + gubunStr);

            if (gubun == 1)
                dataKindStr = "RDR";
            if (gubun == 2)
                dataKindStr = "SDR";
            if (gubun == 3)
                dataKindStr = "TDWR";
            // System.out.println("[데이터 종류] : " + dataKindStr);

            String mode = DataCommon.getInfoConf("siteInfo", "mode");
            // System.out.println("[mode] : " + mode);

            if (!mode.equals("test")) {
                srDto = queryService.getStation(gubun);
                srCnt = srDto.size();
            }

            // System.out.println("[레이더 갯수] : " + srCnt);

            for (int a = 0; a < srCnt; a++) {
                String site_cd = srDto.get(a).getSiteCd();
                String siteStr = srDto.get(a).getName_kr();

                System.out.println("[============ " + siteStr + " 정보 ==============]");

                // 지점별 운영상태가 정상일 때만
                StationStatusDto siteStatusDto = queryService.getStationStatus(site_cd);
                // System.out.println("siteStatusDto ::::::: " + siteStatusDto);

                // if (siteStatusDto.getSite_status().equals("RUN")) {
                // 최종상태조회
                ReceiveConditionDto rcDto = queryService.getReceiveCondition(dataKindStr, "NQC", site_cd);
                // System.out.println("[최종상태 조회] : " + rcDto);

                if (rcDto.getRecv_condition().equals("ORDI")) {
                    System.out.println("[최종 상태 정상]");
                    // 최종상태의 SMS 발송 기능 ON/OFF
                } else if (rcDto.getSms_send_activation() == 1 && !rcDto.getRecv_condition().equals("ORDI")) {
                    String code = rcDto.getRecv_condition();
                    String codedtl = rcDto.getCodedtl();
                    String smsCodedtl = rcDto.getCodedtl();
                    int sms_send = rcDto.getSms_send();

                    // System.out.println("[gubun] : " + gubun);
                    System.out.println("[code] : " + code);
                    System.out.println("[codedtl] : " + codedtl);

                    // 경고 기준 (횟수 - criterion)
                    ReceiveConditionCriteriaDto rccDto = queryService.getReceiveConditionCriteria(gubun, code,
                            codedtl);
                    // System.out.println("[경고 기준] : " + rccDto);
                    int criterion = rccDto.getCriterion();
                    System.out.println("[" + rccDto.getName() + " '" + criterion + "'회 체크]");

                    // 문자메시지 패턴
                    String smsPettern = "";
                    if (!code.equals("ORDI")) {
                        if (codedtl.equals("siteconnect_no"))
                            smsCodedtl = "file_no";
                        // SmsSendPatternDto smsSendPatternDto = queryService.getSmsSendPattern(1, 1,
                        // "RUN", code, smsCodedtl);
                        List<SmsSendPatternDto> smsSendPatternDto = queryService.getSmsSendPattern(1, 1, code,
                                smsCodedtl);

                        String dateTime = FormatDateUtil.formatDate("yyyy-MM-dd HH:mm:ss", new Date());

                        // System.out.println("[date Time] : " + dateTime);

                        for (SmsSendPatternDto dto : smsSendPatternDto) {
                            if (siteStatusDto.getSite_status().equals(dto.getMode())) {
                                smsPettern = dto.getPattern();
                            }
                        }

                        smsPettern = smsPettern.replace("%SITE%", siteStr).replace("%TIME%", dateTime);

                    }
                    System.out.println("[문자메시지 패턴] : " + smsPettern);

                    // 이력 조회
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr, criterion);
                    // List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd,
                    // dataKindStr, criterion);
                    int cnt = 0; // 최종이력상태와 결과 값 같은것
                    String recv_con = "";
                    for (ReceiveDataDto rd : rdDto) {
                        // System.out.println("[이력 조회] : " + rd);
                        // ORDI 정상 0 정상 1 ok
                        // RETR 복구 3 자료 크기가 연속으로 N회 이상 정상일때 1 filesize_ok
                        // RETR 복구 3 비 정상에서 복구되어 정상의 범위에 들 경우 1 file_ok
                        // TORE 네트워크 복구 2 네트워크 장애 상태에서 복구되어 정상의 범위에 들때 1 network_ok
                        // TOTA 네트워크 장애 3 대형 레이더 전 사이트 자료 미수신 일 때 1 network_no
                        // WARN 경고 7 자료 크기가 기준파일크기보다 연속으로 N회 이상 작을때 1 filesize_no
                        // WARN 경고 6 자료가 연속으로 N회 이상 자료 미수신일 때 1 file_no
                        if (rd.getRecv_condition().equals("RECV"))
                            recv_con = "ORDI"; // 정상
                        if (rd.getRecv_condition().equals("MISS"))
                            recv_con = "WARN"; // 경고
                        if (rd.getRecv_condition().equals("RETR"))
                            recv_con = "RETR"; // 복구 - 자료 미수신, 사이즈 복구
                        if (rd.getRecv_condition().equals("TORE"))
                            recv_con = "TORE"; // 복구 - 네트워크 복구
                        if (rd.getRecv_condition().equals("TOTA"))
                            recv_con = "TOTA"; // 네트워크장애

                        if (recv_con.equals(code) && criterion == rdDto.size()
                                && rccDto.getCodedtl().equals(rd.getCodedtl())) {
                            cnt++;
                        }
                    }
                    System.out.println("[cnt] : " + cnt);

                    if (rccDto.getCriterion() == cnt) {
                        // 최종 결과의 문자 전송 상태가 0일때만 문자 전송
                        if (sms_send == 0) {
                            // app sequence
                            Long appSeq = queryService.getAppContentNextval();
                            System.out.println("[문자 전송 app sequence] :" + appSeq);
                            // 문자 전송(app_send_contents) insert
                            queryService.intGaonAppSendContentsSave(appSeq, smsPettern);

                            // site 수신그룹 담당자에게 문자 전송
                            List<SmsSendMemberDto> smsDto = queryService.getSmsSendMemberList(dataKindStr, site_cd);
                            // System.out.println("[담당자] : " + smsDto);

                            for (SmsSendMemberDto dto : smsDto) {
                                // private int warn; //경고 -- MISS
                                // private int retr; //복구 -- 장애 복구, 네트워크 복구
                                // private int sms; //문자 발송 여부
                                // private int tota; //네트워크 오류
                                if (dto.getWarn() == 1) { // 문자발송 여부
                                    if ((code.equals("WARN") && dto.getWarn() == 1) // 장애
                                            || (code.equals("RETR") && dto.getWarn() == 1) // 복구
                                            || (code.equals("TOTA") && dto.getWarn() == 1) // 네트워크 오류
                                    ) {
                                        String call_to = dto.getPhone_num().replaceAll("-", "");
                                        String call_from = DataCommon.getInfoConf("siteInfo", "call_from");

                                        System.out.println("[수신자명] : " + dto.getName() + " : " + call_to);
                                        // System.out.println("[수신번호] : " + call_to);
                                        // System.out.println("[발신번호] : " + call_from);

                                        // 문자 전송(app_send_data) insert
                                        if (!call_to.equals("") && call_to.matches("[0-9]+")) { // 전화번호가 아니면 안되있는건
                                                                                                // 보낼 필요가 없지..
                                            queryService.insGaonAppSendDataSave(appSeq, call_to, call_from); // 템플릿
                                                                                                             // 코드
                                                                                                             // 넣어야함.
                                        } else {
                                            System.out.println("[수신번호 확인] : " + call_to);
                                        }
                                    }
                                }
                            }

                            System.out.println("[문자 전송 여부 update]");
                            sms_send = 1;
                            int second = 60 * 4 + 30;
                            String previousTime = queryService.getPreviousTime(second);
                            System.out.println("감시 해야할 시간 이전: " + previousTime);

                            if (Integer.parseInt(
                                    previousTime.substring(previousTime.length() - 1, previousTime.length())) <= 5)
                                previousTime = previousTime.substring(0, previousTime.length() - 1) + "0";
                            if (Integer.parseInt(
                                    previousTime.substring(previousTime.length() - 1, previousTime.length())) > 5)
                                previousTime = previousTime.substring(0, previousTime.length() - 1) + "5";
                            System.out.println("감시 해야할 시간 이후: " + previousTime);

                            previousTime = FormatDateUtil.formatDate("yyyy-MM-dd HH:mm:ss",
                                    FormatDateUtil.stringToDate(previousTime));
                            System.out.println("previousTime :::::: " + previousTime);

                            queryService.updateReceiveCondition(previousTime, code, codedtl, sms_send, code,
                                    site_cd, dataKindStr, "NQC");
                            // queryService.updateReceiveData(code, 0, site_cd, dataKindStr, "NQC", code);
                        } else {
                            System.out.println("[이미 문자 전송했으므로 안해도 됨]");
                        }
                    } else {
                        System.out.println("[문자 전송 체크 - 기준자료] : " + rccDto.getCriterion());
                        System.out.println("[문자 전송 체크 - data cnt] : " + cnt);
                        System.out.println("문자 전송 안해도 됨");
                    }
                } else {
                    System.out.println("[" + siteStr + " 최종상태 문자 발송 기능 off]");
                }
                // } else {
                // System.out.println("[" + siteStr + "는 유지보수 상태여서 문자 전송 안됨]");
                // }
            }
        } else {
            System.out.println("[장애시 문자 발송 기능 off]");
        }
    }
}
