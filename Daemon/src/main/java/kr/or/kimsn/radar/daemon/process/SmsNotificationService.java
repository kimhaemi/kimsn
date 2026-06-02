package kr.or.kimsn.radar.daemon.process;

import java.util.*;
import javax.transaction.Transactional;
import kr.or.kimsn.radar.daemon.config.ConfigManager;
import kr.or.kimsn.radar.daemon.enums.ReceiveConditionEnum; // Enum 추가
import kr.or.kimsn.radar.daemon.enums.ReceiveCodeDtlEnum;   // Enum 추가
import kr.or.kimsn.radar.daemon.service.QueryService;
import kr.or.kimsn.radar.daemon.util.TimeUtil;
import kr.or.kimsn.radar.data.common.util.DateUtil;
import kr.or.kimsn.radar.data.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsNotificationService {

  private final QueryService queryService;

  @Transactional
  public void sendSmsNotifications(int gubun, String dataKindStr, int srCnt, List<StationDto> srDto, String gubunStr, String cycleId, String currentTime) {
    log.info("[{}] ======================= 문자 전송 프로세스 시작 =================================", cycleId);

    String callFrom = ConfigManager.getString("call_from");
    String templateCode = ConfigManager.getString("template_code");
    String smsTitle = queryService.getTemplateCode(templateCode).getHead();

    List<ReceiveConditionDto> rcAll = queryService.getReceiveConditionList(dataKindStr, "NQC");
    List<StationStatusDto> statusDtos = queryService.getStationStatusGubun(gubun);
    String dateTime = DateUtil.formatDate("yyyy-MM-dd HH:mm:ss", new Date());

    int recvTota = 0, recvTore = 0;
    String recvCode = "", recvCodeDtl = "";
    List<ReceiveConditionDto> rcActive = new ArrayList<>();

    for (ReceiveConditionDto rc : rcAll) {
      if (rc.getSms_send_activation() > 0) rcActive.add(rc);

      // ⭐ [Enum 적용] 문자열 비교 안전 검증
      ReceiveConditionEnum condition = ReceiveConditionEnum.find(rc.getRecv_condition());
      if (condition == ReceiveConditionEnum.TOTA) {
        recvCode = ReceiveConditionEnum.TOTA.getCode();
        recvCodeDtl = ReceiveCodeDtlEnum.NETWORK_NO.getCode();
        recvTota++;
      }
      if (condition == ReceiveConditionEnum.TORE) {
        recvCode = ReceiveConditionEnum.TORE.getCode();
        recvCodeDtl = ReceiveCodeDtlEnum.NETWORK_OK.getCode();
        recvTore++;
      }
    }

    String smsPatterns = "";

    // 파트 A: 전 사이트 일괄 알림 (장애 혹은 복구)
    if (srCnt == recvTota || recvTore > 0) {
      List<SmsSendPatternDto> patternDto = queryService.getSmsSendPattern(1, 1, recvCode, recvCodeDtl);
      for (StationStatusDto st : statusDtos) {
        if (st.getStatus() > 0) {
          for (SmsSendPatternDto sp : patternDto) {
            if (st.getSiteStatus().equals(sp.getMode())) {
              for (ReceiveConditionDto act : rcActive) {
                if (act.getSite().equals(st.getSiteCd()) && act.getSms_send() == 0) {
                  String siteName = (ReceiveConditionEnum.TOTA.getCode().equals(recvCode) || ReceiveConditionEnum.TORE.getCode().equals(recvCode)) ? gubunStr + " " : "";
                  smsPatterns = siteName + sp.getPattern().replace("%SITE%", st.getSiteName()).replace("%TIME%", dateTime);
                }
              }
            }
          }
        }
      }
      if (!smsPatterns.isEmpty()) {
        executeSmsInsert(dataKindStr, recvCode, callFrom, templateCode, smsTitle, smsPatterns, null);
        queryService.updateReceiveConditionSms(1, null, null, dataKindStr, "NQC");
      }
    }

    // 파트 B: 개별 사이트 단독 지점별 문자 발송
    if (recvCode.isEmpty()) {
      for (int a = 0; a < srCnt; a++) {
        String siteCd = srDto.get(a).getSiteCd(), siteStr = srDto.get(a).getName_kr();
        StationStatusDto siteStatus = queryService.getStationStatus(siteCd);
        ReceiveConditionDto rcDto = queryService.getReceiveCondition(dataKindStr, "NQC", siteCd);

        ReceiveConditionEnum currentStatus = ReceiveConditionEnum.find(rcDto.getRecv_condition());

        if (currentStatus != ReceiveConditionEnum.ORDI && rcDto.getSms_send_activation() == 1
            && currentStatus != ReceiveConditionEnum.TOTA && currentStatus != ReceiveConditionEnum.TORE) {

          ReceiveConditionCriteriaDto rcc = queryService.getReceiveConditionCriteria(gubun, rcDto.getRecv_condition(), rcDto.getCodedtl());
          String indPattern = "";
          for (SmsSendPatternDto dto : queryService.getSmsSendPattern(1, 1, rcDto.getRecv_condition(), rcDto.getCodedtl())) {
            if (siteStatus.getSiteStatus().equals(dto.getMode())) indPattern = dto.getPattern();
          }
          indPattern = indPattern.replace("%SITE%", siteStr).replace("%TIME%", dateTime);

          List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(siteCd, dataKindStr, rcc.getCriterion());
          long cnt = rdDto.stream().filter(rd -> {
            // ⭐ [Enum 적용] 복잡한 문자열 판별식 매핑 처리
            String rCon = "RECV".equals(rd.getRecv_condition()) ? ReceiveConditionEnum.ORDI.getCode() :
                ("MISS".equals(rd.getRecv_condition()) ? ReceiveConditionEnum.WARN.getCode() :
                    ("RETR".equals(rd.getRecv_condition()) ? ReceiveConditionEnum.RETR.getCode() : ""));
            return rCon.equals(rcDto.getRecv_condition()) && rcc.getCodedtl().equals(rd.getCodedtl());
          }).count();

          if (!indPattern.isEmpty() && rcc.getCriterion() == cnt && rcDto.getSms_send() == 0) {
            executeSmsInsert(dataKindStr, rcDto.getRecv_condition(), callFrom, templateCode, smsTitle, indPattern, siteCd);
            String prevTime = TimeUtil.getAdjustedPreviousTime(queryService.getPreviousTime(270));
            queryService.updateReceiveCondition(prevTime + "00", rcDto.getRecv_condition(), rcDto.getCodedtl(), 1, rcDto.getRecv_condition(), siteCd, dataKindStr, "NQC");
          }
        }
      }
    }
    log.info("[{}] ======================= 문자 전송 프로세스 종료 =================================", cycleId);
  }

  private void executeSmsInsert(String dataKindStr, String code, String callFrom, String templateCode, String smsTitle, String patternStr, String siteCd) {
    String fullText = smsTitle.replaceAll("\n", "") + "\n\n" + patternStr;
    String resDate = DateUtil.formatDate("yyyyMMddHHmmss", new Date());
    List<SmsSendMemberDto> members = queryService.getSmsSendMemberList(dataKindStr, siteCd);

    for (SmsSendMemberDto dto : members) {
      if (dto.getSms() == 1 && dto.getWarn() == 1 &&
          (ReceiveConditionEnum.WARN.getCode().equals(code) || ReceiveConditionEnum.RETR.getCode().equals(code) ||
              ReceiveConditionEnum.TOTA.getCode().equals(code) || ReceiveConditionEnum.TORE.getCode().equals(code))) {

        String callTo = dto.getPhone_num().replaceAll("-", "");
        if (!callTo.isEmpty() && callTo.matches("[0-9]+")) {
          queryService.intNuri2Save(resDate, callTo, callFrom, templateCode, smsTitle, fullText);
        }
      }
    }
  }
}
