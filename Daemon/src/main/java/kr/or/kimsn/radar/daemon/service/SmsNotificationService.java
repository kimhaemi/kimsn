package kr.or.kimsn.radar.daemon.service;

import java.util.ArrayList;
import java.util.List;
import kr.or.kimsn.radar.daemon.service.QueryService;
import kr.or.kimsn.radar.daemon.util.ConfigManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kimsn.radar.daemon.enums.ReceiveConditionEnum;
import kr.or.kimsn.radar.daemon.enums.ReceiveCodeDtlEnum;
import kr.or.kimsn.radar.daemon.util.TimeUtil;
import kr.or.kimsn.radar.data.dto.StationDto;
import kr.or.kimsn.radar.data.dto.StationStatusDto;
import kr.or.kimsn.radar.data.dto.ReceiveConditionDto;
import kr.or.kimsn.radar.data.dto.ReceiveConditionCriteriaDto;
import kr.or.kimsn.radar.data.dto.ReceiveDataDto;
import kr.or.kimsn.radar.data.dto.SmsSendPatternDto;
import kr.or.kimsn.radar.data.dto.SmsSendMemberDto;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsNotificationService {

  private final QueryService queryService;

  @Transactional
  public void sendSmsNotifications(int gubun, String dataKindStr, int srCnt, List<StationDto> srDto, String gubunStr, String cycleId, String currentTime) {
    log.info("[{}] ======================= 💬 문자 메시지 발송 조건 검증 가동 =================================", cycleId);

    String callFrom = ConfigManager.getString("call_from");
    String templateCode = ConfigManager.getString("template_code");
    String smsTitle = queryService.getTemplateCode(templateCode).getHead();

    List<ReceiveConditionDto> rcAll = queryService.getReceiveConditionList(dataKindStr, "NQC");
    List<StationStatusDto> statusDtos = queryService.getStationStatusGubun(gubun);

    // 💡 [DateUtil 완전 제거] 시스템 자바 표준 시간 추출로 완벽 대체 전환
    String dateTime = TimeUtil.getCurrentKstString();

    int recvTota = 0;
    int recvTore = 0;
    List<ReceiveConditionDto> rcActive = new ArrayList<>();

    for (ReceiveConditionDto rc : rcAll) {
      if (rc.getSmsSendActivation() > 0) {
        rcActive.add(rc);
      }

      ReceiveConditionEnum condition = ReceiveConditionEnum.find(rc.getRecvCondition());
      if (condition == ReceiveConditionEnum.TOTA) {
        recvTota++;
      }
      if (condition == ReceiveConditionEnum.TORE) {
        recvTore++;
      }
    }

    String recvCode = "";
    String recvCodeDtl = "";
    if (srCnt == recvTota && srCnt > 0) {
      recvCode = ReceiveConditionEnum.TOTA.getCode();
      recvCodeDtl = ReceiveCodeDtlEnum.NETWORK_NO.getCode();
    } else if (recvTore > 0) {
      recvCode = ReceiveConditionEnum.TORE.getCode();
      recvCodeDtl = ReceiveCodeDtlEnum.NETWORK_OK.getCode();
    }

    StringBuilder smsPatternsBuilder = new StringBuilder();

    // 파트 A: 전 사이트 일괄 알림 대조 발송
    if (!recvCode.isEmpty()) {
      List<SmsSendPatternDto> patternDto = queryService.getSmsSendPattern(1, 1, recvCode, recvCodeDtl);
      for (StationStatusDto st : statusDtos) {
        if (st.getStatus() > 0) {
          for (SmsSendPatternDto sp : patternDto) {
            if (st.getSiteStatus().equals(sp.getMode())) {
              for (ReceiveConditionDto act : rcActive) {
                if (act.getSite().equals(st.getSiteCd()) && act.getSmsSend() == 0) {
                  String sitePrefix = gubunStr + " ";
                  String resolvedPattern = sitePrefix + sp.getPattern().replace("%SITE%", st.getSiteName()).replace("%TIME%", dateTime);

                  if (smsPatternsBuilder.indexOf(resolvedPattern) == -1) {
                    if (smsPatternsBuilder.length() > 0) {
                      smsPatternsBuilder.append("\n");
                    }
                    smsPatternsBuilder.append(resolvedPattern);
                  }
                }
              }
            }
          }
        }
      }

      if (smsPatternsBuilder.length() > 0) {
        executeSmsInsert(dataKindStr, recvCode, callFrom, templateCode, smsTitle, smsPatternsBuilder.toString(), null);
        queryService.updateReceiveConditionSms(1, null, null, dataKindStr, "NQC");
      }
    }

    // 파트 B: 개별 지점별 단독 주의/품질이상/미수신 장애 문자 전송
    if (recvCode.isEmpty()) {
      for (int a = 0; a < srCnt; a++) {
        String siteCd = srDto.get(a).getSiteCd();
        String siteStr = srDto.get(a).getNameKr();
        StationStatusDto siteStatus = queryService.getStationStatus(siteCd);
        ReceiveConditionDto rcDto = queryService.getReceiveCondition(dataKindStr, "NQC", siteCd);

        if (rcDto == null) continue;

        ReceiveConditionEnum currentStatus = ReceiveConditionEnum.find(rcDto.getRecvCondition());

        if (currentStatus != ReceiveConditionEnum.ORDI && rcDto.getSmsSendActivation() == 1
            && currentStatus != ReceiveConditionEnum.TOTA && currentStatus != ReceiveConditionEnum.TORE) {

          ReceiveConditionCriteriaDto rcc = queryService.getReceiveConditionCriteria(gubun, rcDto.getRecvCondition(), rcDto.getCodedtl());
          if (rcc == null) continue;

          String indPattern = "";
          for (SmsSendPatternDto dto : queryService.getSmsSendPattern(1, 1, rcDto.getRecvCondition(), rcDto.getCodedtl())) {
            if (siteStatus != null && siteStatus.getSiteStatus().equals(dto.getMode())) {
              indPattern = dto.getPattern();
            }
          }

          if (indPattern.isEmpty()) continue;
          indPattern = indPattern.replace("%SITE%", siteStr).replace("%TIME%", dateTime);

          List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(siteCd, dataKindStr, rcc.getCriterion());
          long cnt = rdDto.stream().filter(rd -> {
            String rCon = "";
            if ("RECV".equals(rd.getRecv_condition())) {
              rCon = ReceiveConditionEnum.ORDI.getCode();
            } else if ("MISS".equals(rd.getRecv_condition())) {
              if (ReceiveCodeDtlEnum.FILE_SIZE_NO.getCode().equals(rd.getCodedtl())) {
                rCon = ReceiveConditionEnum.WARN_QUAL.getCode();
              } else {
                rCon = ReceiveConditionEnum.WARN_MISS.getCode();
              }
            } else if ("RETR".equals(rd.getRecv_condition())) {
              rCon = ReceiveConditionEnum.RETR.getCode();
            }
            return rCon.equals(rcDto.getRecvCondition()) && rcc.getCodedtl().equals(rd.getCodedtl());
          }).count();

          if (rcc.getCriterion() == cnt && rcDto.getSmsSend() == 0) {
            executeSmsInsert(dataKindStr, rcDto.getRecvCondition(), callFrom, templateCode, smsTitle, indPattern, siteCd);
            // 💡 [DateUtil 완전 제거] TimeUtil 연산 컴포넌트로 전면 치환
            String prevTime = TimeUtil.getAdjustedPreviousTime(TimeUtil.getPreviousTimePattern(270));
            queryService.updateReceiveCondition(prevTime + "00", rcDto.getRecvCondition(), rcDto.getCodedtl(), 1, rcDto.getRecvCondition(), siteCd, dataKindStr, "NQC");
          }
        }
      }
    }
    log.info("[{}] ======================= 💬 문자 메시지 발송 조건 검증 마감 =================================", cycleId);
  }

  private void executeSmsInsert(String dataKindStr, String code, String callFrom, String templateCode, String smsTitle, String patternStr, String siteCd) {
    String fullText = smsTitle.replaceAll("\n", "") + "\n\n" + patternStr;

    // 💡 [DateUtil 완전 제거] 포맷팅 문자열을 시스템 기본 시간 함수로 정밀 전환
    String resDate = TimeUtil.getCurrentKstString().replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
    List<SmsSendMemberDto> members = queryService.getSmsSendMemberList(dataKindStr, siteCd);

    for (SmsSendMemberDto dto : members) {
      boolean isTargetStatus = ReceiveConditionEnum.WARN_ATTN.getCode().equals(code)
          || ReceiveConditionEnum.WARN_QUAL.getCode().equals(code)
          || ReceiveConditionEnum.WARN_MISS.getCode().equals(code)
          || ReceiveConditionEnum.RETR.getCode().equals(code)
          || ReceiveConditionEnum.TOTA.getCode().equals(code)
          || ReceiveConditionEnum.TORE.getCode().equals(code);

      if (dto.getSms() == 1 && dto.getWarn() == 1 && isTargetStatus) {
        String callTo = dto.getPhone_num() != null ? dto.getPhone_num().replaceAll("-", "").trim() : "";
        if (!callTo.isEmpty() && callTo.matches("[0-9]+")) {
          queryService.intNuri2Save(resDate, callTo, callFrom, templateCode, smsTitle, fullText);
          log.info("[🚀 문자 발송 등록 완료] 수신처: {}, 메시지: {}", callTo, patternStr);
        }
      }
    }
  }
}
