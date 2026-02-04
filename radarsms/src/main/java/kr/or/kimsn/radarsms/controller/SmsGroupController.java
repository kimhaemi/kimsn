package kr.or.kimsn.radarsms.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import kr.or.kimsn.radarsms.dto.MenuDto;
import kr.or.kimsn.radarsms.dto.ReceiveDto;
import kr.or.kimsn.radarsms.dto.SmsTargetGroupDto;
import kr.or.kimsn.radarsms.dto.SmsTargetGroupLinkListDto;
import kr.or.kimsn.radarsms.dto.SmsTargetGroupMemberDto;
import kr.or.kimsn.radarsms.dto.SmsTargetMemberDto;
import kr.or.kimsn.radarsms.dto.StationDto;
import kr.or.kimsn.radarsms.service.ManageGetService;
import kr.or.kimsn.radarsms.service.MenuService;
import lombok.RequiredArgsConstructor;

/**
 * 문자 수신 그룹
 */
@RequiredArgsConstructor
@Controller
public class SmsGroupController {

    private final MenuService menuService;
    private final ManageGetService manageGetService;

    // 문자 수신 그룹 관리
    @GetMapping("/manage/sms_target_group")
    public String smsTargetGroup(@CookieValue(name = "userId", required = false) String userId, ModelMap model) {

        if (userId == null) {
            return "views/login";
        }

        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();
        map.put("menuList", menuList);
        map.put("stationList", stationList);

        model.addAttribute("list", map);

        // 문자 수신 그룹 관리 조회
        List<SmsTargetGroupDto> smsTargetGroupList = manageGetService.getSmsTargetGroupList();
        model.addAttribute("smsTargetGroupList", smsTargetGroupList);

        return "views/manage/smsGroup/sms_target_group";
    }

    // 문자 수신 그룹 관리 > [ *** ] 그룹 멤버 관리
    @GetMapping("/manage/sms_target_group_member")
    public String smsTargetGroupMember(@CookieValue(name = "userId", required = false) String userId, ModelMap model,
            Long gid) {

        if (userId == null) {
            return "views/login";
        }

        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();
        map.put("menuList", menuList);
        map.put("stationList", stationList);

        model.addAttribute("list", map);

        if (gid != null) {
            // 수신 그룹
            SmsTargetGroupDto group = manageGetService.getSmsTargetGroupId(gid);
            model.addAttribute("group", group);

            // 그룹에 속한 사용자
            List<SmsTargetGroupMemberDto> links = manageGetService.getSmsTargetGroupsMemberId(gid);
            model.addAttribute("links", links);

            // 그룹에 속하지 않은 멤버
            List<SmsTargetMemberDto> notlinks = manageGetService.getSmsTargetGroupsMemberIdNot(gid);
            model.addAttribute("notlinks", notlinks);
            // log.info("notlinks ::: " + notlinks);

            model.addAttribute("list", map);
            return "views/manage/smsGroup/sms_target_group_member";
        }

        return "redirect:/manage/sms_target_group";

    }

    // 그룹 관리 > [ *** ] 그룹 감시 자료 설정
    @GetMapping("/manage/sms_target_group_link")
    public String smsTargetGroupLink(@CookieValue(name = "userId", required = false) String userId, ModelMap model,
            Long gid) {

        if (userId == null) {
            return "views/login";
        }

        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();
        map.put("menuList", menuList);
        map.put("stationList", stationList);

        model.addAttribute("list", map);

        SmsTargetGroupDto smsTargetGroup = manageGetService.getSmsTargetGroupId(gid);
        model.addAttribute("smsTargetGroup", smsTargetGroup);

        List<SmsTargetGroupLinkListDto> links = manageGetService.getTableJoinAll(gid);
        model.addAttribute("links", links);
        // log.info("links ::: " + links);
        List<ReceiveDto> notlinks = manageGetService.getSmsTargetGroupNotLink(links);
        model.addAttribute("notlinks", notlinks);
        // log.info("notlinks ::: " + notlinks);

        return "views/manage/smsGroup/sms_target_group_link";
    }

    // 문자 수신자 관리
    @GetMapping("/manage/sms_target_member")
    public String smsTargetMember(@CookieValue(name = "userId", required = false) String userId, ModelMap model) {

        if (userId == null) {
            return "views/login";
        }

        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();
        map.put("menuList", menuList);
        map.put("stationList", stationList);

        model.addAttribute("list", map);

        // 문자 수신자 관리 조회
        List<SmsTargetMemberDto> targetmembers = manageGetService.getSmsTargetMemberList();
        model.addAttribute("targetmembers", targetmembers);

        return "views/manage/smsGroup/sms_target_member";
    }

    // 상시 문자 수신 그룹 관리
    @GetMapping("/manage/sms_target_monitorgroup")
    public String smsTargetMonitorgroup(@CookieValue(name = "userId", required = false) String userId, ModelMap model) {

        if (userId == null) {
            return "views/login";
        }

        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();

        map.put("menuList", menuList);
        map.put("stationList", stationList);

        model.addAttribute("list", map);

        // 그룹에 속한 사용자
        List<SmsTargetGroupMemberDto> links = manageGetService.getSmsTargetGroupsMemberId(1L);
        model.addAttribute("links", links);

        // 그룹에 속하지 않은 멤버
        List<SmsTargetMemberDto> notlinks = manageGetService.getSmsTargetGroupsMemberIdNot(1L);
        model.addAttribute("notlinks", notlinks);

        return "views/manage/smsGroup/sms_target_monitorgroup";
    }
}
