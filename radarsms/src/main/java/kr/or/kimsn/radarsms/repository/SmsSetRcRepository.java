package kr.or.kimsn.radarsms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import kr.or.kimsn.radarsms.dto.ReceiveConditionDto;
import kr.or.kimsn.radarsms.dto.SmsSetRcDto;

public interface SmsSetRcRepository extends JpaRepository<SmsSetRcDto, String> {
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
            "T2.sort_order as sort_order\n" +
            "from watchdog.receive_condition T1\n" +
            "left outer join watchdog.station_rdr T2 \n" +
            "on T1.site = T2.site_cd \n" +
            "join watchdog.receive_setting T3 \n" +
            "where 1=1 \n" +
            "and T1.data_kind=T3.data_kind \n" +
            "and T1.data_type=T3.data_type \n" +
            "and T1.data_type = 'NQC'" +
            "order by \n" +
            "gubun, data_kind desc, T2.sort_order asc, data_type asc", nativeQuery = true)
    List<SmsSetRcDto> findReceiveConditionStationRdrReceiveSetting();
}
