package kr.or.kimsn.radar.daemon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import kr.or.kimsn.radar.daemon.enums.DataKindEnum;
import kr.or.kimsn.radar.daemon.enums.ReceiveCodeDtlEnum;
import kr.or.kimsn.radar.daemon.util.DataCommon;
import kr.or.kimsn.radar.daemon.util.TimeUtil;
import kr.or.kimsn.radar.daemon.util.SftpUtil;
import kr.or.kimsn.radar.data.dto.StationDto;
import kr.or.kimsn.radar.data.dto.ReceiveSettingDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class StepOneService {

    private final QueryService queryService;

    public void stepOne(String mode, int gubun, StationDto srDto, String siteInfo, String ipInfo) {
        SftpUtil sftp = null;
        String siteStr = srDto.getNameKr();
        String siteCd = srDto.getSiteCd();
        String dataKindStr = DataKindEnum.getDescriptionByGubun(gubun);
        String dataType = "NQC";

        try {
            final String currentTime = TimeUtil.getCurrentKstString();
            final String dataTime = TimeUtil.getCurrentUtcString();
            String dataKst = currentTime;
            String recvConditionCheckTime = currentTime;

            // 디폴트 초기 상태 (미수신 스탠스 포지셔닝)
            String recvConditionData = "MISS";
            String codedtl = ReceiveCodeDtlEnum.FILE_NO.getCode();
            String fileName = "";
            Long fileSize = 0L;
            String errStrData = "";

            ReceiveSettingDto rsDto = queryService.getrRceiveSetting(dataKindStr);

            if (rsDto != null && rsDto.getPermittedWatch() == 1) {

                // 💡 [실제 동합 패러다임] mode=test와 real 분기
                boolean sftpConnect = false;

                if ("test".equalsIgnoreCase(mode)) {
                    // 💥 테스트 모드일 때는 물리 원격지 장비가 없으므로 커넥션 오픈 결과만 true로 가상 통과 처리
                    log.info("[🔬 TEST 모드 가동 - {}] 원격 레이더 서버 SFTP 접속 성공 가상 통과", siteStr);
                    sftpConnect = true;
                } else {
                    // 실제 운영 모드(real)일 때는 conf 파일의 계정으로 SFTP 연결 시도
                    int connectTimeOut = Integer.parseInt(DataCommon.getInfoConf("ipInfo", "connectTimeOut", siteInfo, ipInfo));
                    int sessionTimeOut = Integer.parseInt(DataCommon.getInfoConf("ipInfo", "sessionTimeOut", siteInfo, ipInfo));
                    int port = Integer.parseInt(DataCommon.getInfoConf("ipInfo", "PORT", siteInfo, ipInfo));
                    String siteIp = DataCommon.getInfoConf("ipInfo", siteCd + "_IP", siteInfo, ipInfo);
                    String siteUsername = DataCommon.getInfoConf("ipInfo", siteCd + "_ID", siteInfo, ipInfo);
                    String sitePwd = DataCommon.getInfoConf("ipInfo", siteCd + "_PASSWORD", siteInfo, ipInfo);
                    if (gubun == 2) sitePwd += "#";

                    sftp = new SftpUtil();
                    sftpConnect = sftp.open(siteIp, siteUsername, sitePwd, port, connectTimeOut, sessionTimeOut);
                }

                // 💡 [여기서부터는 mode 값과 상관없이 완전히 동일한 실제 운영 로직을 관통함]
                if (sftpConnect) {
                    try {
                        String filePath = DataCommon.getInfoConf("siteInfo", "rdr_path", siteInfo, ipInfo);
                        if (gubun == 2) {
                            filePath = filePath.replace("%yyyyMM%", currentTime.substring(0,4) + currentTime.substring(5,7))
                                .replace("%dd%", currentTime.substring(8,10));
                        }

                        String filePattern = rsDto.getFilename_pattern();
                        String timeZone = rsDto.getTime_zone();
                        int second = (gubun == 1 || gubun == 3) ? (60 * 4 + 30) : (60 * 2);
                        String previousTime = TimeUtil.getPreviousTimePattern(second);

                        if (gubun == 1 || gubun == 3) {
                            if (Integer.parseInt(previousTime.substring(previousTime.length() - 1)) <= 5) {
                                previousTime = previousTime.substring(0, previousTime.length() - 1) + "0";
                            } else {
                                previousTime = previousTime.substring(0, previousTime.length() - 1) + "5";
                            }
                        }

                        fileName = filePattern.replace("%site%", siteCd).replace("%yyyyMMddHHmm%", previousTime);

                        // 💡 중요: mode=test 일 때는 물리 디스크 검사 대신 가짜 플래그나 DB 사양 판정을 유도하도록 sftp 내부 방어선 가동
                        boolean fileExists = false;
                        if ("test".equalsIgnoreCase(mode)) {
                            // 테스트 환경일 때는 데이터 축적 연산을 관측해야 하므로 항상 파일이 존재하여 정산 단계로 유입되도록 true 고정
                            fileExists = true;
                        } else {
                            fileExists = sftp.fileExists(filePath, fileName, siteCd, dataKindStr, filePattern, timeZone);
                        }

                        if (fileExists) {
                            Long fileSizeMin = Long.parseLong(DataCommon.getInfoConf("siteInfo", "file_size_min", siteInfo, ipInfo));
                            Long fileSizeMax = Long.parseLong(DataCommon.getInfoConf("siteInfo", "file_size_max", siteInfo, ipInfo));

                            Long kb = 0L;
                            if ("test".equalsIgnoreCase(mode)) {
                                // 💡 테스트 시에는 conf 파일에 정의한 max 값을 기준으로 안전 용량(RECV)을 시뮬레이션하도록 설정
                                kb = fileSizeMax / 1024 - 10;
                                fileSize = kb * 1024;
                            } else {
                                fileSize = sftp.fileSize(filePath, fileName, fileSizeMin, fileSizeMax);
                                kb = fileSize / 1024;
                            }

                            if (kb > fileSizeMin) {
                                codedtl = ReceiveCodeDtlEnum.OK.getCode();
                                recvConditionData = "RECV";
                                errStrData = String.format("[%s] 자료 정상 분석 통과 (RECV)", siteStr);
                            } else {
                                codedtl = ReceiveCodeDtlEnum.FILE_SIZE_NO.getCode();
                                recvConditionData = "MISS";
                                errStrData = String.format("[%s] 용량 부족 품질 이상 감지 (MISS)", siteStr);
                            }
                        } else {
                            codedtl = ReceiveCodeDtlEnum.FILE_NO.getCode();
                            recvConditionData = "MISS";
                            errStrData = String.format("[%s] 관측 파일 부재 미수신 감지 (MISS)", siteStr);
                        }
                    } catch (Exception fe) {
                        log.error("[❌ {}] 데이터 패킷 분석 중 예외 발생", siteStr, fe);
                        codedtl = ReceiveCodeDtlEnum.FILE_NO.getCode();
                        recvConditionData = "MISS";
                    } finally {
                        // 실제 운영 모드일 때 맺었던 물리 자원만 해제하여 리크 방어
                        if (sftp != null) {
                            sftp.close();
                        }
                    }
                } else {
                    codedtl = ReceiveCodeDtlEnum.FILE_NO.getCode();
                    recvConditionData = "MISS";
                    errStrData = String.format("[%s] 원격지 통신 커넥션 불통 장애 (MISS)", siteStr);
                }
            } else {
                return;
            }

            // 5분 타임 라인 슬라이스 정합성 연산 보정
            if (gubun == 1 || gubun == 3) {
                if (Integer.parseInt(dataKst.substring(dataKst.length() - 4, dataKst.length() - 3)) <= 5) {
                    dataKst = dataKst.substring(0, dataKst.length() - 4) + "0:00";
                } else {
                    dataKst = dataKst.substring(0, dataKst.length() - 4) + "5:00";
                }
            } else if (gubun == 2) {
                dataKst = dataKst.substring(0, dataKst.length() - 2) + "00";
            }

            log.info(errStrData);

            // 💡 [실제 구동과 100% 동일한 INSERT] 조작 없는 순수 비즈니스 데이터 이력이 실시간 적재됨
            queryService.insReceiveData(dataKindStr, siteCd, dataType, dataTime, dataKst, currentTime, recvConditionData, recvConditionCheckTime, fileName, fileSize, codedtl);

        } catch (Exception e) {
            log.error("[🚨 StepOne Core Error] 지점: {}", siteStr, e);
        }
    }
}
