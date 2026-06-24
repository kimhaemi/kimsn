package kr.or.kimsn.radar.daemon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

import kr.or.kimsn.radar.daemon.util.TimeUtil;
import kr.or.kimsn.radar.daemon.util.SftpUtil;
import kr.or.kimsn.radar.data.dto.ReceiveSettingDto;
import kr.or.kimsn.radar.data.dto.StationDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class StepOneService {

    private final QueryService queryService;
    private final ConfigurableEnvironment environment;

    public void stepOne(String mode, int gubun, StationDto srDto, String daemonType) {
        try {
            String dataKindStr = ""; 
            String dataType = "NQC"; 

            // 최신 TimeUtil API 표준 규격 완벽 반영
            final String currentTime = TimeUtil.getCurrentKstString();
            final String dataTime = TimeUtil.getCurrentUtcString();
            
            final String agencyCd = daemonType.contains("MCEE") ? "MCEE" : "KMA";

            String dataKst = TimeUtil.getAdjustedCurrentTime(gubun, agencyCd);
            String dataUtc = TimeUtil.getUTCTime(gubun, agencyCd);
            String recvConditionCheckTime = currentTime;

            String recvConditionData = "";
            String codedtl = "";
            String fileName = "";
            Long fileSize = 0L;
            String errStrData = "";

            if (gubun == 1) dataKindStr = "RDR";
            if (gubun == 2) dataKindStr = "SDR";
            if (gubun == 3) dataKindStr = "TDWR";

            System.out.println("data_kst :::: " + dataKst);
            System.out.println("dataUtc :::: " + dataUtc);

            String siteCd = srDto.getSiteCd();
            String siteStr = srDto.getNameKr();
            // =========================================================================
            // 💡 [실시간 YML 주입 가드] mode: "test" 런타임 제어 엔진 (SFTP 바이패스)
            // =========================================================================
            String globalPrefix = "radar.config.global.";
            String liveMode = environment.getProperty(globalPrefix + "mode", "real");
            String targetSite = environment.getProperty(globalPrefix + "target_site", "");

            if ("test".equalsIgnoreCase(liveMode) && (targetSite.isEmpty() || targetSite.contains(siteCd))) {
                log.info("[⚙️ 실시간 가상 테스트 가드 발동] 대상 관측소: {}", siteStr);
                
                // 외부 YML 편집 결과 즉각 미러링 파싱
                recvConditionData = environment.getProperty(globalPrefix + "recv_condition", "MISS");
                codedtl = environment.getProperty(globalPrefix + "codedtl", "file_no");
                
                fileName = "TEST_INJECTED_" + siteCd + "_" + codedtl + ".nc";
                fileSize = "ok".equals(codedtl) ? 1048576L : 500L; 
                errStrData = "[" + siteStr + " 실시간 YML 테스트 결과 강제 주입 성공] 상태: " + recvConditionData + " | 상세: " + codedtl;

                log.info("[" + siteStr + " 가상 파일 명] : " + fileName);
                log.info("[" + siteStr + " 가상 파일 size] : " + fileSize);
                log.info("[" + siteStr + " 가상 파일 recv] : " + recvConditionData);
                log.info("[" + siteStr + " 가상 파일 recvDtl] : " + codedtl);
                log.info(errStrData);

                // 가상 결과 데이터를 수집 이력 테이블에 곧바로 인서트 후 세션 조기 탈출
                queryService.insReceiveData(dataKindStr, siteCd, dataType, dataTime, dataKst + ":00", currentTime, recvConditionData, recvConditionCheckTime, fileName, fileSize, codedtl);
                return; 
            }
            // =========================================================================
            // 💡 오리지널 "real" 비즈니스 파이프라인 (신형 6인자 SftpUtil 적용 사양)
            // =========================================================================
            SftpUtil sftp = new SftpUtil();
            try {
                String pathPrefix = "radar.config.paths." + daemonType + ".stations." + siteCd + ".";
                
                int port = environment.getProperty(pathPrefix + "PORT", Integer.class, 22);
                String siteIp = environment.getProperty(pathPrefix + "IP", "");
                String siteUsername = environment.getProperty(pathPrefix + "ID", "");
                String sitePwd = environment.getProperty(pathPrefix + "PASSWORD", "");
                
                // 글로벌 스펙 타임아웃 추출
                int sessionTimeOut = environment.getProperty(globalPrefix + "sessionTimeOut", Integer.class, 5);
                int connectTimeOut = environment.getProperty(globalPrefix + "connectTimeOut", Integer.class, 35);
                
                // 소형 레이더 패스워드 고유 규칙 유지
                if (gubun == 2 && !sitePwd.isEmpty() && !sitePwd.endsWith("#") && agencyCd.equals("KMA")) {
                    sitePwd = sitePwd + "#";
                }

                log.info("[========================= {} 접속 정보 ===============================]", siteStr);
                log.info("[{} port]: {}", siteStr, port);
                log.info("[{} ip]: {}", siteStr, siteIp);
                log.info("[{} username]: {}", siteStr, siteUsername);
                log.info("[{} pwd]: {}", siteStr, sitePwd);

                ReceiveSettingDto rsDto = queryService.getrRceiveSetting(dataKindStr);
                
                if (rsDto.getPermittedWatch() == 1) {
                    log.info("[자료감시 설정 on]");
                    boolean sftpConnect = false;
                    try {
                        // 💡 요구하신 6인자 사양 오픈 메서드 인터페이스 규격 호출
                        sftpConnect = sftp.open(siteIp, siteUsername, sitePwd, port, connectTimeOut, sessionTimeOut);
                    } catch (Exception ce) {
                        System.out.println("접속오류: " + ce);
                    }

                    log.info("[{} 접속 유무]: {}", siteStr, sftpConnect);

                    //기후부 대형만 UTC: U(9시간 전), 나머지는 KST: K(현재시간)
                    String timeZone = (gubun == 1 && agencyCd.equals("MCEE")) ? "UTC" : "KST";
                    String utcTime = TimeUtil.getPreviousTimePatternUTC(0, "yyyyMMddHHmm");
                    String kstTime = TimeUtil.getPreviousTimePattern(0, "yyyyMMddHHmm");

                    if (sftpConnect) {
                        String yyyyMMdd = "";
                        String yyyyMM = "";
                        String dd = "";
                        String filePath = environment.getProperty(pathPrefix + "PATH", "");
                        
                        if(timeZone.equals("UTC")){
                            yyyyMMdd = utcTime.substring(0, 8).replaceAll("(\\d{4})(\\d{2})(\\d{2})", "$1-$2-$3");
                            yyyyMM = utcTime.substring(0, 6);
                            dd = utcTime.substring(6, 8);
                        } else {
                            // if (gubun == 2 && agencyCd.equals("KMA")) {
                            yyyyMM = kstTime.substring(0, 6);
                            dd = kstTime.substring(6, 8);
                                // }
                        }

                        filePath = filePath.replace("%yyyyMM%", yyyyMM).replace("%dd%", dd).replace("%yyyy-MM-dd%", yyyyMMdd);

                        //파일 패턴
                        String filePattern = environment.getProperty(pathPrefix + "FILE", "");
                        // log.info("[{} filePattern]: {}", siteStr, filePattern);
                        
                        int second = (gubun == 1 || gubun == 3 || (gubun == 2 && agencyCd.equals("MCEE"))) ? (60 * 4 + 30) : (60 * 2);
                        String dateFormat = "yyyyMMddHHmm";
                        // //삼척, 통고산 10분 전 시간 추출
                        // if(){
                        //     second
                        // }
                        
                        String previousTime = timeZone.equals("UTC") 
                            ? TimeUtil.getPreviousTimePatternUTC(second, dateFormat) 
                            : TimeUtil.getPreviousTimePattern(second, dateFormat);
                        
                        if (gubun == 1 || gubun == 3 || (gubun == 2 && agencyCd.equals("MCEE"))) {
                            previousTime = TimeUtil.getAdjustedPreviousTime(previousTime);
                        }
                        log.info("[{} timeZone]: {}: {}", siteStr, timeZone, previousTime);

                        recvConditionCheckTime = previousTime+ "00";

                        // log.info("[{} recvConditionCheckTime]: {}", siteStr, recvConditionCheckTime);

                        fileName = filePattern
                            .replace("%site%", siteCd)
                            .replace("%yyyyMMddHHmm%", previousTime)
                            .replace("%yyMMddHHmm%", previousTime);;
                        // if(filePattern.contains("yyyyMMddHHmmss")){
                        //     fileName = filePattern.replace("%yyyyMMddHHmmss%", previousTime);
                        // }
                            
                        log.info("[{} 찾아야할 파일명]: {}", siteStr, fileName); 
                        try {
                            //파일 존재유무
                            boolean fileExists = sftp.fileExists(filePath, fileName, siteStr, dataKindStr, filePattern);
                            log.info("[{} 파일존재유무]: {} ", siteStr, fileExists);

                            if (fileExists) {
                                Long fileSizeMin = environment.getProperty(pathPrefix + "file_size_min", Long.class, 2048L);
                                
                                //관측소 파일 사이즈 추출
                                fileSize = sftp.fileSize(filePath, fileName, siteStr); //byte
                                Long kb = fileSize / 1024; //kb

                                log.info("[{} 파일사이즈(kb) 기준 / 실제]: {}/{}", siteStr, fileSizeMin, kb);

                                // 파일 사이즈 비교
                                if (kb > fileSizeMin) {
                                    codedtl = "ok";
                                    recvConditionData = "RECV";
                                    errStrData = "[" + siteStr + " 자료 정상 수신 인서트]";
                                } else {
                                    codedtl = "filesize_no";
                                    recvConditionData = "MISS";
                                    errStrData = "[" + siteStr + " 파일 품질 이상 인서트]";
                                }
                            } else {
                                fileName = ""; fileSize = 0L; codedtl = "file_no"; recvConditionData = "MISS";
                                errStrData = "[" + siteStr + " 자료 미수신 인서트]";
                            }
                        } catch (Exception fe) {
                            fileName = ""; fileSize = 0L; codedtl = "file_no"; recvConditionData = "MISS";
                            errStrData = "[" + siteStr + " 파일 접속 오류 인서트]";
                        }
                    } else {
                        codedtl = "file_no"; recvConditionData = "MISS";
                        errStrData = "[" + siteStr + " 접속 실패 인서트]";
                    }
                    sftp.close();
                } else {
                    log.info("[{} 자료감시 설정 off]", siteStr);
                }

                if(codedtl != "ok"){
                    log.error("[❌ {} 접속 결과] : {}, {}: {}", siteStr, codedtl, recvConditionData, errStrData);
                } else {
                    log.info("[{} 접속 결과 : {}, {}: {}]", siteStr, codedtl, recvConditionData, errStrData);
                }

                queryService.insReceiveData(dataKindStr, siteCd, dataType, dataTime, dataKst + ":00", currentTime, recvConditionData, recvConditionCheckTime, fileName, fileSize, codedtl);
            } catch (Exception e) {
                sftp.close();
                log.info("StepOne Service Inner Error - {}", e);
            }
        } catch (Exception e) {
            log.info("Thread error ::: {}", e);
        }
    }
}
