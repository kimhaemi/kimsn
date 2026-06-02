package kr.or.kimsn.radar.daemon.process;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import kr.or.kimsn.radar.daemon.service.QueryService;
import kr.or.kimsn.radar.daemon.config.ConfigManager;
import kr.or.kimsn.radar.daemon.enums.RadarTypeEnum;
import kr.or.kimsn.radar.daemon.enums.ReceiveConditionEnum; // Enum 추가
import kr.or.kimsn.radar.daemon.enums.ReceiveCodeDtlEnum;   // Enum 추가
import kr.or.kimsn.radar.daemon.util.TimeUtil;
import kr.or.kimsn.radar.data.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StepTwoProcess {

    private QueryService queryService;
    private SmsNotificationService smsNotificationService;
    private List<StationDto> srDto;

    @Transactional
    public void stepTwo(String gubunStr, String cycleId, String placeholder) {
        String mode = ConfigManager.getString("mode");
        int gubun = ConfigManager.getInt("gubun");
        String dataKindStr = RadarTypeEnum.find(gubun).getCode();

        String currentTime = TimeUtil.getAdjustedCurrentTime(gubun);
        int srCnt = (!"test".equals(mode) && (srDto = queryService.getStation(gubun)) != null) ? srDto.size() : 0;

        int allSiteNetNo = 0;
        if (gubun != 3 && srCnt > 0) {
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
            String siteCd = srDto.get(a).getSiteCd(), siteStr = srDto.get(a).getName_kr();
            String newCond = "", newDtl = "", applyTime = "";

            log.info("[{}] [============ {} 정보 정산 ==============]", cycleId, siteStr);

            if (allSiteNetNo == srCnt) {
                queryService.updateReceiveData(ReceiveConditionEnum.TOTA.getCode(), ReceiveCodeDtlEnum.NETWORK_NO.getCode(), siteCd, dataKindStr, "NQC", "MISS", currentTime);
            }

            ReceiveConditionDto rcDto = queryService.getReceiveCondition(dataKindStr, "NQC", siteCd);

            // ⭐ [Enum 적용] 마스터 상태 획득 및 치환
            ReceiveConditionEnum currentStatus = ReceiveConditionEnum.find(rcDto.getRecv_condition());
            ReceiveCodeDtlEnum currentDtl = ReceiveCodeDtlEnum.find(rcDto.getCodedtl());
            String oldCond = currentStatus.getCode();

            if (currentStatus == ReceiveConditionEnum.TOTA) {
                queryService.updateReceiveData(ReceiveConditionEnum.TORE.getCode(), ReceiveCodeDtlEnum.NETWORK_OK.getCode(), siteCd, dataKindStr, "NQC", "RECV", currentTime);
            }
            if (currentStatus == ReceiveConditionEnum.WARN) {
                if (currentDtl == ReceiveCodeDtlEnum.FILE_NO) queryService.updateReceiveData(ReceiveConditionEnum.RETR.getCode(), ReceiveCodeDtlEnum.FILE_OK.getCode(), siteCd, dataKindStr, "NQC", "RECV", currentTime);
                if (currentDtl == ReceiveCodeDtlEnum.FILE_SIZE_NO) queryService.updateReceiveData(ReceiveConditionEnum.RETR.getCode(), ReceiveCodeDtlEnum.FILE_SIZE_OK.getCode(), siteCd, dataKindStr, "NQC", "RECV", currentTime);
            }

            // 연속 기준 규칙 대조 검증 매트릭스
            for (ReceiveConditionCriteriaDto rcc : rccDtoList) {
                List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(siteCd, dataKindStr, rcc.getCriterion());
                ReceiveConditionEnum rccStatus = ReceiveConditionEnum.find(rcc.getCode());
                ReceiveCodeDtlEnum rccDtl = ReceiveCodeDtlEnum.find(rcc.getCodedtl());

                if (rccStatus == ReceiveConditionEnum.TORE && (currentStatus == ReceiveConditionEnum.RETR || currentStatus == ReceiveConditionEnum.TORE)) {
                    if (rdDto.stream().anyMatch(rd -> "RECV".equals(rd.getRecv_condition()) && ReceiveCodeDtlEnum.OK.getCode().equals(rd.getCodedtl()))) {
                        newCond = ReceiveConditionEnum.ORDI.getCode(); newDtl = ReceiveCodeDtlEnum.OK.getCode();
                    }
                }
                if (currentStatus == ReceiveConditionEnum.WARN && (rccDtl == ReceiveCodeDtlEnum.FILE_OK || rccDtl == ReceiveCodeDtlEnum.FILE_SIZE_OK)) {
                    long matchCnt = rdDto.stream().filter(rd -> rccDtl.getCode().equals(rd.getCodedtl())).count();
                    if (rcc.getCriterion() == matchCnt) { newCond = ReceiveConditionEnum.RETR.getCode(); newDtl = rccDtl.getCode(); }
                }
                if (rccStatus == ReceiveConditionEnum.TORE) {
                    long cnt = rdDto.stream().filter(rd -> rccStatus.getCode().equals(rd.getRecv_condition())).count();
                    long miss = rdDto.stream().filter(rd -> "MISS".equals(rd.getRecv_condition())).count();
                    if (rcc.getCriterion() == cnt) { newCond = ReceiveConditionEnum.TORE.getCode(); newDtl = ReceiveCodeDtlEnum.NETWORK_OK.getCode(); }
                    if (currentStatus == ReceiveConditionEnum.TOTA && rcc.getCriterion() == miss) { newCond = ReceiveConditionEnum.WARN.getCode(); newDtl = ReceiveCodeDtlEnum.FILE_NO.getCode(); }
                }
                if (rccStatus == ReceiveConditionEnum.TOTA && rcc.getCriterion() == rdDto.stream().filter(rd -> rccStatus.getCode().equals(rd.getRecv_condition())).count()) {
                    newCond = ReceiveConditionEnum.TOTA.getCode(); newDtl = ReceiveCodeDtlEnum.NETWORK_NO.getCode();
                }
                if (rccStatus == ReceiveConditionEnum.WARN && (rccDtl == ReceiveCodeDtlEnum.FILE_NO || rccDtl == ReceiveCodeDtlEnum.FILE_SIZE_NO)) {
                    if (rcc.getCriterion() == rdDto.stream().filter(rd -> rccDtl.getCode().equals(rd.getCodedtl())).count()) {
                        newCond = ReceiveConditionEnum.WARN.getCode(); newDtl = rccDtl.getCode();
                    }
                }
            }

            if (!newCond.isEmpty()) {
                applyTime = oldCond.equals(newCond) ? rcDto.getApply_time() : currentTime + ":00";
                queryService.updateReceiveCondition(applyTime, newCond, newDtl, oldCond.equals(newCond) ? rcDto.getSms_send() : 0, oldCond, siteCd, dataKindStr, "NQC");
            } else {
                queryService.updateReceiveCondition(rcDto.getApply_time(), oldCond, rcDto.getCodedtl(), rcDto.getSms_send(), oldCond, siteCd, dataKindStr, "NQC");
            }
        }

        if (queryService.getSmsSendOnOffData().getValue() == 1) {
            smsNotificationService.sendSmsNotifications(gubun, dataKindStr, srCnt, srDto, gubunStr, cycleId, currentTime);
        }
    }
}
