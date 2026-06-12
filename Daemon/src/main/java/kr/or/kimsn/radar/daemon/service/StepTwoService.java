package kr.or.kimsn.radar.daemon.service;

import java.util.ArrayList;
import java.util.List;
import kr.or.kimsn.radar.daemon.util.ConfigManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kimsn.radar.daemon.enums.RadarTypeEnum;
import kr.or.kimsn.radar.daemon.enums.ReceiveConditionEnum;
import kr.or.kimsn.radar.daemon.enums.ReceiveCodeDtlEnum;
import kr.or.kimsn.radar.daemon.util.TimeUtil;
import kr.or.kimsn.radar.data.dto.StationDto;
import kr.or.kimsn.radar.data.dto.ReceiveDataDto;
import kr.or.kimsn.radar.data.dto.ReceiveConditionDto;
import kr.or.kimsn.radar.data.dto.ReceiveConditionCriteriaDto;

@Service
@Slf4j
@RequiredArgsConstructor
public class StepTwoService {

    private final QueryService queryService;
    private final SmsNotificationService smsNotificationService;

    @Transactional
    public void stepTwo(String gubunStr, String cycleId, String placeholder) {
        String mode = ConfigManager.getString("mode");
        int gubun = ConfigManager.getInt("gubun");
        String dataKindStr = RadarTypeEnum.find(gubun).getCode();

        // 💡 자바 8 표준 타임스탬프 계산 바인딩
        String currentTime = TimeUtil.getAdjustedCurrentTime(gubun);
        List<StationDto> srDto = (!"test".equalsIgnoreCase(mode)) ? queryService.getStation(gubun) : new ArrayList<>();
        int srCnt = (srDto != null) ? srDto.size() : 0;

        int allSiteNetNo = 0;
        if (srCnt > 0) {
            for (StationDto sd : srDto) {
                List<ReceiveDataDto> single = queryService.getReceiveDataList(sd.getSiteCd(), dataKindStr, 1);
                if (!single.isEmpty() && "MISS".equals(single.get(0).getRecv_condition()) &&
                    ReceiveCodeDtlEnum.FILE_NO.getCode().equals(single.get(0).getCodedtl())) {
                    allSiteNetNo++;
                }
            }
        }

        List<ReceiveConditionCriteriaDto> rccDtoList = queryService.getReceiveConditionCriteriaList(gubun);

        for (int a = 0; a < srCnt; a++) {
            String siteCd = srDto.get(a).getSiteCd();
            String siteStr = srDto.get(a).getNameKr();
            String newCond = "";
            String newDtl = "";
            String applyTime = "";

            log.info("[{}] [============ {} 관측 데이터 최종 정산 시작 ==============]", cycleId, siteStr);

            if (srCnt > 0 && allSiteNetNo == srCnt) {
                queryService.updateReceiveData(ReceiveConditionEnum.TOTA.getCode(), ReceiveCodeDtlEnum.NETWORK_NO.getCode(), siteCd, dataKindStr, "NQC", "MISS", currentTime);
            }

            ReceiveConditionDto rcDto = queryService.getReceiveCondition(dataKindStr, "NQC", siteCd);
            if (rcDto == null) continue;

            ReceiveConditionEnum currentStatus = ReceiveConditionEnum.find(rcDto.getRecvCondition());
            ReceiveCodeDtlEnum currentDtl = ReceiveCodeDtlEnum.find(rcDto.getCodedtl());
            String oldCond = currentStatus.getCode();

            if (currentStatus == ReceiveConditionEnum.TOTA) {
                queryService.updateReceiveData(ReceiveConditionEnum.TORE.getCode(), ReceiveCodeDtlEnum.NETWORK_OK.getCode(), siteCd, dataKindStr, "NQC", "RECV", currentTime);
            }
            if (currentStatus == ReceiveConditionEnum.WARN_ATTN || currentStatus == ReceiveConditionEnum.WARN_QUAL || currentStatus == ReceiveConditionEnum.WARN_MISS) {
                if (currentDtl == ReceiveCodeDtlEnum.FILE_NO) {
                    queryService.updateReceiveData(ReceiveConditionEnum.RETR.getCode(), ReceiveCodeDtlEnum.FILE_OK.getCode(), siteCd, dataKindStr, "NQC", "RECV", currentTime);
                }
                if (currentDtl == ReceiveCodeDtlEnum.FILE_SIZE_NO) {
                    queryService.updateReceiveData(ReceiveConditionEnum.RETR.getCode(), ReceiveCodeDtlEnum.FILE_SIZE_OK.getCode(), siteCd, dataKindStr, "NQC", "RECV", currentTime);
                }
            }

            for (ReceiveConditionCriteriaDto rcc : rccDtoList) {
                List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(siteCd, dataKindStr, rcc.getCriterion());
                ReceiveConditionEnum rccStatus = ReceiveConditionEnum.find(rcc.getCode());
                ReceiveCodeDtlEnum rccDtl = ReceiveCodeDtlEnum.find(rcc.getCodedtl());

                if (rccStatus == ReceiveConditionEnum.TORE && (currentStatus == ReceiveConditionEnum.RETR || currentStatus == ReceiveConditionEnum.TORE)) {
                    if (rdDto.stream().anyMatch(rd -> "RECV".equals(rd.getRecv_condition()) && ReceiveCodeDtlEnum.OK.getCode().equals(rd.getCodedtl()))) {
                        newCond = ReceiveConditionEnum.ORDI.getCode();
                        newDtl = ReceiveCodeDtlEnum.OK.getCode();
                    }
                }
                if ((currentStatus == ReceiveConditionEnum.WARN_ATTN || currentStatus == ReceiveConditionEnum.WARN_QUAL || currentStatus == ReceiveConditionEnum.WARN_MISS)
                    && (rccDtl == ReceiveCodeDtlEnum.FILE_OK || rccDtl == ReceiveCodeDtlEnum.FILE_SIZE_OK)) {
                    long matchCnt = rdDto.stream().filter(rd -> rccDtl.getCode().equals(rd.getCodedtl())).count();
                    if (rcc.getCriterion() == matchCnt) {
                        newCond = ReceiveConditionEnum.RETR.getCode();
                        newDtl = rccDtl.getCode();
                    }
                }
                if (rccStatus == ReceiveConditionEnum.TORE) {
                    long cnt = rdDto.stream().filter(rd -> rccStatus.getCode().equals(rd.getRecv_condition())).count();
                    long miss = rdDto.stream().filter(rd -> "MISS".equals(rd.getRecv_condition())).count();
                    if (rcc.getCriterion() == cnt) {
                        newCond = ReceiveConditionEnum.TORE.getCode();
                        newDtl = ReceiveCodeDtlEnum.NETWORK_OK.getCode();
                    }
                    if (currentStatus == ReceiveConditionEnum.TOTA && rcc.getCriterion() == miss) {
                        newCond = ReceiveConditionEnum.WARN_MISS.getCode();
                        newDtl = ReceiveCodeDtlEnum.FILE_NO.getCode();
                    }
                }
                if (rccStatus == ReceiveConditionEnum.TOTA && rcc.getCriterion() == rdDto.stream().filter(rd -> rccStatus.getCode().equals(rd.getRecv_condition())).count()) {
                    newCond = ReceiveConditionEnum.TOTA.getCode();
                    newDtl = ReceiveCodeDtlEnum.NETWORK_NO.getCode();
                }
                if (rccStatus == ReceiveConditionEnum.WARN_ATTN || rccStatus == ReceiveConditionEnum.WARN_QUAL || rccStatus == ReceiveConditionEnum.WARN_MISS) {
                    long matchedHistoryCnt = rdDto.stream().filter(rd -> rccDtl.getCode().equals(rd.getCodedtl())).count();
                    if (rcc.getCriterion() == matchedHistoryCnt) {
                        if (rccDtl == ReceiveCodeDtlEnum.FILE_NO) {
                            newCond = ReceiveConditionEnum.WARN_MISS.getCode();
                        } else if (rccDtl == ReceiveCodeDtlEnum.FILE_SIZE_NO) {
                            newCond = ReceiveConditionEnum.WARN_QUAL.getCode();
                        } else {
                            newCond = ReceiveConditionEnum.WARN_ATTN.getCode();
                        }
                        newDtl = rccDtl.getCode();
                    }
                }
            }

            if (!newCond.isEmpty()) {
                applyTime = oldCond.equals(newCond) ? rcDto.getApplyTime() : currentTime + ":00";
                queryService.updateReceiveCondition(applyTime, newCond, newDtl, oldCond.equals(newCond) ? rcDto.getSmsSend() : 0, oldCond, siteCd, dataKindStr, "NQC");
            } else {
                queryService.updateReceiveCondition(rcDto.getApplyTime(), oldCond, rcDto.getCodedtl(), rcDto.getSmsSend(), oldCond, siteCd, dataKindStr, "NQC");
            }
        }

        if (queryService.getSmsSendOnOffData() != null && queryService.getSmsSendOnOffData().getValue() == 1) {
            smsNotificationService.sendSmsNotifications(gubun, dataKindStr, srCnt, srDto, gubunStr, cycleId, currentTime);
        }
    }
}
