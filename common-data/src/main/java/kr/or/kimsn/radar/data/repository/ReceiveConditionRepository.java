package kr.or.kimsn.radar.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kimsn.radar.data.dto.ReceiveConditionDto;

@Repository
public interface ReceiveConditionRepository extends JpaRepository<ReceiveConditionDto, String> {

    List<ReceiveConditionDto> findByDataTypeOrderBySite(String dataType);

    List<ReceiveConditionDto> findBySiteAndDataType(String site, String dataType);

    List<ReceiveConditionDto> findByDataKindAndDataType(String dataKind, String dataType);

    ReceiveConditionDto findByDataKindAndDataTypeAndSite(String dataKind, String dataType, String Site);

    @Query(nativeQuery = true, value = "update watchdog.receive_condition set\n" +
        // " apply_time = DATE_FORMAT(:apply_time, '%Y-%m-%d %H:%i:%S'), \n" +
        "  last_check_time = now(), \n" +
        // " recv_condition = :new_recv_condition, \n" +
        // " codedtl = :new_codedtl, \n" +
        "  sms_send = :sms_send \n" +
        "where 1=1 \n" +
        "  and (:where_recv_condition is null or recv_condition = :where_recv_condition) \n" +
        "  and (:site is null or site = :site) \n" +
        "  and data_kind = :dataKindStr \n" +
        "  and data_type = :dataType \n")
    @Transactional
    @Modifying
    // 문자 발송 update
    Integer updateReceiveConditionSms(
        // @Param("apply_time") String apply_time,
        // @Param("new_recv_condition") String new_recv_condition,
        // @Param("new_codedtl") String new_codedtl,
        @Param("sms_send") int sms_send,
        @Param("where_recv_condition") String where_recv_condition,
        @Param("site") String site,
        @Param("dataKindStr") String dataKindStr,
        @Param("dataType") String dataType);

    @Query(nativeQuery = true, value = "update watchdog.receive_condition set\n" +
        "  apply_time = DATE_FORMAT(:apply_time, '%Y-%m-%d %H:%i:%S'), \n" +
        "  last_check_time = now(), \n" +
        "  recv_condition = :new_recv_condition, \n" +
        "  codedtl = :new_codedtl, \n" +
        "  sms_send = :sms_send \n" +
        "where 1=1 \n" +
        "  and (:where_recv_condition is null or recv_condition = :where_recv_condition) \n" +
        "  and (:site is null or site = :site) \n" +
        "  and data_kind = :dataKindStr \n" +
        "  and data_type = :dataType \n")
    @Transactional
    @Modifying
    // 최종결과 update
    Integer updateReceiveCondition(
        @Param("apply_time") String apply_time,
        @Param("new_recv_condition") String new_recv_condition,
        @Param("new_codedtl") String new_codedtl,
        @Param("sms_send") int sms_send,
        @Param("where_recv_condition") String where_recv_condition,
        @Param("site") String site,
        @Param("dataKindStr") String dataKindStr,
        @Param("dataType") String dataType);

    @Query(
        nativeQuery = true,
        value=
        "update receive_condition set\n"+
        "  sms_send_activation = :sms_send_activation \n"+
        "where 1=1 \n"+
        "  and data_kind = :data_kind \n"+
        "  and site = :site \n"+
        "  and data_type = :dataType \n"
    )
    @Transactional
    @Modifying
    // 지점/자료별 문자 발송 설정 일괄 수정
    Integer setReceiveConditionModify(
        @Param("sms_send_activation") int sms_send_activation,
        @Param("data_kind") String data_kind,
        @Param("site") String site,
        @Param("dataType") String dataType
    );

}
