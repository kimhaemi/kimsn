package kr.or.kimsn.radarsms.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import kr.or.kimsn.radarsms.common.util.DateUtil;
import kr.or.kimsn.radarsms.dto.MenuDto;
import kr.or.kimsn.radarsms.dto.ReceiveSettingDto;
import kr.or.kimsn.radarsms.dto.StationDto;
import kr.or.kimsn.radarsms.service.MenuService;
import kr.or.kimsn.radarsms.service.StationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 통계
 */
@Slf4j
@RequiredArgsConstructor
@Controller
public class StatisticsController {

    private final MenuService menuService;
    private final StationService stationService;

    // @Autowired
    // private HistoricalDataService historicalDataService;

    // 조회
    @RequestMapping(value = "/stat/{site}", method = { RequestMethod.GET, RequestMethod.POST })
    public String getStation(@CookieValue(name = "userId", required = false) String userId,
            @PathVariable("site") String site,
            HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap model) {

        if (userId == null) {
            return "views/login";
        }
        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();

        map.put("menuList", menuList);
        map.put("stationList", stationList);

        model.addAttribute("list", map);

        // lgt :낙뢰
        // rdr : radar(대형)
        // sml : 스몰
        String dataKind = !site.equals("LGT") ? "RDR" : "LGT";
        if (site.equals("IIA"))
            dataKind = "TDWR";
        if (site.equals("DJK") || site.equals("SRI") || site.equals("MIL"))
            dataKind = "SDR";

        String statYear = request.getParameter("statYear");
        log.info("statYear :::: " + statYear);

        Date now = new Date();
        Date dateStart = null;
        Date dateClose = null;

        dateStart = DateUtil.stringToDate("yyyy", statYear);

        if (dateStart == null) {
            dateStart = DateUtils.truncate(now, 1);
        }
        if (dateClose == null) {
            dateClose = DateUtils.addYears(dateStart, 1);
        }
        for (StationDto sdto : stationList) {
            if (sdto.getSiteCd().equals(site)) {
                model.addAttribute("siteName", sdto.getName_kr());
                model.addAttribute("siteCd", sdto.getSiteCd());
            }
        }

        // 자료 수신 처리 설정
        ReceiveSettingDto rdrSet = stationService.getReceiveSetting(dataKind, 1);
        model.addAttribute("rdrSet", rdrSet);

        // 통계
        model = stationService.getReceiveDataSiteStatMonth(rdrSet, site, dateStart, dateClose, model);
        // model.addAttribute("recvData", recvData);
        // log.info("model :::: " + model.get("recvData"));

        model.addAttribute("statYear", DateUtil.formatDate("yyyy", dateStart));
        model.addAttribute("nowYear", DateUtil.formatDate("yyyy", now));
        model.addAttribute("now", now);

        // model.addAttribute("recv", null);
        model.addAttribute("retr", null);
        model.addAttribute("miss", null);
        model.addAttribute("tota", null);

        return "views/statistics/statistics";
    }
}
