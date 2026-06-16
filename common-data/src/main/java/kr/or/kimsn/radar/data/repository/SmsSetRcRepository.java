package kr.or.kimsn.radar.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import kr.or.kimsn.radar.data.dto.ReceiveConditionDto;
import kr.or.kimsn.radar.data.dto.SmsSetRcDto;
import kr.or.kimsn.radar.data.dto.pkColumn.CommonPk;

public interface SmsSetRcRepository extends JpaRepository<SmsSetRcDto, CommonPk> {
    List<ReceiveConditionDto> findBySiteAndDataType(String site, String data_type);

    @Query(value = "select \n" +
            "T1.data_kind as data_kind, \n" +
            "T1.site as site, \n" +
            "T2.name_kr as name_kr,\n" +
            "T1.data_type as data_type, \n" +
            "T3.data_name as data_name,\n" +
            "T1.sms_send_activation as sms_send_activation,\n" +
            "T1.status as status,\n" +
            "T2.gubun as gubun,\n" +
            "T2.sort_order as sort_order,\n" +
            "T1.agency_cd AS agency_cd \n" +
            "from watchdog.receive_condition T1\n" +
            "left outer join watchdog.station_rdr T2 \n" +
            "on T1.site = T2.site_cd \n" +
            "join watchdog.receive_setting T3 \n" +
            "where 1=1 \n" +
            "and T1.data_kind=T3.data_kind \n" +
            "and T1.data_type=T3.data_type \n" +
            "and T1.data_type = 'NQC'" +
            "ORDER BY \n" +
            "T2.gubun ASC, T2.sort_order asc, T1.agency_cd ASC, T1.data_kind desc, T1.data_type asc"

//            + "    T2.gubun ASC,          -- 1. 대형 -> 소형 -> 공항 순으로 먼저 뭉침\n"
//            + "    T2.sort_order ASC,     -- 2. 레이더 순서 정렬\n"
//            + "    T1.data_kind DESC,     -- 3. 자료 종류 일치용\n"
//            + "    T1.data_type ASC,      -- 4. NQC 일치용\n"
//            + "    T1.agency_cd ASC       -- 5. 기상청(KMA)이 무조건 왼쪽, 기후부(MCEE)가 무조건 오른쪽으"
        , nativeQuery = true)
    List<SmsSetRcDto> findReceiveConditionStationRdrReceiveSetting();
}
