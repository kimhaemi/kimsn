package kr.or.kimsn.radarsms.restApiController;

import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.or.kimsn.radarsms.common.ApiResult;
import kr.or.kimsn.radarsms.dto.AppTemplateCodeDto;
import kr.or.kimsn.radarsms.dto.MembersDto;
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
import kr.or.kimsn.radarsms.service.ManagePostService;
import kr.or.kimsn.radarsms.service.MembersService;
import kr.or.kimsn.radarsms.service.SmsService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class PostController {

    private final MembersService membersService;
    private final ManagePostService managePostService;
    private final SmsService smsService;

    private final PasswordEncoder passwordEncoder;

    // 사용자 등록
    @PostMapping("/users/admin_user_create")
    public ApiResult<MembersDto> userSave(@RequestBody MembersDto membersDto) {
        membersDto.setPassword(passwordEncoder.encode(membersDto.getPassword()));
        return ApiResult.success(membersService.userAdd(membersDto));
    }

    // 사용자 수정
    @PostMapping("/users/admin_user_modify")
    public ApiResult<MembersDto> userModify(@RequestBody MembersDto membersDto) {
        membersDto.setPassword(passwordEncoder.encode(membersDto.getPassword()));
        return ApiResult.success(membersService.userModify(membersDto));
    }

    // 사용자 삭제
    @PostMapping("/users/admin_user_del")
    public ApiResult<Long> userDelete(@RequestParam Long id) {
        return ApiResult.success(membersService.userDelete(id));
    }

    // 지점/자료별 문자 발송 설정 일괄 수정
    @PostMapping("/manage/station_set_rc_modify")
    public ApiResult<Integer> receiveConditionModify(@RequestBody List<SmsSetRcDto> dtoList) {
        return ApiResult.success(managePostService.setReceiveConditionModify(dtoList));
    }

    // 경고 기준 설정 일괄 수정
    @PostMapping("/manage/station_set_rcc_modify")
    public ApiResult<Integer> receiveConditionCriteriaModify(@RequestBody List<ReceiveConditionCriteriaDto> dtoList) {
        return ApiResult.success(managePostService.setReceiveConditionCriteriaModify(dtoList));
    }

    // 자료 수신 감시 설정 일괄 수정
    @PostMapping("/manage/station_set_rs_modify")
    public ApiResult<Integer> receiveSettingModify(@RequestBody List<ReceiveSettingDto> dtoList) {
        return ApiResult.success(managePostService.setReceiveSettingModify(dtoList));
    }

    // 문자 메시지 패턴 일괄 수정
    @PostMapping("/manage/sms_set_msg_modify")
    public ApiResult<Integer> smsSetMsgModify(@RequestBody List<SmsSendPatternDto> dtoList) {
        return ApiResult.success(managePostService.setSmsSetMsgModify(dtoList));
    }

    // 지점별 운영상태 설정 일괄 수정
    @PostMapping("/manage/station_status_modify")
    public ApiResult<Integer> stationStatusModify(@RequestBody List<StationStatusDto> dtoList) {
        return ApiResult.success(managePostService.setStationStatusModify(dtoList));
    }

    // 문자 수신 그룹 관리 설정 일괄 수정
    @PostMapping("/manage/sms_target_group_modify")
    public ApiResult<Integer> smsTargetGroupModify(@RequestBody List<SmsTargetGroupDto> dtoList) {
        return ApiResult.success(managePostService.setSmsTargetGroupModify(dtoList));
    }

    // 그룹 멤버 관리 > 단계별 문자 전송 on/off
    @PostMapping("/manage/sms_target_group_member_modify")
    public ApiResult<SmsTargetMemberLinkDto> smsTargetGroupMemberModify(@RequestBody SmsTargetMemberLinkDto dto) {
        return ApiResult.success(managePostService.setSmsTargetGroupMemberModify(dto));
    }

    // 그룹 멤버 관리 > 연결 해제
    @PostMapping("/manage/sms_target_group_member_unlink")
    public ApiResult<Integer> smsTargetGroupMemberUnlink(@RequestParam Long mid, Long gid) {
        return ApiResult.success(managePostService.setSmsTargetGroupMemberUnlink(mid, gid));
    }

    // 그룹 멤버 관리 > 연결 추가
    @PostMapping("/manage/sms_target_group_member_addlink")
    public ApiResult<SmsTargetMemberLinkDto> smsTargetGroupMemberAddLink(@RequestParam Long mid, Long gid) {
        return ApiResult.success(managePostService.setSmsTargetGroupMemberAddLink(mid, gid));
    }

    // 그룹 감시 자료 설정 > 연결 해제
    @PostMapping("/manage/sms_target_group_unlink")
    public ApiResult<SmsTargetGroupLinkDto> smsTargetgroupUnlink(@RequestBody SmsTargetGroupLinkDto dto) {
        return ApiResult.success(managePostService.setSmsTargetgroupUnlink(dto));
    }

    // 그룹 감시 자료 설정 > 연결 추가
    @PostMapping("/manage/sms_target_group_addlink")
    public ApiResult<SmsTargetGroupLinkDto> smsTargetgroupAddlink(@RequestBody SmsTargetGroupLinkDto dto) {
        return ApiResult.success(managePostService.setSmsTargetgroupAddlink(dto));
    }

    // 문자 수신자 관리 > 수정
    @PostMapping("/manage/sms_target_member_modify")
    public ApiResult<SmsTargetMemberDto> smsTargetMemberModify(@RequestBody SmsTargetMemberDto dto) {
        return ApiResult.success(managePostService.setSmsTargetMemberModify(dto));
    }

    // 문자 수신자 관리 > 삭제
    @PostMapping("/manage/sms_target_member_delete")
    public ApiResult<Integer> smsTargetMemberDelete(@RequestParam Long mid) {
        return ApiResult.success(managePostService.setSmsTargetMemberDelete(mid));
    }

    // 문자 수신자 관리 > 추가
    @PostMapping("/manage/sms_target_member_add")
    public ApiResult<SmsTargetMemberDto> smsTargetMemberAdd(@RequestBody SmsTargetMemberDto dto) {
        return ApiResult.success(managePostService.setSmsTargetMemberAdd(dto));
    }

    // 문자 발송 기능 ON/OFF 설정
    @PostMapping("/manage/sms_send_onoff_modify")
    public ApiResult<Integer> smsSendOnoffModify(@RequestBody List<SmsSendOnOffDto> dto) {
        return ApiResult.success(managePostService.setSmsSendOnoffModify(dto));
    }

    // 문자 발송
    @PostMapping("/manage/sms_send_save")
    public ApiResult<String> smsSendSave(@CookieValue(name = "userId", required = false) String userId,
            @RequestBody List<Map<String, Object>> dto) {

        String SmsSendDto = smsService.smsSendsave(dto);

        if (SmsSendDto.equals("")) {
            return ApiResult.success(SmsSendDto);
        } else {
            return ApiResult.error(SmsSendDto, null, null);
        }

        // return ApiResult.success("");
    }

    // 문자 발송 저장 nuri2
    @PostMapping("/manage/sms_send_save_nuri2")
    public ApiResult<String> smsSendNuri2Save(@CookieValue(name = "userId", required = false) String userId,
        @RequestBody List<Map<String, Object>> dto) {

        String SmsSendDto = smsService.smsSendNuri2Save(dto);

        if (SmsSendDto.equals("")) {
            return ApiResult.success(SmsSendDto);
        } else {
            return ApiResult.error(SmsSendDto, null, null);
        }

        // return ApiResult.success("");
    }

    // 템플릿 등록
    @PostMapping("/manage/sms_temp_add")
    public ApiResult<Integer> setAppTemplateCodeAdd(@CookieValue(name = "userId", required = false) String userId,
            @RequestBody AppTemplateCodeDto dto) {
        return ApiResult.success(smsService.setAppTemplateCodeAdd(dto));
    }

    // 템플릿 수정
    @PostMapping("/manage/sms_temp_modify")
    public ApiResult<Integer> setAppTemplateCodeModify(@CookieValue(name = "userId", required = false) String userId,
            @RequestBody List<Map<String, Object>> dto) {
        return ApiResult.success(smsService.setAppTemplateCodeModify(dto));
    }

    // 템플릿 수정
    @PostMapping("/manage/sms_temp_delete")
    public ApiResult<Integer> setAppTemplateCodeDelete(@CookieValue(name = "userId", required = false) String userId,
            @RequestParam String tempCode) {
        return ApiResult.success(smsService.setAppTemplateCodeDelete(tempCode));
    }

}
