package kr.or.kimsn.radarsms.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import kr.or.kimsn.radarsms.common.util.DateUtil;
import kr.or.kimsn.radarsms.dto.MenuDto;
import kr.or.kimsn.radarsms.dto.ReceiveConditionJoinStationRdrDto;
import kr.or.kimsn.radarsms.dto.StationDto;
import kr.or.kimsn.radarsms.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 메뉴
 */
@Slf4j
@RequiredArgsConstructor
@Controller
public class MenuController {

    private final MenuService menuService;

    // 메인 화면(전체 감시)
    @GetMapping({ "", "/" })
    public String main(
            // @SessionAttribute(name = "loginUser", required = false)MembersDto MembersDto,
            @CookieValue(name = "userId", required = false) String userId,
            ModelMap model, HttpServletRequest req, HttpServletResponse res) {

        log.info("loginUser ::::: " + userId);

        if (userId == null) {
            return "views/login";
        }

        // HttpSession session = req.getSession();
        // log.info("loginUser :::::: " + session.getAttribute("loginUser"));

        // if( session.getAttribute("loginUser") == null){
        // return "views/login";
        // }

        // log.info("session.getAttribute(\"loginUser\") ::::: " +
        // session.getAttribute("loginUser"));

        // model.addAttribute("loginUser", MembersDto);
        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();

        map.put("menuList", menuList);
        map.put("stationList", stationList);

        model.addAttribute("list", map);

        // 최종수신상태
        List<ReceiveConditionJoinStationRdrDto> rcState = menuService
                .getReceiveConditionJoinStationRdrList(stationList);
        model.addAttribute("rcState", rcState);

        Date now = new Date();
        DateUtil.formatDate("yyyy/MM/dd HH:mm:ss", now);

        return "views/main/main";
    }
}
