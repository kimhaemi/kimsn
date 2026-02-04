package kr.or.kimsn.radarsms.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import kr.or.kimsn.radarsms.dto.ReceiveConditionCriteriaDto;
import kr.or.kimsn.radarsms.dto.ReceiveSettingDto;
import kr.or.kimsn.radarsms.dto.SmsSendOnOffDto;
import kr.or.kimsn.radarsms.dto.SmsSendPatternDto;
import kr.or.kimsn.radarsms.dto.SmsSetRcDto;
import kr.or.kimsn.radarsms.dto.SmsTargetGroupDto;
import kr.or.kimsn.radarsms.dto.SmsTargetGroupLinkDto;
import kr.or.kimsn.radarsms.dto.SmsTargetMemberDto;
import kr.or.kimsn.radarsms.dto.SmsTargetMemberLinkDto;
import kr.or.kimsn.radarsms.dto.StationStatusDto;

import kr.or.kimsn.radarsms.repository.ReceiveConditionCriteriaRepository;
import kr.or.kimsn.radarsms.repository.ReceiveConditionRepository;
import kr.or.kimsn.radarsms.repository.ReceiveSettingRepository;
import kr.or.kimsn.radarsms.repository.SmsSendOnOffRepository;
import kr.or.kimsn.radarsms.repository.SmsSendPatternRepository;
import kr.or.kimsn.radarsms.repository.SmsTargetGroupLinkRepository;
import kr.or.kimsn.radarsms.repository.SmsTargetGroupRepository;
import kr.or.kimsn.radarsms.repository.SmsTargetMemberLinkRepository;
import kr.or.kimsn.radarsms.repository.SmsTargetMemberRepository;
import kr.or.kimsn.radarsms.repository.StationStatusRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ManagePostService {

    private final ReceiveConditionRepository receiveConditionRepository;
    private final ReceiveConditionCriteriaRepository receiveConditionCriteriaRepository;
    private final ReceiveSettingRepository receiveSettingRepository;
    private final SmsSendPatternRepository smsSendPatternRepository;
    private final StationStatusRepository stationStatusRepository;
    private final SmsTargetGroupRepository smsTargetGroupRepository;
    private final SmsTargetMemberLinkRepository smsTargetMemberLinkRepository;
    private final SmsTargetGroupLinkRepository smsTargetGroupLinkRepository;
    private final SmsTargetMemberRepository smsTargetMemberRepository;
    private final SmsSendOnOffRepository smsSendOnOffRepository;

    // 지점/자료별 문자 발송 설정 일괄 수정
    public Integer setReceiveConditionModify(List<SmsSetRcDto> req) {
        Integer result = 0;
        try {
            for (SmsSetRcDto dto : req) {
                int sms_send_activation = dto.getSms_send_activation();
                String data_kind = dto.getDataKind();
                String site = dto.getSite();
                String dataType = dto.getDataType();
                receiveConditionRepository.setReceiveConditionModify(sms_send_activation, data_kind, site, dataType);
                result++;
            }
        } catch (Exception e) {
            log.info("insert error : " + e);
        }
        return result;
    }

    // 경고 기준 설정 일괄 수정
    public Integer setReceiveConditionCriteriaModify(List<ReceiveConditionCriteriaDto> receiveConditionCriteriaDto) {
        Integer result = 0;
        try {
            for (ReceiveConditionCriteriaDto dto : receiveConditionCriteriaDto) {
                int criterion = dto.getCriterion();
                String code = dto.getCode();
                Integer gubun = dto.getGubun();
                String codedtl = dto.getCodedtl();
                receiveConditionCriteriaRepository.setReceiveConditionCriteriaModify(criterion, code, gubun, codedtl);
                result++;
            }
        } catch (Exception e) {
            log.info("insert error : " + e);
        }
        return result;
    }

    // 자료 수신 감시 설정 일괄 수정
    public Integer setReceiveSettingModify(List<ReceiveSettingDto> receiveConditionCriteriaDto) {
        Integer result = 0;
        try {
            for (ReceiveSettingDto dto : receiveConditionCriteriaDto) {
                String time_zone = dto.getTime_zone();
                String filename_pattern = dto.getFilename_pattern();
                String filename_rege = dto.getFilename_regex();
                Integer delay_tolerance = dto.getDelay_tolerance();
                Integer permitted_watch = dto.getPermittedWatch();
                String data_kind = dto.getDataKind();
                String data_type = dto.getDataType();
                receiveSettingRepository.setReceiveSettingModify(time_zone, filename_pattern, filename_rege,
                        delay_tolerance, permitted_watch, data_kind, data_type);
                result++;
            }
        } catch (Exception e) {
            log.info("insert error : " + e);
        }
        return result;
    }

    // 문자 메시지 패턴 일괄 수정
    public Integer setSmsSetMsgModify(List<SmsSendPatternDto> smsSendPatternDto) {
        Integer result = 0;
        try {
            for (SmsSendPatternDto dto : smsSendPatternDto) {
                String pattern = dto.getPattern();
                String activation = dto.getActivation();
                String code = dto.getCode();
                String mode = dto.getMode();
                String codedtl = dto.getCodedtl();
                smsSendPatternRepository.setSmsSetMsgModify(pattern, activation, code, mode, codedtl);
                result++;
            }
        } catch (Exception e) {
            log.info("insert error : " + e);
        }
        return result;
    }

    // 지점별 운영상태 설정 일괄 수정
    public Integer setStationStatusModify(List<StationStatusDto> stationStatusDto) {
        Integer result = 0;
        try {
            for (StationStatusDto dto : stationStatusDto) {
                String site_cd = dto.getSiteCd();
                String site_status = dto.getSiteStatus();
                stationStatusRepository.setStationStatusModify(site_cd, site_status);
                result++;
            }
        } catch (Exception e) {
            log.info("insert error : " + e);
        }
        return result;
    }

    // 문자 수신 그룹 관리 설정 일괄 수정
    public Integer setSmsTargetGroupModify(List<SmsTargetGroupDto> smsTargetGroupDto) {
        Integer result = 0;
        try {
            for (SmsTargetGroupDto dto : smsTargetGroupDto) {
                Long id = dto.getId();
                String activation = dto.getActivation();
                smsTargetGroupRepository.setSmsTargetGroupModify(id, activation);
                result++;
            }
        } catch (Exception e) {
            log.info("insert error : " + e);
        }
        return result;
    }

    // 그룹 멤버 관리 > 단계별 문자 전송 on/off
    @Transactional
    public SmsTargetMemberLinkDto setSmsTargetGroupMemberModify(SmsTargetMemberLinkDto dto) {
        return smsTargetMemberLinkRepository.save(dto);
    }

    // 그룹 멤버 관리 > 연결 해제
    @Transactional
    public Integer setSmsTargetGroupMemberUnlink(Long mid, Long gid) {
        SmsTargetMemberLinkDto dto = new SmsTargetMemberLinkDto();
        try {
            dto.setId(mid);
            dto.setGid(gid);

            smsTargetMemberLinkRepository.delete(dto);
            // result = smsTargetMemberLinkRepository.deleteQuery(id, gid);
        } catch (Exception e) {
            log.info("insert error : " + e);
        }
        return 1;
    }

    // 그룹 멤버 관리 > 연결 추가
    @Transactional
    public SmsTargetMemberLinkDto setSmsTargetGroupMemberAddLink(Long mid, Long gid) {
        SmsTargetMemberLinkDto dto = new SmsTargetMemberLinkDto();

        dto.setId(mid);
        dto.setGid(gid);
        dto.setNoti(1);
        dto.setWarn(1);
        dto.setTota(1);
        dto.setRetr(1);
        dto.setSms(1);

        return smsTargetMemberLinkRepository.save(dto);
    }

    // 그룹 감시 자료 설정 > 연결 해제
    @Transactional
    public SmsTargetGroupLinkDto setSmsTargetgroupUnlink(SmsTargetGroupLinkDto dto) {
        try {
            smsTargetGroupLinkRepository.delete(dto);
        } catch (Exception e) {
            log.info("insert error : " + e);
        }
        return dto;
    }

    // 그룹 감시 자료 설정 > 연결 추가
    @Transactional
    public SmsTargetGroupLinkDto setSmsTargetgroupAddlink(SmsTargetGroupLinkDto dto) {
        return smsTargetGroupLinkRepository.save(dto);
    }

    // 문자 수신자 관리 > 수정
    @Transactional
    public SmsTargetMemberDto setSmsTargetMemberModify(SmsTargetMemberDto dto) {
        return smsTargetMemberRepository.save(dto);
    }

    // 문자 수신자 관리 > 삭제
    @Transactional
    public Integer setSmsTargetMemberDelete(Long mid) {
        try {
            smsTargetMemberRepository.deleteById(mid);
        } catch (Exception e) {
            log.info("insert error : " + e);
        }
        return 1;
    }

    // 문자 수신자 관리 > 추가
    @Transactional
    public SmsTargetMemberDto setSmsTargetMemberAdd(SmsTargetMemberDto dto) {
        return smsTargetMemberRepository.save(dto);
    }

    // 문자 발송 기능 ON/OFF 설정
    public Integer setSmsSendOnoffModify(List<SmsSendOnOffDto> smsSendOnOffDto) {
        Integer result = 0;
        try {
            for (SmsSendOnOffDto dto : smsSendOnOffDto) {
                smsSendOnOffRepository.save(dto);
                result++;
            }
        } catch (Exception e) {
            log.info("insert error : " + e);
        }
        return result;
    }
}
