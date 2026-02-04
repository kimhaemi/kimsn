package kr.or.kimsn.radarsms.controller;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.or.kimsn.radarsms.dto.SmsSendNuri2Dto;
import lombok.ToString.Include;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import kr.or.kimsn.radarsms.common.util.DateUtil;
import kr.or.kimsn.radarsms.dto.AppErrorCodeDto;
import kr.or.kimsn.radarsms.dto.AppTemplateCodeDto;
import kr.or.kimsn.radarsms.dto.MenuDto;
import kr.or.kimsn.radarsms.dto.SmsSendDto;
import kr.or.kimsn.radarsms.dto.SmsSendOnOffDto;
import kr.or.kimsn.radarsms.dto.SmsTargetGroupDto;
import kr.or.kimsn.radarsms.dto.SmsTargetGroupMemberDto;
import kr.or.kimsn.radarsms.dto.StationDto;
import kr.or.kimsn.radarsms.service.ManageGetService;
import kr.or.kimsn.radarsms.service.MenuService;
import kr.or.kimsn.radarsms.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 문자발송
 */
@Slf4j
@RequiredArgsConstructor
@Controller
public class SmsController {

    private final MenuService menuService;
    private final SmsService smsService;
    private final ManageGetService manageGetService;

    // 문자 발송
    @GetMapping("/manage/sms_send")
    public String sms_send(@CookieValue(name = "userId", required = false) String userId, ModelMap model) {

        if (userId == null) {
            return "views/login";
        }

        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();
        map.put("menuList", menuList);
        map.put("stationList", stationList);

        model.addAttribute("list", map);

        Date today = new Date();
        SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd");
        SimpleDateFormat time = new SimpleDateFormat("HH:mm");

        model.addAttribute("nowDate", date.format(today));
        model.addAttribute("nowTime", time.format(today));

        // 문자 수신 그룹
        List<SmsTargetGroupDto> groups = manageGetService.getSmsTargetGroupList();
        model.addAttribute("groups", groups);

        // 문자 수신 그룹 멤버
        List<SmsTargetGroupMemberDto> memberList = manageGetService.getSmsTargetGroupMemberList();
        model.addAttribute("memberList", memberList);

        // 템플릿 코드정보
        List<AppTemplateCodeDto> tempCodeList = manageGetService.getAppTemplateCodeDtoList();
        model.addAttribute("tempCodeList", tempCodeList);

        return "views/manage/sms/sms_send";
    }

    // 템플릿 화면
    @GetMapping("/manage/sms_template")
    public String sms_template(@CookieValue(name = "userId", required = false) String userId, ModelMap model) {

        if (userId == null) {
            return "views/login";
        }

        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();

        map.put("menuList", menuList);
        map.put("stationList", stationList);

        model.addAttribute("list", map);

        // 템플릿 코드정보
        List<AppTemplateCodeDto> tempCodeList = manageGetService.getAppTemplateCodeDtoList();
        model.addAttribute("tempCodeList", tempCodeList);
        log.info("tempCodeList :: " + tempCodeList);

        return "views/manage/sms/sms_template";
    }

    // 문자 발송 내역
    @RequestMapping(value = "/manage/sms_send_result", method = { RequestMethod.GET, RequestMethod.POST })
    public String sms_send_result(@CookieValue(name = "userId", required = false) String userId,
            ModelMap model, HttpServletRequest request, HttpServletResponse response, Pageable pageable) {

        if (userId == null) {
            return "views/login";
        }

        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();
        map.put("menuList", menuList);
        map.put("stationList", stationList);

        model.addAttribute("list", map);

        int page = (pageable.getPageNumber() == 0) ? 0 : pageable.getPageNumber();
        log.info("pageable.getPageNumber() :::: " + pageable.getPageNumber());
        log.info("page :::: " + page);
        // pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "MSG_SEQ"));
        pageable = PageRequest.of(page, 15, Sort.by(Sort.Direction.DESC, "MSG_SEQ"));

        log.info("pageable ::::: " + pageable);

        // 오늘 날짜
        Date now = new Date();
        Date dateStart = null;
        Date dateClose = null;

        // parameter
        String sDt = request.getParameter("sDt");
        sDt = sDt == null ? DateUtil.formatDate("yyyyMM", now) : sDt;
        model.addAttribute("sDt", sDt);
        log.info("sDt :::: " + sDt);
        String smsSUC = null;
        String smsFail = null;

        String smsResult = request.getParameter("smsResult");
        if (smsResult != null && smsResult.equals("0000")) {
            smsSUC = "0000";
        } else if (smsResult != null && smsResult.equals("9999")) {
            smsFail = "9999";
        }
        model.addAttribute("smsResult", smsResult);
        System.out.println("smsResult ::: " + smsResult);

        String termStart = sDt != null ? sDt + "01000000" : DateUtil.formatDate("yyyyMM", now) + "01000000";
        String termClose = sDt != null ? sDt + "31235959" : DateUtil.formatDate("yyyyMM", now) + "31235959";

        log.info("termStart :::: " + termStart);
        log.info("termClose :::: " + termClose);

        dateStart = DateUtil.stringToDate("yyyyMMddHHmmss", termStart);
        dateClose = DateUtil.stringToDate("yyyyMMddHHmmss", termClose);

        if (dateStart == null) {
            // dateStart = DateUtils.addDays(now, -1);
            // dateStart = now;
            dateStart = DateUtil.stringToDate(DateUtil.formatDate("yyyyMM", now) + "01000000");
        }
        if (dateClose == null) {
            // dateClose = now;
            dateClose = DateUtil.stringToDate(DateUtil.formatDate("yyyyMM", now) + "31235959");
        }

        log.info("dateClose ::; " + dateClose);
        // String searchDate = DateUtil.formatDate("yyyy'년 'MM'월 'dd'일'", dateClose);
        String searchDate = DateUtil.formatDate("yyyy'년 'MM'월'", dateStart);
        System.out.println("searchDate ::::: " + searchDate);
        model.addAttribute("monthpick", searchDate);

        // String yearMonth = DateUtil.formatDate("yyyyMM", sDt);
        // System.out.println("yearMonth ::::: " + yearMonth);

        // app 발송 내역
        Page<SmsSendDto> smsRsultList = smsService.getAppSendData(pageable, Integer.parseInt(sDt), termStart,
                termClose, smsSUC, smsFail);
        model.addAttribute("smsRsultList", smsRsultList);

        List<AppErrorCodeDto> appErrorCodeList = smsService.getAppErrorCode();
        model.addAttribute("appErrorCodeList", appErrorCodeList);

        return "views/manage/sms/sms_send_result";
    }

    // 문자 발송 nuri2
    @GetMapping("/manage/sms_send_nuri2")
    public String sms_send_nuri2(@CookieValue(name = "userId", required = false) String userId, ModelMap model) {

        if (userId == null) {
            return "views/login";
        }

        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();
        map.put("menuList", menuList);
        map.put("stationList", stationList);

        model.addAttribute("list", map);

        Date today = new Date();
        SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd");
        SimpleDateFormat time = new SimpleDateFormat("HH:mm");

        model.addAttribute("nowDate", date.format(today));
        model.addAttribute("nowTime", time.format(today));

        // 문자 수신 그룹
        List<SmsTargetGroupDto> groups = manageGetService.getSmsTargetGroupList();
        model.addAttribute("groups", groups);

        // 문자 수신 그룹 멤버
        List<SmsTargetGroupMemberDto> memberList = manageGetService.getSmsTargetGroupMemberList();
        model.addAttribute("memberList", memberList);

        // 템플릿 코드정보
        List<AppTemplateCodeDto> tempCodeList = manageGetService.getAppTemplateCodeDtoList();
        model.addAttribute("tempCodeList", tempCodeList);

        return "views/manage/sms/sms_send_nuri2";
    }

    // 문자 발송 내역 nuri2
    @RequestMapping(value = "/manage/sms_send_result_nuri2", method = { RequestMethod.GET, RequestMethod.POST })
    public String sms_send_result_nuri2(@CookieValue(name = "userId", required = false) String userId,
        ModelMap model, HttpServletRequest request, HttpServletResponse response, Pageable pageable) {

        if (userId == null) {
            return "views/login";
        }

        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();
        map.put("menuList", menuList);
        map.put("stationList", stationList);

        model.addAttribute("list", map);

        int page = (pageable.getPageNumber() == 0) ? 0 : pageable.getPageNumber();
        log.info("pageable.getPageNumber() :::: " + pageable.getPageNumber());
        log.info("page :::: " + page);
        // pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "MSG_SEQ"));
        pageable = PageRequest.of(page, 15, Sort.by(Sort.Direction.DESC, "MSG_KEY"));

        System.out.println("pageable ::::: " + pageable);

        // 오늘 날짜
        Date now = new Date();
        Date dateStart = null;
        Date dateClose = null;

        // parameter
        String sDt = request.getParameter("sDt");
        sDt = sDt == null ? DateUtil.formatDate("yyyyMM", now) : sDt;
        model.addAttribute("sDt", sDt);
        log.info("sDt :::: " + sDt);
        String smsSUC = null;
        String smsFail = null;

        String smsResult = request.getParameter("smsResult");
        model.addAttribute("smsResult", smsResult);
        System.out.println("smsResult ::: " + smsResult);

        String termStart = sDt != null ? sDt + "01000000" : DateUtil.formatDate("yyyyMM", now) + "01000000";
        String termClose = sDt != null ? sDt + "31235959" : DateUtil.formatDate("yyyyMM", now) + "31235959";

        log.info("termStart :::: " + termStart);
        log.info("termClose :::: " + termClose);

        dateStart = DateUtil.stringToDate("yyyyMMddHHmmss", termStart);
        dateClose = DateUtil.stringToDate("yyyyMMddHHmmss", termClose);

        if (dateStart == null) {
            // dateStart = DateUtils.addDays(now, -1);
            // dateStart = now;
            dateStart = DateUtil.stringToDate(DateUtil.formatDate("yyyyMM", now) + "01000000");
        }
        if (dateClose == null) {
            // dateClose = now;
            dateClose = DateUtil.stringToDate(DateUtil.formatDate("yyyyMM", now) + "31235959");
        }

        log.info("dateClose ::; " + dateClose);
        // String searchDate = DateUtil.formatDate("yyyy'년 'MM'월 'dd'일'", dateClose);
        String searchDate = DateUtil.formatDate("yyyy'년 'MM'월'", dateStart);
        System.out.println("searchDate ::::: " + searchDate);
        model.addAttribute("monthpick", searchDate);

        // String yearMonth = DateUtil.formatDate("yyyyMM", sDt);
        // System.out.println("yearMonth ::::: " + yearMonth);

        // 문자 발송 내역
        Page<SmsSendNuri2Dto> smsRsultList = smsService.getSmsSendNuri2List(pageable, Integer.parseInt(sDt), termStart, termClose, smsResult);
        System.out.println("smsRsultList: " + smsRsultList);
        model.addAttribute("smsRsultList", smsRsultList);

        List<AppErrorCodeDto> appErrorCodeList = smsService.getAppErrorCode();
        model.addAttribute("appErrorCodeList", appErrorCodeList);

        return "views/manage/sms/sms_send_result_nuri2";
    }

    // 문자 발송 기능 ON/OFF 설정
    @GetMapping("/manage/sms_send_onoff")
    public String sms_send_onoff(@CookieValue(name = "userId", required = false) String userId, ModelMap model) {

        if (userId == null) {
            return "views/login";
        }

        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();

        map.put("menuList", menuList);
        map.put("stationList", stationList);

        List<SmsSendOnOffDto> smsSendOnOffData = smsService.getSmsSendOnOffData();
        // log.info("smsSendOnOffData : " + smsSendOnOffData);
        model.addAttribute("onOffData", smsSendOnOffData);

        model.addAttribute("list", map);
        return "views/manage/sms/sms_send_onoff";
    }

}
