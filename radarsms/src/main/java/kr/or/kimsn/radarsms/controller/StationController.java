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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import kr.or.kimsn.radarsms.dto.MenuDto;
import kr.or.kimsn.radarsms.dto.ReceiveConditionDto;
import kr.or.kimsn.radarsms.dto.ReceiveDataDto;
import kr.or.kimsn.radarsms.dto.ReceiveSettingDto;
import kr.or.kimsn.radarsms.dto.StationDto;
import kr.or.kimsn.radarsms.service.MenuService;
import kr.or.kimsn.radarsms.service.StationService;
import kr.or.kimsn.radarsms.common.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 지점별 감시
 */
@Slf4j
@RequiredArgsConstructor
@Controller
public class StationController {

    // @Autowired
    // private HistoricalDataService historicalDataService;

    private final MenuService menuService;
    private final StationService stationService;

    // 지점별 감시
    @GetMapping("/station/{site}")
    public String getStation(@CookieValue(name = "userId", required = false) String userId,
            @PathVariable("site") String site, ModelMap model) {

        if (userId == null) {
            return "views/login";
        }

        // lgt :낙뢰
        // rdr : radar(대형)
        // sml : 스몰
        String dataKind = !site.equals("LGT") ? "RDR" : "LGT";

        if (site.equals("IIA"))
            dataKind = "TDWR";
        if (site.equals("DJK") || site.equals("SRI") || site.equals("MIL"))
            dataKind = "SDR";

        Map<String, Object> map = new HashMap<>();

        List<MenuDto> menuList = menuService.getMenuList();
        List<StationDto> stationList = menuService.getStationList();

        map.put("menuList", menuList);
        map.put("stationList", stationList);

        // 지점별 감시
        StationDto stationDtl = stationService.getStationDetail(site);
        model.addAttribute("siteName", stationDtl.getName_kr());
        model.addAttribute("siteCd", stationDtl.getSiteCd());

        Date now = new Date();
        model.addAttribute("now", DateUtil.formatDate("yyyy-MM-dd HH:mm:ss", now));

        // 최종수신상태
        List<ReceiveConditionDto> rcState = stationService.getStationLastCheck(site, "NQC");
        model.addAttribute("rcState", rcState);

        // 자료 수신 처리 설정
        ReceiveSettingDto rdrSet = stationService.getReceiveSetting(dataKind, 1);
        model.addAttribute("rdrSet", rdrSet);

        // 3시간 전까지 data
        List<ReceiveDataDto> rdrMap = stationService.getReceiveDataThreeHour(dataKind, site, rdrSet, now);
        model.addAttribute("rdrMap", rdrMap);
        log.info("rdrMap :::: " + rdrMap);

        Map<String, List<String>> keySet = stationService.getReceiveTimeList(now, rdrSet, dataKind);
        log.info("keySet ::::::: " + keySet);
        model.addAttribute("keySet", keySet);

        // receive_condition

        model.addAttribute("list", map);

        return "views/station/station";
    }

    // 과거자료 검색
    @RequestMapping(value = "/station/hist/{site}", method = { RequestMethod.GET, RequestMethod.POST })
    // @GetMapping("/station/hist/{site}")
    public String getStationHist(@CookieValue(name = "userId", required = false) String userId,
            @PathVariable("site") String site, HttpServletRequest request, HttpServletResponse response,
            ModelMap model) {

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

        model.addAttribute("site", site);

        // parameter
        String sDt = request.getParameter("sDt");
        // String termStart = request.getParameter("termStart");
        // String termClose = request.getParameter("termClose");
        String termStart = sDt != null ? sDt + "000000" : null;
        String termClose = sDt != null ? sDt + "235959" : null;

        if (termStart == null)
            termStart = DateUtil.formatDate("yyyyMMdd", new Date()) + "000000";
        if (termClose == null)
            termClose = DateUtil.formatDate("yyyyMMdd", new Date()) + "235959";

        // 오늘 날짜
        Date now = new Date();
        Date dateStart = null;
        Date dateClose = null;

        dateStart = DateUtil.stringToDate("yyyyMMddHHmmss", termStart);
        dateClose = DateUtil.stringToDate("yyyyMMddHHmmss", termClose);

        if (dateStart == null) {
            // dateStart = DateUtils.addDays(now, -1);
            dateStart = now;
            // dateStart = DateUtil.stringToDate("yyyyMMddHHmmss", );
        }
        if (dateClose == null) {
            dateClose = now;
            // dateStart = DateUtil.stringToDate("yyyyMMddHHmmss",
            // DateUtil.formatDate("yyyyMMdd", now) + "235959");
        }

        String searchDate = DateUtil.formatDate("yyyy'년 'MM'월 'dd'일'", dateClose);
        model.addAttribute("searchDate", searchDate);

        // 레이더
        // if (dataKind.equals("RDR")) {
        // 지점별 감시
        StationDto stationDtl = stationService.getStationDetail(site);
        model.addAttribute("siteName", stationDtl.getName_kr());

        ReceiveSettingDto rdrList = stationService.getReceiveSetting(dataKind, 1);
        String data_type = rdrList.getDataType();
        model.addAttribute("rdrList", rdrList);

        // hist
        List<ReceiveDataDto> recvData = stationService.getReceiveDataList(dataKind, data_type, site, termStart,
                termClose);
        model.addAttribute("recvData", recvData);

        // }

        // 낙뢰

        return "views/station/stationHistory";
    }

}
