package kr.or.kimsn.radardemo.process;

import java.util.Date;
import java.util.List;

import kr.or.kimsn.radardemo.common.DataCommon;
import kr.or.kimsn.radardemo.common.FormatDateUtil;
import kr.or.kimsn.radardemo.common.SftpUtil;
import kr.or.kimsn.radardemo.dto.ReceiveSettingDto;
import kr.or.kimsn.radardemo.dto.StationDto;
import kr.or.kimsn.radardemo.service.QueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class StepOneProcess extends Thread {
    // public class StepOneProcess implements Runnable {

    private final QueryService queryService;

    private List<StationDto> srDto;

    @Override
    public void run() {
        try {
            String gubunStr = ""; // 데몬 구분
            String dataKindStr = ""; // 데이터 종류
            String dataType = "NQC"; // 데이터 타입
            int srCnt = 0; // for문 site 갯수

            String currentTime = FormatDateUtil.formatDate("yyyy-MM-dd HH:mm:ss", new Date());
            String data_time = FormatDateUtil.formatDate("yyyy-MM-dd HH:mm:ss",
                    FormatDateUtil.changeKstToUtc(new Date()));
            // String data_kst = FormatDateUtil.formatDate("yyyyMMddHHmm", new Date());
            String data_kst = currentTime;
            String recv_condition_check_time = currentTime;
            String recv_condition_data = "";
            String codedtl = "";
            String file_name = "";
            Long file_size = 0L;
            String errStrData = "";

            int gubun = Integer.parseInt(DataCommon.getInfoConf("siteInfo", "gubun"));
            // log.info("[데몬 구분 int] : " + gubun);

            if (gubun == 1)
                gubunStr = "대형";
            if (gubun == 2)
                gubunStr = "소형";
            if (gubun == 3)
                gubunStr = "공항";
            log.info("[데몬 구분] : " + gubunStr);

            if (gubun == 1)
                dataKindStr = "RDR";
            if (gubun == 2)
                dataKindStr = "SDR";
            if (gubun == 3)
                dataKindStr = "TDWR";
            // log.info("[데이터 종류] : " + dataKindStr);

            String mode = DataCommon.getInfoConf("siteInfo", "mode");
            // log.info("[mode] : " + mode);

            if (gubun == 1 || gubun == 3) { // 대형, 공항 4분 30초 전
                if (Integer.parseInt(data_kst.substring(data_kst.length() - 4, data_kst.length() - 3)) <= 5)
                    data_kst = data_kst.substring(0, data_kst.length() - 4) + "0:00";
                if (Integer.parseInt(data_kst.substring(data_kst.length() - 4, data_kst.length() - 3)) > 5)
                    data_kst = data_kst.substring(0, data_kst.length() - 4) + "5:00";
            }
            if (gubun == 2) { // 소형
                data_kst = data_kst.substring(0, data_kst.length() - 2) + "00";
            }

            System.out.println("data_kst :::: " + data_kst);

            if (!mode.equals("test")) {
                srDto = queryService.getStation(gubun);
                srCnt = srDto.size();
            }

            // log.info("[접속 레이더 갯수] : " + srCnt);

            // log.info("data_kst :::::::::" + data_kst);

            for (int a = 0; a < srCnt; a++) {
                // site 접속
                SftpUtil sftp = new SftpUtil();

                String site_cd = srDto.get(a).getSiteCd();
                String siteStr = srDto.get(a).getName_kr();

                int port = Integer.parseInt(DataCommon.getInfoConf("ipInfo", "PORT"));
                String site_ip = DataCommon.getInfoConf("ipInfo", site_cd + "_IP");
                String site_username = DataCommon.getInfoConf("ipInfo", site_cd + "_ID");
                String site_pwd = DataCommon.getInfoConf("ipInfo", site_cd + "_PASSWORD");
                if (gubun == 2)
                    site_pwd = DataCommon.getInfoConf("ipInfo", site_cd + "_PASSWORD") + "#";

                try {

                    log.info("[============ " + siteStr + " site 접속 정보 ==============]");
                    log.info("[" + site_cd + "_port] : " + port);
                    log.info("[" + site_cd + "_ip] : " + site_ip);
                    log.info("[" + site_cd + "_username] : " + site_username);
                    log.info("[" + site_cd + "_pwd] : " + site_pwd);

                    ReceiveSettingDto rsDto = queryService.getrRceiveSetting(dataKindStr);

                    // 자료감시 설정 on
                    if (rsDto.getPermittedWatch() == 1) {
                        // log.info("[자료감시 설정 on]");

                        // site 접속
                        // SftpUtil sftp = new SftpUtil();
                        boolean sftpConnect = sftp.open(site_ip, site_username, site_pwd, port);

                        FormatDateUtil.formatDate("yyyyMMddHHmm", new Date());

                        log.info("[접속 유무] : " + sftpConnect);

                        // 접속 O
                        if (sftpConnect) {
                            String file_path = DataCommon.getInfoConf("siteInfo", "rdr_path");
                            if (gubun == 2) { // 소형폴더는 조합이 다르네..
                                String yearmonth = FormatDateUtil.formatDate("yyyyMM", new Date()); // 연월
                                String day = FormatDateUtil.formatDate("dd", new Date()); // 일
                                file_path = file_path.replace("%yyyyMM%", yearmonth).replace("%dd%", day);
                            }
                            log.info("[file_path] : " + file_path);

                            // log.info("[rsDto] :::: " + rsDto);

                            String filePattern = rsDto.getFilename_pattern();
                            String timeZone = rsDto.getTime_zone();

                            // String dateTime = FormatDateUtil.formatDate("yyyyMMddHHmm", new Date());

                            // 특정 이전시간 구하기 (예. 4분 30초 전 - 300초)
                            int second = 0;
                            String previousTime = "";

                            // 파일 날짜
                            if (gubun == 1 || gubun == 3) { // 대형, 공항 4분 30초 전
                                second = 60 * 4 + 30;
                                previousTime = queryService.getPreviousTime(second);

                                log.info("감시 해야할 시간 이전: " + previousTime);

                                if (Integer.parseInt(
                                        previousTime.substring(previousTime.length() - 1,
                                                previousTime.length())) <= 5)
                                    previousTime = previousTime.substring(0, previousTime.length() - 1) + "0";
                                if (Integer.parseInt(
                                        previousTime.substring(previousTime.length() - 1,
                                                previousTime.length())) > 5)
                                    previousTime = previousTime.substring(0, previousTime.length() - 1) + "5";
                                log.info("감시 해야할 시간 이후: " + previousTime);
                            }

                            if (gubun == 2) { // 소형 2분 전
                                second = 60 * 2;
                                previousTime = queryService.getPreviousTime(second);
                            }

                            // 파일 패턴으로 파일명 찾기
                            file_name = filePattern.replace("%site%", site_cd).replace("%yyyyMMddHHmm%",
                                    previousTime);
                            log.info("[파일 패턴] : " + filePattern);
                            log.info("[파일 명] : " + file_name);

                            boolean file_exists = sftp.fileExists(file_path, file_name, site_cd, dataKindStr,
                                    filePattern,
                                    timeZone);
                            log.info("[파일존재유무] : " + file_exists);

                            // 파일 O (ORDI - file_ok)
                            if (file_exists) {
                                log.info("[파일 O]");
                                Long file_size_min = Long
                                        .parseLong(DataCommon.getInfoConf("siteInfo", "file_size_min"));
                                Long file_size_max = 0L; // 23.09.15 wl
                                // Long.parseLong(DataCommon.getInfoConf("siteInfo", "file_size_max"));

                                file_size = sftp.fileSize(file_path, file_name, file_size_min, file_size_max);
                                log.info("[file size] : " + file_size);

                                Long kb = file_size / 1024;
                                log.info("[kb] : " + kb);

                                // 파일 품질 정상 (ORDI - filesize_ok)
                                if (kb > file_size_min) {
                                    codedtl = "ok";
                                    // recv_condition = "ORDI";
                                    recv_condition_data = "RECV";
                                    // errStr = "[자료 수신 (ORDI - filesize_ok) query insert table1 -
                                    // receive_condition]";
                                    errStrData = "[자료 수신 (ORDI - filesize_ok) query insert receive_data]";

                                } else {
                                    // 파일 품질 이상 (WARN - filesize_no)
                                    // recv_condition = "WARN";
                                    codedtl = "filesize_no";
                                    recv_condition_data = "MISS";
                                    // errStr = "[파일 품질 이상 (WARN - filesize_no) query insert table1 -
                                    // receive_condition]";
                                    errStrData = "[파일 품질 이상 (WARN - filesize_no) query insert - receive_data]";
                                }

                            } else {
                                // 파일 X (WARN - file_no)
                                // recv_condition = "WARN";
                                file_name = "";
                                file_size = 0L;
                                codedtl = "file_no";
                                recv_condition_data = "MISS";
                                // errStr = "[자료 미수신 (WARN - file_no) query insert table1 - receive_condition]";
                                errStrData = "[자료 미수신 (WARN - file_no) query insert - receive_data]";
                            }
                        } else {
                            // 접속 X (WARN - MISS)
                            // recv_condition = "WARN";
                            // codedtl = "siteconnect_no";
                            codedtl = "file_no";
                            recv_condition_data = "MISS";

                            // test
                            if (site_cd.equals("KWK")) {
                                codedtl = "ok";
                                recv_condition_data = "RECV";
                            }
                            // codedtl = "file_no";
                            // recv_condition_data = "MISS";

                            // errStr = "[접속 실패 query insert table1 - receive_condition]";
                            errStrData = "[접속 실패 query insert - receive_data]";
                        }
                        sftp.close();
                    } else {
                        // 자료감시 설정 off
                        log.info("[" + siteStr + " 자료감시 설정 off]");
                    }

                    log.info("[파일 명] : " + file_name);
                    log.info("[파일 size] : " + file_size);
                    log.info("[파일 recv] : " + recv_condition_data);
                    log.info("[파일 recvDtl] : " + codedtl);
                    log.info(errStrData);

                    queryService.insReceiveData(dataKindStr, site_cd, dataType, data_time, data_kst, currentTime,
                            recv_condition_data, recv_condition_check_time, file_name, file_size, codedtl);
                } catch (Exception e) {
                    sftp.close();
                    // log.info("error : " + e);
                    log.info("StepOneProcess run Error - " + e);
                }
            }
        } catch (Exception e) {
            log.info("Thread error ::: " + e);
            // TODO: handle exception
        }

    }
}
