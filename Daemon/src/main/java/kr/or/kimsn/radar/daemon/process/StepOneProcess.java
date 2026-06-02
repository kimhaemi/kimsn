package kr.or.kimsn.radar.daemon.process;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import kr.or.kimsn.radar.daemon.enums.RadarTypeEnum;
import kr.or.kimsn.radar.daemon.service.QueryService;
import kr.or.kimsn.radar.daemon.config.DataCommon;
import kr.or.kimsn.radar.data.common.util.DateUtil;
import kr.or.kimsn.radar.data.common.util.SftpUtil;
import kr.or.kimsn.radar.data.dto.ReceiveSettingDto;
import kr.or.kimsn.radar.data.dto.StationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component // 스프링 빈으로 등록하여 싱글톤 재사용 보장
@RequiredArgsConstructor
public class StepOneProcess {

    private final QueryService queryService;
    private final DateTimeFormatter kstFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void stepOne(String mode, int gubun, StationDto srDto, String siteInfo, String ipInfo) {
        // finally 블록에서 접근할 수 있도록 try 밖에서 선언 및 초기화
        SftpUtil sftp = null;

        RadarTypeEnum radarType = RadarTypeEnum.find(gubun);

        try {
            String dataKindStr = radarType.getCode(); // 데이터 종류
            String dataType = "NQC"; // 데이터 타입

            int connectTimeOut = Integer.parseInt(DataCommon.getInfoConf("ipInfo", "connectTimeOut", siteInfo, ipInfo));
            int sessionTimeOut = Integer.parseInt(DataCommon.getInfoConf("ipInfo", "sessionTimeOut", siteInfo, ipInfo));

            final String currentTime = DateUtil.formatDate("yyyy-MM-dd HH:mm:ss", new Date());
            final String dataTime = DateUtil.formatDate("yyyy-MM-dd HH:mm:ss", DateUtil.changeKstToUtc(new Date()));
            String dataKst = currentTime;
            String recvConditionCheckTime = currentTime;
            String recvConditionData = "";
            String codedtl = "";
            String fileName = "";
            Long fileSize = 0L;
            String errStrData = "";



            if (gubun == 1 || gubun == 3) { // 대형, 공항 4분 30초 전
                if (Integer.parseInt(dataKst.substring(dataKst.length() - 4, dataKst.length() - 3)) <= 5)
                    dataKst = dataKst.substring(0, dataKst.length() - 4) + "0:00";
                if (Integer.parseInt(dataKst.substring(dataKst.length() - 4, dataKst.length() - 3)) > 5)
                    dataKst = dataKst.substring(0, dataKst.length() - 4) + "5:00";
            }
            if (gubun == 2) { // 소형
                dataKst = dataKst.substring(0, dataKst.length() - 2) + "00";
            }

            System.out.println("data_kst :::: " + dataKst);

            // site 접속
            sftp = new SftpUtil();

            String siteCd = srDto.getSiteCd();
            String siteStr = srDto.getName_kr();
            try {

                int port = Integer.parseInt(DataCommon.getInfoConf("ipInfo", "PORT", siteInfo, ipInfo));
                String siteIp = DataCommon.getInfoConf("ipInfo", siteCd + "_IP", siteInfo, ipInfo);
                String siteUsername = DataCommon.getInfoConf("ipInfo", siteCd + "_ID", siteInfo, ipInfo);
                String sitePwd = DataCommon.getInfoConf("ipInfo", siteCd + "_PASSWORD", siteInfo, ipInfo);
                if (gubun == 2)
                    sitePwd = DataCommon.getInfoConf("ipInfo", siteCd + "_PASSWORD", siteInfo, ipInfo) + "#";

                log.info("[========================= " + siteStr + " 접속 정보(connect, file, filesize) ===============================]");
                log.info("[" + siteStr + " port] : " + port);
                log.info("[" + siteStr + " ip] : " + siteIp);
                log.info("[" + siteStr + " username] : " + siteUsername);
                log.info("[" + siteStr + " pwd] : " + sitePwd);

                ReceiveSettingDto rsDto = queryService.getrRceiveSetting(dataKindStr);
                System.out.println("rsDto ::: " + rsDto.getPermittedWatch());

                // 자료감시 설정 on
                if (rsDto.getPermittedWatch() == 1) {
                     log.info("[자료감시 설정 on]");

                    boolean sftpConnect = false;

                    try {
                        sftpConnect = sftp.open(siteIp, siteUsername, sitePwd, port, connectTimeOut, sessionTimeOut);
                    } catch (Exception ce) {
                        System.out.println("접속오류: "+ ce);
                    }

                    // site 접속
//                    boolean sftpConnect = sftp.open(siteIp, siteUsername, sitePwd, port);
                    log.info("[" + siteStr + " 접속 유무] : " + sftpConnect);

                    // 접속 O
                    if (sftpConnect) {
                        String filePath = DataCommon.getInfoConf("siteInfo", "rdr_path", siteInfo, ipInfo);
                        if (gubun == 2) { // 소형폴더는 조합이 다르네..
                            String yearmonth = DateUtil.formatDate("yyyyMM", new Date()); // 연월
                            String day = DateUtil.formatDate("dd", new Date()); // 일
                            filePath = filePath.replace("%yyyyMM%", yearmonth).replace("%dd%", day);
                        }
                        log.info("[" + siteStr + " file_path] : " + filePath);

                        String filePattern = rsDto.getFilename_pattern();
                        String timeZone = rsDto.getTime_zone();

                        // 특정 이전시간 구하기 (예. 4분 30초 전 - 300초)
                        int second = 0;
                        String previousTime = "";

                        // 파일 날짜
                        if (gubun == 1 || gubun == 3) { // 대형, 공항 4분 30초 전
                            second = 60 * 4 + 30;
                            previousTime = queryService.getPreviousTime(second);

                            log.info("[" + siteStr + " 감시 해야할 시간 이전: " + previousTime);

                            if (Integer.parseInt(previousTime.substring(previousTime.length() - 1, previousTime.length())) <= 5)
                                previousTime = previousTime.substring(0, previousTime.length() - 1) + "0";
                            if (Integer.parseInt(previousTime.substring(previousTime.length() - 1, previousTime.length())) > 5)
                                previousTime = previousTime.substring(0, previousTime.length() - 1) + "5";
                            log.info("[" + siteStr + " 감시 해야할 시간 이후: " + previousTime);
                        }

                        if (gubun == 2) { // 소형 2분 전
                            second = 60 * 2;
                            previousTime = queryService.getPreviousTime(second);
                        }

                        // 파일 패턴으로 파일명 찾기
                        fileName = filePattern.replace("%site%", siteCd).replace("%yyyyMMddHHmm%", previousTime);
                        log.info("[" + siteStr + " 파일 패턴] : " + filePattern);
                        log.info("[" + siteStr + " 파일 명] : " + fileName);

                        try {
                            boolean fileExists = sftp.fileExists(filePath, fileName, siteCd, dataKindStr, filePattern, timeZone);
                            log.info("[" + siteStr + " 파일존재유무] : " + fileExists);

                            // 파일 O (ORDI - file_ok)
                            if (fileExists) {
                                log.info("[" + siteStr + " 파일 O]");
                                Long fileSizeMin = Long.parseLong(DataCommon.getInfoConf("siteInfo", "file_size_min", siteInfo, ipInfo));
                                Long fileSizeMax = 0L; // 23.09.15 wl
                                // Long.parseLong(DataCommon.getInfoConf("siteInfo", "file_size_max"));

                                fileSize = sftp.fileSize(filePath, fileName, fileSizeMin, fileSizeMax);
                                log.info("[" + siteStr + " file size] : " + fileSize);

                                Long kb = fileSize / 1024;
                                log.info("[" + siteStr + " kb] : " + kb);

                                // 파일 품질 정상 (ORDI - filesize_ok)
                                if (kb > fileSizeMin) {
                                    codedtl = "ok";
                                    // recvCondition = "ORDI";
                                    recvConditionData = "RECV";
                                    errStrData = "[" + siteStr + " [자료 수신 (ORDI - filesize_ok) query insert receive_data]";

                                } else {
                                    // 파일 품질 이상 (WARN - filesize_no)
                                    // recvCondition = "WARN";
                                    codedtl = "filesize_no";
                                    recvConditionData = "MISS";
                                    errStrData = "[" + siteStr
                                        + " 파일 품질 이상 (WARN - filesize_no) query insert - receive_data]";
                                }

                            } else {
                                // 파일 X (WARN - file_no)
                                // recvCondition = "WARN";
                                log.info("[" + siteStr + " 파일 X]");
                                fileName = "";
                                fileSize = 0L;
                                codedtl = "file_no";
                                recvConditionData = "MISS";
                                errStrData = "[" + siteStr + " 자료 미수신 (WARN - file_no) query insert - receive_data]";
                            }
                        } catch (Exception fe) {
                            log.info("[" + siteStr + " 파일 접속X]: "+ fe);
                            fileName = "";
                            fileSize = 0L;
                            codedtl = "file_no";
                            recvConditionData = "MISS";
                            errStrData = "[" + siteStr + " 파일 접속 오류 (TOTA - network_no) query insert - receive_data]";
                        }

                    } else {
                        // 접속 X (WARN - MISS)
                        // recvCondition = "WARN";
                        // codedtl = "siteconnect_no";

                        log.info("[" + siteStr + " 접속 X]");
                        codedtl = "file_no";
                        recvConditionData = "MISS";

                        // test
                        // codedtl = "ok";
                        // recv_condition_data = "RECV";

                        // if (site_cd.equals("BRI") || site_cd.equals("PSN")) {
                        // codedtl = "ok";
                        // recv_condition_data = "RECV";
                        // }

                        // errStr = "[접속 실패 query insert table1 - receive_condition]";
                        errStrData = "[" + siteStr + " 접속 실패 query insert - receive_data]";
                    }
                    sftp.close();
                    log.info("[/// " + siteStr + " ssh 접속 종료 /// ]");
                } else {
                    // 자료감시 설정 off
                    log.info("[" + siteStr + " 자료감시 설정 off]");
                }

                log.info("[" + siteStr + " 파일 명] : " + fileName);
                log.info("[" + siteStr + " 파일 size] : " + fileSize);
                log.info("[" + siteStr + " 파일 recv] : " + recvConditionData);
                log.info("[" + siteStr + " 파일 recvDtl] : " + codedtl);
                log.info(errStrData);

                queryService.insReceiveData(dataKindStr, siteCd, dataType, dataTime, dataKst, currentTime, recvConditionData, recvConditionCheckTime, fileName, fileSize, codedtl);
            } catch (Exception e) {
                sftp.close();
                // log.info("error : " + e);
                log.info("StepOneProcessTwo run Error - " + e);
            }
        } catch (Exception e) {
            log.info("Thread error ::: " + e);

        }

    }
}
