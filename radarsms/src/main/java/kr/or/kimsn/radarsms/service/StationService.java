package kr.or.kimsn.radarsms.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import kr.or.kimsn.radarsms.common.util.DateUtil;
import kr.or.kimsn.radarsms.dto.ReceiveConditionDto;
import kr.or.kimsn.radarsms.dto.ReceiveDataDto;
import kr.or.kimsn.radarsms.dto.ReceiveDataForSiteStatMonthDto;
import kr.or.kimsn.radarsms.dto.ReceiveSettingDto;
import kr.or.kimsn.radarsms.dto.StationDto;
import kr.or.kimsn.radarsms.repository.ReceiveConditionRepository;
import kr.or.kimsn.radarsms.repository.ReceiveDataForSiteStatMonthRepository;
import kr.or.kimsn.radarsms.repository.ReceiveDataRepository;
import kr.or.kimsn.radarsms.repository.ReceiveSettingRepository;
import kr.or.kimsn.radarsms.repository.StationRepository;

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
    public List<ReceiveDataDto> getReceiveDataThreeHour(String dataKind, String site, ReceiveSettingDto rsDto,
            Date now) {

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

        return receiveDataRepository.getReceiveDataThreeHour(rdDto.getData_time(), rdDto.getData_kind(),
                rdDto.getSite(), rdDto.getData_type());
    }

    // time setting
    public Map<String, List<String>> getReceiveTimeList(Date now, ReceiveSettingDto rsDto, String dataKind) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();

        Date date = null;
        if (rsDto.getTime_zone().equals("U")) {
            date = DateUtils.addHours(now, -9);
        } else {
            date = now;
        }

        List<String> list = new ArrayList<String>();

        // 소형(1분)
        if (dataKind.equals("SDR")) {
            int hour = 3 * 30; // 3시간을 1분 단위로
            log.info("hour ::::: " + hour);
            for (int i = 0; i < hour; i++) {
                String key = DateUtil.formatDate("yyyy.MM.dd_HH:mm", DateUtils.addMinutes(date, -(i * 1)));

                // key = key.substring(0, key.length()-1) + "0";

                list.add(key);
            }

        }

        // 대형, 공항(5분)
        if (dataKind.equals("RDR") || dataKind.equals("TDWR")) {
            int hour = 3 * 12; // 3시간을 5분 단위로
            // 이게 중요한것 같네~~
            for (int i = 0; i < hour; i++) {
                // 10분단위
                // String key = DateUtil.formatDate("yyyy.MM.dd_HH:mm",
                // DateUtils.addMinutes(date, -(i * 10))).substring(0, 15) + "0";
                // 5분단위
                String key = DateUtil.formatDate("yyyy.MM.dd_HH:mm", DateUtils.addMinutes(date, -(i * 5)));

                if (Integer.parseInt(key.substring(key.length() - 1, key.length())) < 5) {
                    key = key.substring(0, key.length() - 1) + "0";
                }
                if (Integer.parseInt(key.substring(key.length() - 1, key.length())) > 5) {
                    key = key.substring(0, key.length() - 1) + "5";
                }

                // log.info("key :::: " + key);
                // log.info("key ::::: " + DateUtil.formatDate("yyyy.MM.dd_HH:mm",
                // DateUtils.addMinutes(date, -(i * 5))));
                // log.info("key ::::: " + DateUtil.formatDate("yyyy.MM.dd_HH:mm",
                // DateUtils.addMinutes(date, -(i * 5))).substring(0, 15) + "0");
                list.add(key);
            }
        }

        map.put(rsDto.getDataType(), list);

        return map;
    }

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
