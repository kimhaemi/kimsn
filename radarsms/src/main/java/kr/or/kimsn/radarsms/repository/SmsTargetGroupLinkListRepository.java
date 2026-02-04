package kr.or.kimsn.radarsms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.or.kimsn.radarsms.dto.SmsTargetGroupLinkListDto;

public interface SmsTargetGroupLinkListRepository extends JpaRepository<SmsTargetGroupLinkListDto, String> {

    @Query(nativeQuery = true, value = "select /*getTableJoinAll*/\n" +
            "  T1.site as site, \n" +
            "  T1.data_kind as data_kind, \n" +
            "  T1.data_type as data_type, \n" +
            "  T1.group_id as group_id,\n" +
            "  T2.name_kr as name_kr, \n" +
            "  T3.data_name as data_name\n" +
            "from\n" +
            "  sms_target_group_link T1\n" +
            "left outer join\n" +
            "  station_rdr T2\n" +
            "on\n" +
            "  T1.site = T2.site_cd\n" +
            "join\n" +
            "  receive_setting T3\n" +
            "where\n" +
            "  T1.data_kind = T3.data_kind\n" +
            "and T1.data_type = T3.data_type\n" +
            "and T1.group_id = :id \n" +
            "order by \n" +
            "T1.data_kind asc,\n" +
            "T2.name_kr, T3.data_name")
    List<SmsTargetGroupLinkListDto> getTableJoinAll(@Param("id") Long id);
}
