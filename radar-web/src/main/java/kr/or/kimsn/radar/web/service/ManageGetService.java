package kr.or.kimsn.radar.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import kr.or.kimsn.radar.data.dto.AppTemplateCodeDto;
import kr.or.kimsn.radar.data.dto.ReceiveConditionCriteriaDto;
import kr.or.kimsn.radar.data.dto.ReceiveDto;
import kr.or.kimsn.radar.data.dto.ReceiveSettingDto;
import kr.or.kimsn.radar.data.dto.SmsSendPatternDto;
import kr.or.kimsn.radar.data.dto.SmsSetRcDto;
import kr.or.kimsn.radar.data.dto.SmsTargetGroupDto;
import kr.or.kimsn.radar.data.dto.SmsTargetGroupLinkListDto;
import kr.or.kimsn.radar.data.dto.SmsTargetGroupMemberDto;
import kr.or.kimsn.radar.data.dto.SmsTargetMemberDto;
import kr.or.kimsn.radar.data.dto.StationStatusDto;
import kr.or.kimsn.radar.data.repository.AppTemplateCodeRepository;
import kr.or.kimsn.radar.data.repository.ReceiveConditionCriteriaRepository;
import kr.or.kimsn.radar.data.repository.ReceiveRepository;
import kr.or.kimsn.radar.data.repository.ReceiveSettingRepository;
import kr.or.kimsn.radar.data.repository.SmsSendPatternRepository;
import kr.or.kimsn.radar.data.repository.SmsSetRcRepository;
import kr.or.kimsn.radar.data.repository.SmsTargetGroupLinkListRepository;
import kr.or.kimsn.radar.data.repository.SmsTargetGroupMemberRepository;
import kr.or.kimsn.radar.data.repository.SmsTargetGroupRepository;
import kr.or.kimsn.radar.data.repository.SmsTargetMemberRepository;
import kr.or.kimsn.radar.data.repository.StationStatusRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ManageGetService {

    private final StationStatusRepository stationStatusRepository;

    private final ReceiveConditionCriteriaRepository receiveConditionCriteriaRepository;
    private final ReceiveSettingRepository receiveSettingRepository;

    private final SmsSendPatternRepository smsSendPatternRepository;
    private final SmsTargetGroupRepository smsTargetGroupRepository;
    private final SmsTargetGroupMemberRepository smsTargetGroupMemberRepository;

    private final SmsSetRcRepository smsSetRcRepository;
    private final SmsTargetMemberRepository smsTargetMemberRepository;
    private final SmsTargetGroupLinkListRepository smsTargetGroupLinkListRepository;

    private final ReceiveRepository receiveRepository;
    private final AppTemplateCodeRepository appTemplateCodeRepository;

    /*
     * 각 지점의 현재 상태(정상운영 중인지 유지보수 상태인지...)
     */
    public List<StationStatusDto> getStationStatusList() {
        return stationStatusRepository.findByOrderBySortOrder();
    }

    // 지점/자료별 문자 발송 설정 조회(자료 수신 상태)
    public List<SmsSetRcDto> getReceiveConditionList() {
        return smsSetRcRepository.findReceiveConditionStationRdrReceiveSetting();
    }

    // 경고 기준 설정
    public List<ReceiveConditionCriteriaDto> getReceiveConditionCriteriaList() {
        return receiveConditionCriteriaRepository.findByOrderByGubunAscSortAsc();
    }

    // 자료 수신 감시 설정(자료 수신 처리 설정 테이블) - 상태가 사용인 것만
    public List<ReceiveSettingDto> getReceiveSettingList() {
        return receiveSettingRepository.findByOrderByDataKindAscPermittedWatchDesc();
    }

    // 문자 메시지 패턴
    public Map<String, String> getSmsSendPatternList() {
        Map<String, String> map = new HashMap<String, String>();

        List<SmsSendPatternDto> list = smsSendPatternRepository.findByOrderByCodeAscModeDesc();

        for (SmsSendPatternDto vo : list) {
            String key = String.valueOf(vo.getCode()) + "_" + vo.getMode() + "_" + vo.getCodedtl();
            map.put(String.valueOf(key) + "_activation", vo.getActivation()+"");
            map.put(String.valueOf(key) + "_pattern", vo.getPattern());
        }

        return map;
    }

    // 그룹관리 > 그룹 감시 자료 설정
    public List<SmsTargetGroupLinkListDto> getTableJoinAll(Long id) {
        return smsTargetGroupLinkListRepository.getTableJoinAll(id);
    }

    // 그룹관리 > 그룹 감시 자료 설정 되지 않음
    public List<ReceiveDto> getSmsTargetGroupNotLink(List<SmsTargetGroupLinkListDto> links) {
        List<ReceiveDto> rclist = receiveRepository.getReceiveTableJoin();
        List<ReceiveDto> rmlist = new ArrayList<ReceiveDto>();

        for (SmsTargetGroupLinkListDto link : links) {
            for (ReceiveDto rc : rclist) {
                if (link.getDataKind().equals(rc.getDataKind()) && link.getSite().equals(rc.getSite())
                        && link.getDataType().equals(rc.getDataType())) {
                    rmlist.add(rc);
                    break;
                }
            }
        }
        if (rmlist.size() > 0) {
            for (ReceiveDto rm : rmlist) {
                rclist.remove(rm);
            }
        }

        return rclist;
    }

    // 문자 수신 그룹 관리
    public List<SmsTargetGroupDto> getSmsTargetGroupList() {
        return smsTargetGroupRepository.findAllByOrderByAgencyCdAscGubunAscSortOrderAsc();
//        return smsTargetGroupRepository.findByOrderBySortOrderAsc();
    }

    // 문자 수신 그룹 > [ *** ] 그룹 관리
    public SmsTargetGroupDto getSmsTargetGroupId(Long gid) {
        return smsTargetGroupRepository.findById(gid).orElseThrow(() -> new NoSuchElementException("Member Not Found"));
    }

    // 문자 수신 그룹 멤버
    public List<SmsTargetGroupMemberDto> getSmsTargetGroupMemberList() {
        return smsTargetGroupMemberRepository.getSmsTargetGroupMemberList();
    }

    // 문자 수신 그룹 멤버 ([ *** ] 그룹에 속한 사용자)
    public List<SmsTargetGroupMemberDto> getSmsTargetGroupsMemberId(Long gid) {
        return smsTargetGroupMemberRepository.getSmsTargetGroupsMemberId(gid);
    }

    // 문자 수신 그룹 멤버 ([ *** ] 그룹에 속한 사용자)
    public List<SmsTargetGroupMemberDto> getSmsTargetGroupsMemberId2(List<String> gid) {
        return smsTargetGroupMemberRepository.getSmsTargetGroupsMemberId2(gid);
    }

    // 문자 수신 그룹 멤버 ([ *** ] 그룹에 속하지 않은 사용자)
    public List<SmsTargetMemberDto> getSmsTargetGroupsMemberIdNot(Long gid) {
        return smsTargetMemberRepository.getSmsTargetGroupsMemberIdNot(gid);
    }

    // 문자 수신자 관리
    public List<SmsTargetMemberDto> getSmsTargetMemberList() {
        return smsTargetMemberRepository.findByOrderByName();
    }

    // 템플릿 코드정보
    public List<AppTemplateCodeDto> getAppTemplateCodeDtoList() {
        return appTemplateCodeRepository.findAll();
    }
}