package kr.or.kimsn.radardemo.dto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kimsn.radardemo.dto.ReceiveConditionDto;

public interface ReceiveConditionRepository extends JpaRepository<ReceiveConditionDto, String> {

        List<ReceiveConditionDto> findByDataTypeOrderBySite(String data_type);

        List<ReceiveConditionDto> findBySiteAndDataType(String site, String data_type);

        List<ReceiveConditionDto> findByDataKindAndDataType(String data_kind, String data_type);

        ReceiveConditionDto findByDataKindAndDataTypeAndSite(String data_kind, String data_type, String Site);

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

}
