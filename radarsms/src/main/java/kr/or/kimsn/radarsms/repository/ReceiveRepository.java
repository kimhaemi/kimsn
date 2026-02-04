package kr.or.kimsn.radarsms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import kr.or.kimsn.radarsms.dto.ReceiveDto;

public interface ReceiveRepository extends JpaRepository<ReceiveDto, String> {
    @Query(nativeQuery = true, value = "select /*getReceiveCondition*/\n" +
            "  T1.data_kind as data_kind,\n" +
            "  T1.site as site, \n" +
            "  T2.name_kr as name_kr,\n" +
            "  T1.data_type as data_type, \n" +
            "  T3.data_name as data_name,\n" +
            "  T1.sms_send_activation as sms_send_activation, \n" +
            "  T1.status as status \n" +
            "from\n" +
            "  receive_condition T1\n" +
            "left outer join station_rdr T2\n" +
            "on\n" +
            "  T1.site = T2.site_cd\n" +
            "join\n" +
            "  receive_setting T3\n" +
            "where\n" +
            "  T1.data_kind=T3.data_kind and\n" +
            "  T1.data_type=T3.data_type\n" +
            "order by\n" +
            "  T1.data_kind asc,\n" +
            "  name_kr asc,\n" +
            "  T1.data_type asc")
    List<ReceiveDto> getReceiveTableJoin();
}
