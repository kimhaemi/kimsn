package kr.or.kimsn.radardemo.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.json.simple.JSONObject;
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
public class StepTwoProcess {
    private final QueryService queryService;

    private List<StationDto> srDto;

    @Transactional
    public void stepTwo(String gubunStr) {
        String mode = DataCommon.getInfoConf("siteInfo", "mode");
        String currentTime = FormatDateUtil.formatDate("yyyy-MM-dd HH:mm", new Date());
        int gubun = Integer.parseInt(DataCommon.getInfoConf("siteInfo", "gubun"));
        String dataKindStr = "";
        int srCnt = 0; // site

        // log.info("[mode] : " + mode);
        // log.info("[데몬 구분 int] : " + gubun);

        if (gubun == 1)
            dataKindStr = "RDR";
        if (gubun == 2)
            dataKindStr = "SDR";
        if (gubun == 3)
            dataKindStr = "TDWR";
        // log.info("[데이터 종류] : " + dataKindStr);

        if (!mode.equals("test")) {
            srDto = queryService.getStation(gubun);
            srCnt = srDto.size();
        }

        if (gubun == 1 || gubun == 3) { // 대형, 공항 4분 30초 전
            if (Integer.parseInt(currentTime.substring(currentTime.length() - 1, currentTime.length())) <= 5)
                currentTime = currentTime.substring(0, currentTime.length() - 1) + "0";
            if (Integer.parseInt(currentTime.substring(currentTime.length() - 1, currentTime.length())) > 5)
                currentTime = currentTime.substring(0, currentTime.length() - 1) + "5";
        }

        int all_site_network_no = 0; // 전 사이트 네트워크 장애 여부

        // 경고 기준 List (횟수 - criterion)
        List<ReceiveConditionCriteriaDto> rccDtoList = queryService.getReceiveConditionCriteriaList(gubun);

        // 전 사이트가 장애인지 여부 cnt
        if (gubun != 3) {
            for (int a = 0; a < srCnt; a++) {
                String site_cd = srDto.get(a).getSiteCd();
                // 전 site 이력 조회
                List<ReceiveDataDto> rdDtoSingle = queryService.getReceiveDataList(site_cd, dataKindStr, 1);
                // System.out.println("rdDtoSingle ::::" + rdDtoSingle);
                for (ReceiveDataDto rd : rdDtoSingle) {
                    if (rd.getRecv_condition().equals("MISS") && rd.getCodedtl().equals("file_no")) {
                        all_site_network_no++;
                    }
                }
            }
        }

        for (int a = 0; a < srCnt; a++) {

            String new_recv_condition = ""; // 정상(ok) / 장애(file_no, filesize_no) / 네트워크 장애(network_no)
            String recv_con_dtl = ""; // 정상(ok) / 장애(file_no, filesize_no) / 네트워크 장애(network_no)

            String old_recv_condition = "";
            String apply_time = "";

            String site_cd = srDto.get(a).getSiteCd();
            String siteStr = srDto.get(a).getName_kr();

            log.info("[============ " + siteStr + " 정보 ==============]");

            if (all_site_network_no == srCnt) {
                log.info("[####### 전 사이트 네트워크 장애 이력 update ########]");
                // new_recv_condition = "TOTA";
                // recv_con_dtl = "network_no";
                // 이력 update
                queryService.updateReceiveData("TOTA", "network_no", site_cd, dataKindStr,
                        "NQC", "MISS", currentTime);
            }

            log.info("dataKindStr : " + dataKindStr);
            log.info("site_cd : " + site_cd);
            log.info("data_type : " + "NQC");
            log.info("currentTime : " + currentTime);

            // 최종상태조회
            ReceiveConditionDto rcDto = queryService.getReceiveCondition(dataKindStr, "NQC", site_cd);
            // log.info("rcDto ::::::::: " + rcDto);
            log.info("최종 상태 : " + rcDto.getRecv_condition());

            old_recv_condition = rcDto.getRecv_condition();

            // 최종 상태가 TOTA 이면 네트워크 복구 이력으로 update
            if (rcDto.getRecv_condition().equals("TOTA")) {
                // new_recv_condition = "TORE";
                // recv_con_dtl = "network_ok";
                // 이력 update
                queryService.updateReceiveData("TORE", "network_ok", site_cd, dataKindStr,
                        "NQC", "RECV", currentTime);
            }

            // 최종 상태가 WARN 에서
            if (rcDto.getRecv_condition().equals("WARN")) {
                // 이력 update
                // 자료가 수신되었을 때
                if (rcDto.getCodedtl().equals("file_no")) {
                    // new_recv_condition = "RETR";
                    // recv_con_dtl = "file_ok";
                    queryService.updateReceiveData("RETR", "file_ok", site_cd, dataKindStr,
                            "NQC", "RECV", currentTime);
                }

                // 파일크기가 정상 수신되었을때
                if (rcDto.getCodedtl().equals("filesize_no")) {
                    // new_recv_condition = "RETR";
                    // recv_con_dtl = "filesize_ok";
                    queryService.updateReceiveData("RETR", "filesize_ok", site_cd, dataKindStr,
                            "NQC", "RECV", currentTime);
                }

            }

            for (ReceiveConditionCriteriaDto rcc : rccDtoList) {

                // 복구상태에서 > 정상
                if (rcc.getCode().equals("TORE") &&
                        (rcDto.getRecv_condition().equals("RETR") || rcDto.getRecv_condition().equals("TORE"))) {
                    // 정상(ok)
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr,
                            rcc.getCriterion());
                    for (ReceiveDataDto rd : rdDto) {
                        if (rd.getRecv_condition().equals("RECV") && rd.getCodedtl().equals("ok")) {
                            new_recv_condition = "ORDI";
                            recv_con_dtl = "ok";
                        }
                    }
                }

                // 장애 > 복구 - 최종 상태가 '주의 또는 경고' 상태에서 자료가 연속으로 N회 이상 자료가 수신되었을 때
                if (rcDto.getRecv_condition().equals("WARN")) {
                    // 복구(file_ok)
                    if (rcc.getCodedtl().equals("file_ok")) {
                        // 최신 이력조회
                        List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr,
                                rcc.getCriterion());
                        int cnt = 0;
                        for (ReceiveDataDto rd : rdDto) {
                            if (rd.getCodedtl().equals(rcc.getCodedtl())) {
                                cnt++;
                            }
                        }
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
                        List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd,
                                dataKindStr,
                                rcc.getCriterion());
                        int cnt = 0;
                        for (ReceiveDataDto rd : rdDto) {
                            if (rd.getCodedtl().equals(rcc.getCodedtl())) {
                                cnt++;
                            }
                        }
                        // 이력 비교
                        if (rcc.getCriterion() == cnt) {
                            new_recv_condition = "RETR";
                            recv_con_dtl = "filesize_ok";
                            // recv_con_dtl = rcc.getCodedtl();
                        }
                    }
                }

                // 네트워크 복구(network_ok)
                if (rcc.getCode().equals("TORE")) {
                    // 최신 이력 이력조회
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd,
                            dataKindStr,
                            rcc.getCriterion());
                    int cnt = 0;
                    int misscnt = 0;
                    for (ReceiveDataDto rd : rdDto) {
                        if (rd.getRecv_condition().equals(rcc.getCode())) {
                            cnt++;
                        }
                        if (rd.getRecv_condition().equals("MISS")) {
                            misscnt++;
                        }
                    }
                    if (rcc.getCriterion() == cnt) {
                        log.info("네트워크 복구(network_ok)");
                        new_recv_condition = "TORE";
                        recv_con_dtl = "network_ok";
                    }
                    // 최종 네트워크 장애이고 이력이 장애일때 최종값 장애로
                    if (rcDto.getRecv_condition().equals("TOTA") && rcc.getCriterion() == misscnt) {
                        new_recv_condition = "WARN";
                        recv_con_dtl = "file_no";
                    }

                }
                // 네트워크 장애(network_no)
                if (rcc.getCode().equals("TOTA")) {
                    // 이력조회
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr,
                            rcc.getCriterion());
                    int cnt = 0;
                    for (ReceiveDataDto rd : rdDto) {
                        if (rd.getRecv_condition().equals(rcc.getCode())) {
                            cnt++;
                        }
                    }
                    if (rcc.getCriterion() == cnt) {
                        log.info("네트워크 장애(network_no)");
                        new_recv_condition = "TOTA";
                        recv_con_dtl = "network_no";
                    }
                }
                // 장애
                if (rcc.getCode().equals("WARN")) {
                    // 이력조회
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr,
                            rcc.getCriterion());

                    // 장애(file_no)
                    if (rcc.getCodedtl().equals("file_no")) {
                        int cnt = 0;
                        for (ReceiveDataDto rd : rdDto) {
                            if (rd.getCodedtl().equals(rcc.getCodedtl())) {
                                cnt++;
                            }
                        }
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
                        // 이력 비교
                        if (rcc.getCriterion() == cnt) {
                            new_recv_condition = "WARN";
                            recv_con_dtl = "filesize_no";
                        }
                    }
                }
            }

            log.info("[결과] : " + new_recv_condition);
            log.info("[결과 상세] : " + recv_con_dtl);

            if (!new_recv_condition.equals("")) {
                // 최종상태조회
                // ReceiveConditionDto rcDto = queryService.getReceiveCondition(dataKindStr,
                // "NQC", site_cd);
                // log.info("[최종상태 조회] : " + rcDto);
                int sms_send = 0;
                if (rcDto.getRecv_condition().equals(new_recv_condition)) {
                    sms_send = rcDto.getSms_send();
                    apply_time = rcDto.getApply_time();
                } else {
                    sms_send = 0;
                    // apply_time = FormatDateUtil.formatDate("yyyy-MM-dd HH:mm:ss", new Date());
                    //
                    // 일
                    apply_time = currentTime + ":00";
                }

                // 최종상태 update
                log.info(siteStr + " 별 최종상태 update");
                log.info("new_recv_condition ::: " + new_recv_condition);
                log.info("recv_con_dtl ::: " + recv_con_dtl);
                queryService.updateReceiveCondition(apply_time, new_recv_condition,
                        recv_con_dtl, sms_send,
                        old_recv_condition, site_cd, dataKindStr, "NQC");
            } else {
                log.info("[상태 안바뀜 - update 날짜는 바뀜]");
                queryService.updateReceiveCondition(rcDto.getApply_time(),
                        rcDto.getRecv_condition(),
                        rcDto.getCodedtl(), rcDto.getSms_send(),
                        old_recv_condition, site_cd, dataKindStr, "NQC");
            }

        }

        // ======================= 문자 전송 =================================
        // 문자 메시지 발송 상태가 on 일때만
        SmsSendOnOffDto onoffDto = queryService.getSmsSendOnOffData();
        // System.out.println("onoffDto ::: " + onoffDto);
        if (onoffDto.getValue() == 1) {
            stepThree(gubun, dataKindStr, srCnt, srDto, gubunStr);
        } else {
            log.info("[장애시 문자 발송 기능 off]");
        }
    }

    @Transactional
    public void stepThree(int gubun, String dataKindStr, int srCnt, List<StationDto> srDto, String gubunStr) {

        log.info("======================= 문자 전송 =================================");

        // 전 사이트 장애/복구 메시지는 1번씩만
        List<ReceiveConditionDto> rcDtoAll = queryService.getReceiveConditionList(dataKindStr, "NQC");
        // 지점별 운영상태가 정상일 때만
        List<StationStatusDto> siteStatusDtos = queryService.getStationStatusGubun(gubun);

        String dateTime = FormatDateUtil.formatDate("yyyy-MM-dd HH:mm:ss", new Date());

        int recvTota = 0; // 장애
        int recvTore = 0; // 복구
        String recvCode = "";
        String recvCodeDtl = "";
        List<ReceiveConditionDto> rcActiveList = new ArrayList<ReceiveConditionDto>();
        for (ReceiveConditionDto rc : rcDtoAll) {
            // 전 지점 문자 발송 설정 on
            if (rc.getSms_send_activation() > 0) {
                rcActiveList.add(rc);
            }

            // 전 사이트 장애
            if (rc.getRecv_condition().equals("TOTA")) {
                recvCode = "TOTA";
                recvCodeDtl = "network_no";
                recvTota++;
            }
            // 한 사이트 이상 복구
            if (rc.getRecv_condition().equals("TORE")) {
                recvCode = "TORE";
                recvCodeDtl = "network_ok";
                recvTore++;
            }
        }
        System.out.println("[recvCode] :::: " + recvCode);
        System.out.println("[recvCodeDtl] :::: " + recvCodeDtl);
        System.out.println("[recvTota] :::: " + recvTota);
        System.out.println("[recvTore] :::: " + recvTore);

        // JSONObject jsonSmsPettern = new JSONObject();
        String smsPetterns = "";
        // int rdCnt = 0; // 최종이력상태와 결과 값 같은것

        if (srDto.size() == recvTota || recvTore > 0) {
            List<SmsSendPatternDto> smsPatternDto = queryService.getSmsSendPattern(1, 1, recvCode, recvCodeDtl);

            for (StationStatusDto status : siteStatusDtos) {
                if (status.getStatus() > 0) {
                    for (SmsSendPatternDto spDto : smsPatternDto) {
                        if (status.getSite_status().equals(spDto.getMode())) {
                            for (ReceiveConditionDto rcList : rcActiveList) {
                                if (rcList.getSite().equals(status.getSiteCd()) && rcList.getSms_send() == 0) {
                                    String siteName = "";
                                    // 네트워크 장애, 복구 일때 대형,소형,공항 구분
                                    if (recvCode.equals("TOTA") || recvCode.equals("TORE")) {
                                        siteName = gubunStr + " ";
                                    }

                                    smsPetterns = siteName + spDto.getPattern()
                                            .replace("%SITE%", status.getSite_name())
                                            .replace("%TIME%", dateTime);
                                }
                            }
                        }
                    }
                }
            }
            log.info("[smsPetterns] :: " + smsPetterns);

            String callFrom = DataCommon.getInfoConf("siteInfo", "call_from");
            String templateCode = DataCommon.getInfoConf("siteInfo", "template_code");

            // json data
            JSONObject jsonObject = new JSONObject();
//        jsonObject.put("text", smsText.replaceAll("\n", "\\\\n"));
            jsonObject.put("text", smsPetterns);

            //json to string
            String jsonToString = "";
            jsonToString = jsonObject.toJSONString().replaceAll("\"", "\\\"");

            String resDate = FormatDateUtil.formatDate("yyyyMMddHHmmss", new Date());

            // 문자메시지 패턴 정보가 없으면 못보냄.
            if (!smsPetterns.equals("")) {
                // if (rccDto.getCriterion() == rdCnt) {
                // 최종 결과의 문자 전송 상태가 0일때만 문자 전송
                // if (rcList.getSms_send() == 0) {

                // site 수신그룹 담당자에게 문자 전송
                List<SmsSendMemberDto> smsMembersDto = queryService
                        .getSmsSendMemberList(dataKindStr, null);
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

                            log.info("[날짜]: " + resDate);
                            log.info("[수신자/번호]: " + dto.getName() + ": " + callTo);
                            log.info("[발신번호]: " + callTo);
                            log.info("[템플릿코드]: " + templateCode);
                            log.info("[문자내용]: " + jsonToString);

                            // 문자 전송 insert
                            // 전화번호가 아니면 안되있는건 보낼 필요가 없지..
                            if (!callTo.equals("") && callTo.matches("[0-9]+")) {
                                queryService.intNuri2Save(resDate, callTo, callFrom, templateCode, jsonToString);
                            } else {
                                log.info("[수신번호 확인] : " + callTo);
                            }
                        }
                    }
                }

                log.info("[문자 전송 여부 update]");
                // sms_send = 1;
                int second = 60 * 4 + 30;
                String previousTime = queryService.getPreviousTime(second);
                log.info("감시 해야할 시간 이전: " + previousTime);

                if (Integer.parseInt(
                        previousTime.substring(previousTime.length() - 1, previousTime.length())) <= 5)
                    previousTime = previousTime.substring(0, previousTime.length() - 1) + "0";
                if (Integer.parseInt(
                        previousTime.substring(previousTime.length() - 1, previousTime.length())) > 5)
                    previousTime = previousTime.substring(0, previousTime.length() - 1) + "5";

                log.info("감시 해야할 시간 이후: " + previousTime);

                // update
                queryService.updateReceiveConditionSms(1, null, null, dataKindStr, "NQC");
                // } else {
                // log.info("[이미 문자 전송했으므로 안해도 됨]");
                // }
                // } else {
                // log.info("[문자 전송 체크 - 기준자료] : " + rccDto.getCriterion());
                // log.info("[문자 전송 체크 - data cnt] : " + rdCnt);
                // log.info("문자 전송 안해도 됨");
                // }
            } else {
                log.info("설정값에 따른 문자메시지 패턴 다시 확인 on/off");
            }
        }

        // Iterator<String> jspList = jsonSmsPettern.keySet().iterator();
        // if (jsonSmsPettern.length() > 0) {
        // while (jspList.hasNext()) {
        // String site = jspList.next();
        // String pattern = (String) jsonSmsPettern.get(site);
        // System.out.println(site + " //// " + jsonSmsPettern.get(site));

        // }

        // }

        if (recvCode.equals("")) {
            for (int a = 0; a < srCnt; a++) {
                String site_cd = srDto.get(a).getSiteCd();
                String siteStr = srDto.get(a).getName_kr();

                log.info("[============ " + siteStr + " 정보 ==============]");

                // 지점별 운영상태
                StationStatusDto siteStatusDto = queryService.getStationStatus(site_cd);
                // log.info("siteStatusDto ::::::: " + siteStatusDto);

                // if (siteStatusDto.getSite_status().equals("RUN")) {
                // 최종상태조회
                ReceiveConditionDto rcDto = queryService.getReceiveCondition(dataKindStr, "NQC", site_cd);
                // log.info("[최종상태 조회] : " + rcDto);

                if (rcDto.getRecv_condition().equals("ORDI")) {
                    log.info("[최종 상태 정상]");
                    // 최종상태의 SMS 발송 기능 ON/OFF
                } else if (rcDto.getSms_send_activation() == 1 && !rcDto.getRecv_condition().equals("ORDI")
                        && !rcDto.getRecv_condition().equals("TOTA") && !rcDto.getRecv_condition().equals("TORE")) {
                    String code = rcDto.getRecv_condition();
                    String codedtl = rcDto.getCodedtl();
                    int sms_send = rcDto.getSms_send();

                    // log.info("[gubun] : " + gubun);
                    log.info("[code] : " + code);
                    log.info("[codedtl] : " + codedtl);

                    // 경고 기준 (횟수 - criterion)
                    ReceiveConditionCriteriaDto rccDto = queryService.getReceiveConditionCriteria(gubun, code, codedtl);
                    // log.info("[경고 기준] : " + rccDto);
                    int criterion = rccDto.getCriterion();
                    log.info("[" + rccDto.getName() + " '" + criterion + "'회 체크]");

                    // 문자메시지 패턴
                    String smsPettern = "";
                    if (!code.equals("ORDI") && !code.equals("TOTA") && !code.equals("TORE")) {
                        // SmsSendPatternDto smsSendPatternDto = queryService.getSmsSendPattern(1, 1,
                        // "RUN", code, smsCodedtl);
                        List<SmsSendPatternDto> smsSendPatternDto = queryService.getSmsSendPattern(1, 1, code, codedtl);

                        // log.info("[date Time] : " + dateTime);

                        for (SmsSendPatternDto dto : smsSendPatternDto) {
                            if (siteStatusDto.getSite_status().equals(dto.getMode())) {
                                smsPettern = dto.getPattern();
                            }
                        }

                        smsPettern = smsPettern.replace("%SITE%", siteStr).replace("%TIME%",
                                dateTime);

                    }
                    log.info("[문자메시지 패턴] : " + smsPettern);

                    // 이력 조회
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd,
                            dataKindStr, criterion);
                    // List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd,
                    // dataKindStr, criterion);
                    int cnt = 0; // 최종이력상태와 결과 값 같은것
                    String recv_con = "";
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
                            recv_con = "ORDI"; // 정상
                        if (rd.getRecv_condition().equals("MISS"))
                            recv_con = "WARN"; // 경고
                        if (rd.getRecv_condition().equals("RETR"))
                            recv_con = "RETR"; // 복구 - 자료 미수신, 사이즈 복구
                        // if (rd.getRecv_condition().equals("TORE"))
                        // recv_con = "TORE"; // 복구 - 네트워크 복구
                        // if (rd.getRecv_condition().equals("TOTA"))
                        // recv_con = "TOTA"; // 네트워크장애

                        if (recv_con.equals(code) && criterion == rdDto.size()
                                && rccDto.getCodedtl().equals(rd.getCodedtl())) {
                            cnt++;
                        }
                    }
                    log.info("recv_con ::: " + recv_con);
                    // log.info("[cnt] : " + cnt);

                    // 문자메시지 패턴 정보가 없으면 못보냄.
                    if (!smsPettern.equals("")) {
                        if (rccDto.getCriterion() == cnt) {
                            // 최종 결과의 문자 전송 상태가 0일때만 문자 전송
                            if (sms_send == 0) {
                                // app sequence
                                Long appSeq = queryService.getAppContentNextval();
                                log.info("[문자 전송 app sequence] :" + appSeq);
                                // 문자 전송(app_send_contents) insert
                                queryService.intGaonAppSendContentsSave(appSeq, smsPettern);

                                // site 수신그룹 담당자에게 문자 전송
                                List<SmsSendMemberDto> smsDto = queryService.getSmsSendMemberList(dataKindStr, site_cd);
                                // log.info("[담당자] : " + smsDto);

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

                                            log.info("[수신자명] : " + dto.getName() + " : " + call_to);
                                            // log.info("[수신번호] : " + call_to);
                                            // log.info("[발신번호] : " + call_from);

                                            // 문자 전송(app_send_data) insert
                                            if (!call_to.equals("") && call_to.matches("[0-9]+")) { // 전화번호가 아니면 안되있는건
                                                // 보낼 필요가 없지..
                                                queryService.insGaonAppSendDataSave(appSeq, call_to, call_from); // 템플릿
                                                // 코드
                                                // 넣어야함.
                                            } else {
                                                log.info("[수신번호 확인] : " + call_to);
                                            }
                                        }
                                    }
                                }

                                log.info("[문자 전송 여부 update]");
                                sms_send = 1;
                                int second = 60 * 4 + 30;
                                String previousTime = queryService.getPreviousTime(second);
                                log.info("감시 해야할 시간 이전: " + previousTime);

                                if (Integer.parseInt(
                                        previousTime.substring(previousTime.length() - 1, previousTime.length())) <= 5)
                                    previousTime = previousTime.substring(0, previousTime.length() - 1) + "0";
                                if (Integer.parseInt(
                                        previousTime.substring(previousTime.length() - 1, previousTime.length())) > 5)
                                    previousTime = previousTime.substring(0, previousTime.length() - 1) + "5";
                                log.info("감시 해야할 시간 이후: " + previousTime);

                                queryService.updateReceiveCondition(previousTime + "00", code, codedtl,
                                        sms_send, code,
                                        site_cd, dataKindStr, "NQC");
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
                // } else {
                // log.info("[" + siteStr + "는 유지보수 상태여서 문자 전송 안됨]");
                // }
            }
        }
    }
}
