package kr.or.kimsn.radarsms.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import kr.or.kimsn.radarsms.dto.MenuDto;
import kr.or.kimsn.radarsms.dto.ReceiveConditionCriteriaDto;
import kr.or.kimsn.radarsms.dto.ReceiveSettingDto;
import kr.or.kimsn.radarsms.dto.SmsSetRcDto;
import kr.or.kimsn.radarsms.dto.StationDto;
import kr.or.kimsn.radarsms.dto.StationStatusDto;
import kr.or.kimsn.radarsms.service.ManageGetService;
import kr.or.kimsn.radarsms.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ManageController {

    private final MenuService menuService;
    private final ManageGetService manageGetService;

    // 지점/자료별 문자 발송 설정
    @GetMapping("/manage/station_set_rc")
    public String smsSetRc(@CookieValue(name = "userId", required = false) String userId, ModelMap model) {

        if (userId == null) {
            return "views/login";
        }

        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();
        map.put("menuList", menuList);
        map.put("stationList", stationList);

        model.addAttribute("list", map);

        // 자료 수신 상태 조회
        List<SmsSetRcDto> receiveList = manageGetService.getReceiveConditionList();
        model.addAttribute("receiveList", receiveList);

        return "views/manage/station_set_rc";
    }

    // 경고 기준 설정
    @GetMapping("/manage/station_set_rcc")
    public String smsSetRcc(@CookieValue(name = "userId", required = false) String userId, ModelMap model) {

        if (userId == null) {
            return "views/login";
        }

        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();
        map.put("menuList", menuList);
        map.put("stationList", stationList);

        model.addAttribute("list", map);

        // 경고 기준 조회
        List<ReceiveConditionCriteriaDto> criteriaList = manageGetService.getReceiveConditionCriteriaList();
        log.info("criteriaList ::: " + criteriaList);
        map.put("criteriaList", criteriaList);

        return "views/manage/station_set_rcc";
    }

    // 자료 수신 감시 설정
    @GetMapping("/manage/station_set_rs")
    public String smsSetRs(@CookieValue(name = "userId", required = false) String userId, ModelMap model) {

        if (userId == null) {
            return "views/login";
        }

        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();
        map.put("menuList", menuList);
        map.put("stationList", stationList);

        model.addAttribute("list", map);

        // 자료 수신 감시 설정 조회
        List<ReceiveSettingDto> receiveSettingList = manageGetService.getReceiveSettingList();
        model.addAttribute("receiveSettingList", receiveSettingList);

        return "views/manage/station_set_rs";
    }

    // 문자 메시지 패턴
    @GetMapping("/manage/sms_set_msg")
    public String smsSetMsg(@CookieValue(name = "userId", required = false) String userId, ModelMap model) {

        if (userId == null) {
            return "views/login";
        }

        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();
        map.put("menuList", menuList);
        map.put("stationList", stationList);

        model.addAttribute("list", map);

        // 문자 메시지 패턴 조회
        // List<SmsSendPatternDto> smsSendPatternList =
        // manageGetService.getSmsSendPatternList();
        Map<String, String> smsSendPatternList = manageGetService.getSmsSendPatternList();
        model.addAttribute("smsSendPatternList", smsSendPatternList);
        log.info("smsSendPatternList : \n" + smsSendPatternList);

        return "views/manage/smsConfig/sms_set_msg";
    }

    // 지점별 운영상태 설정
    @GetMapping("/manage/station_status")
    public String stationStatus(@CookieValue(name = "userId", required = false) String userId, ModelMap model) {

        if (userId == null) {
            return "views/login";
        }

        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();
        map.put("menuList", menuList);
        map.put("stationList", stationList);

        model.addAttribute("list", map);

        // 지점별 운영상태 설정 조회
        List<StationStatusDto> stationStatusList = manageGetService.getStationStatusList();
        model.addAttribute("stationStatusList", stationStatusList);

        return "views/manage/station_status";
    }
}
