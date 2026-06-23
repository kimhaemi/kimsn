package kr.or.kimsn.radar.web.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import kr.or.kimsn.radar.data.common.util.DateUtil;
import kr.or.kimsn.radar.data.dto.ReceiveConditionDto;
import kr.or.kimsn.radar.data.dto.ReceiveDataDto;
import kr.or.kimsn.radar.data.dto.ReceiveDataForSiteStatMonthDto;
import kr.or.kimsn.radar.data.dto.ReceiveSettingDto;
import kr.or.kimsn.radar.data.dto.StationDto;
import kr.or.kimsn.radar.data.repository.ReceiveConditionRepository;
import kr.or.kimsn.radar.data.repository.ReceiveDataForSiteStatMonthRepository;
import kr.or.kimsn.radar.data.repository.ReceiveDataRepository;
import kr.or.kimsn.radar.data.repository.ReceiveSettingRepository;
import kr.or.kimsn.radar.data.repository.StationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class StationService {

    private final StationRepository stationRepository;
    private final ReceiveConditionRepository receiveConditionRepository;
    private final ReceiveSettingRepository receiveSettingRepository;
    private final ReceiveDataRepository receiveDataRepository;
    private final ReceiveDataForSiteStatMonthRepository receiveDataForSiteStatMonthRepository;

    // 지점 데이터
    public StationDto getStationDetail(String siteCd) {
        return stationRepository.findBySiteCdOrderBySortOrder(siteCd);
    }

    // 자료 수신 처리 설정(자료 수신 처리 설정 테이블)
    public List<ReceiveSettingDto> getReceiveSetting(String dataKind) {
        return receiveSettingRepository.findByDataKind(dataKind);
    }

    public ReceiveSettingDto getReceiveSetting(String dataKind, Integer permitted_watch) {
        return receiveSettingRepository.findByDataKindAndPermittedWatch(dataKind, permitted_watch);
    }

    // 3시간 전까지의 data
    public List<ReceiveDataDto> getReceiveDataThreeHour(String dataKind, String site, ReceiveSettingDto rsDto, Date now) {

        ReceiveDataDto rdDto = new ReceiveDataDto();

        // if(rsDto.getTime_zone().equals("U")){
        // String dateStr = DateUtil.formatDate("yyyy-MM-dd HH:mm:ss",
        // DateUtils.addHours(now, -9));
        // rdDto.setData_time(dateStr);
        // } else {
        rdDto.setData_time(DateUtil.formatDate("yyyy-MM-dd HH:mm:ss", now));
        // }

        log.info("rsDto.getDataKind() : " + rsDto.getDataKind());
        log.info("rsDto.getDataType() : " + rsDto.getDataType());
        log.info("site : " + site);

        rdDto.setData_kind(rsDto.getDataKind());
        rdDto.setData_type(rsDto.getDataType());
        rdDto.setSite(site);

        return receiveDataRepository.getReceiveDataThreeHour(rdDto.getData_time(), rdDto.getData_kind(), rdDto.getSite(), rdDto.getData_type());
    }

    // time setting
    public Map<String, List<String>> getReceiveTimeList(Date now, ReceiveSettingDto rsDto, String dataKind, String agencyCd) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();

        Date date = null;
        if (rsDto.getTime_zone().equals("U")) {
            date = DateUtils.addHours(now, -9);
        } else {
            date = now;
        }

        List<String> list = new ArrayList<String>();

        boolean isSdr = "SDR".equalsIgnoreCase(dataKind != null ? dataKind.trim() : "");
        boolean isKma = "KMA".equalsIgnoreCase(agencyCd != null ? agencyCd.trim() : "");

        // 💡 KMA 소형(1분 주기) 여부 판별
        boolean isKmaSdr = isSdr && isKma;

        if (isKmaSdr) {
            // ----------------------------------------------------
            // [KMA 소형] 현재 시간 정각부터 1분 단위로 정직하게 리스트 생성
            // ----------------------------------------------------
            int hour = 3 * 60; // 3시간 = 180회
            log.info("KMA 소형 루프 횟수 ::::: " + hour);

            for (int i = 0; i < hour; i++) {
                // 현재 시간에서 1분씩 과거로 이동 (인위적인 초/분 차감 제거)
                Date targetDate = DateUtils.addMinutes(date, -i);
                String key = DateUtil.formatDate("yyyy.MM.dd_HH:mm", targetDate);
                list.add(key);
            }

        } else {
            // ----------------------------------------------------
            // [대형, 공항, MECC 소형] 5분 단위 정각 리스트 생성
            // ----------------------------------------------------
            int hour = 3 * 12; // 3시간 = 36회
            log.info("대형/공항/MECC소형 루프 횟수 ::::: " + hour);

            // 현재 시간의 분을 명확하게 5분 단위로 버림 처리하여 기준점 설정
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int currentMinute = cal.get(Calendar.MINUTE);
            cal.set(Calendar.MINUTE, (currentMinute / 5) * 5); // 예: 17분 -> 15분정각 시작
            cal.set(Calendar.SECOND, 0);
            
            Date baseDate = cal.getTime();

            for (int i = 0; i < hour; i++) {
                // 5분 단위로 과거로 이동
                Date targetDate = DateUtils.addMinutes(baseDate, -(i * 5));
                String key = DateUtil.formatDate("yyyy.MM.dd_HH:mm", targetDate);
                list.add(key);
            }
        }

        map.put(rsDto.getDataType(), list);
        return map;
    }

    // public Map<String, List<String>> getReceiveTimeList(Date now, ReceiveSettingDto rsDto, String dataKind, String agencyCd) {
    //     Map<String, List<String>> map = new HashMap<String, List<String>>();

    //     Date date = null;
    //     if (rsDto.getTime_zone().equals("U")) {
    //         date = DateUtils.addHours(now, -9);
    //     } else {
    //         date = now;
    //     }

    //     List<String> list = new ArrayList<String>();

    //     boolean isSdr = "SDR".equalsIgnoreCase(dataKind != null ? dataKind.trim() : "");
    //     boolean isKma = "KMA".equalsIgnoreCase(agencyCd != null ? agencyCd.trim() : "");

    //     if (isSdr && isKma) {
    //         // ----------------------------------------------------
    //         // [소형] 1분 단위 체크 / 1분 30초에 처리
    //         // ----------------------------------------------------
    //         int hour = 3 * 60; // 3시간 = 180회
    //         log.info("소형 루프 횟수 ::::: " + hour);

    //         // 안전하게 1분 30초(90초) 전으로 기준 시간을 이동하여 이미 처리가 완료된 데이터부터 수집
    //         Date baseDate = DateUtils.addSeconds(date, -90);

    //         for (int i = 0; i < hour; i++) {
    //             Date targetDate = DateUtils.addMinutes(baseDate, -i);
    //             String key = DateUtil.formatDate("yyyy.MM.dd_HH:mm", targetDate);
    //             list.add(key);
    //         }

    //     } else {
    //         // ----------------------------------------------------
    //         // [대형, 공항] 5분 단위 체크 / 1분 늦게 처리
    //         // ----------------------------------------------------
    //         int hour = 3 * 12; // 3시간 = 36회
    //         log.info("대형/공항 루프 횟수 ::::: " + hour);

    //         // 1분 늦게 처리되므로 안전하게 1분을 먼저 빼줌
    //         Date baseDate = DateUtils.addMinutes(date, -1);

    //         for (int i = 0; i < hour; i++) {
    //             // 5분 단위로 과거로 이동
    //             Date targetDate = DateUtils.addMinutes(baseDate, -(i * 5));
                
    //             // 분 단위를 명확하게 5분 단위로 버림(Floor) 처리
    //             Calendar cal = Calendar.getInstance();
    //             cal.setTime(targetDate);
    //             int minute = cal.get(Calendar.MINUTE);
    //             cal.set(Calendar.MINUTE, (minute / 5) * 5); // 예: 16분 -> 15분, 14분 -> 10분
    //             cal.set(Calendar.SECOND, 0);                 // 초 초기화
                
    //             String key = DateUtil.formatDate("yyyy.MM.dd_HH:mm", cal.getTime());
    //             list.add(key);
    //         }
    //     }


    //     map.put(rsDto.getDataType(), list);

    //     return map;
    // }

    // 지점별 최종 수신 확인 시각(NQC)
    public List<ReceiveConditionDto> getStationLastCheck(String siteCd, String date_Type) {
        return receiveConditionRepository.findBySiteAndDataType(siteCd, date_Type);
    }

    // 전체 최종 수신 확인 시각(NQC)
    public List<ReceiveConditionDto> getStationLastCheck(String date_Type) {
        return receiveConditionRepository.findByDataTypeOrderBySite(date_Type);
    }

    // 지점별 과거자료 검색
    public List<ReceiveDataDto> getReceiveDataList(String data_kind, String data_type, String site, String dateStart,
            String dateClose) {
        return receiveDataRepository.getReceiveDataList(data_kind, data_type, site, dateStart, dateClose);
    }

    // 통계
    public ModelMap getReceiveDataSiteStatMonth(ReceiveSettingDto rsDto, String site, Date dateStart, Date dateClose,
            ModelMap model) {

        String data_time = DateUtil.formatDate("yyyy-MM-dd HH:mm:ss", dateStart);
        String recv_time = DateUtil.formatDate("yyyy-MM-dd HH:mm:ss", dateClose);

        log.info("data_time :::: " + data_time);
        log.info("recv_time :::: " + recv_time);

        List<ReceiveDataForSiteStatMonthDto> recvData = receiveDataForSiteStatMonthRepository
                .getReceiveDataForSiteStatMonth(site, data_time, recv_time, "RECV", rsDto.getDataType());
        List<ReceiveDataForSiteStatMonthDto> retrData = receiveDataForSiteStatMonthRepository
                .getReceiveDataForSiteStatMonth(site, data_time, recv_time, "RETR", rsDto.getDataType());
        List<ReceiveDataForSiteStatMonthDto> missData = receiveDataForSiteStatMonthRepository
                .getReceiveDataForSiteStatMonth(site, data_time, recv_time, "MISS", rsDto.getDataType());

        model.addAttribute("recvMap", recvData);
        model.addAttribute("retrMap", retrData);
        model.addAttribute("missMap", missData);

        return model;
    }

}
