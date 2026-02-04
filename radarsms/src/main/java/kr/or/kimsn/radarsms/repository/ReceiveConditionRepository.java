package kr.or.kimsn.radarsms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kimsn.radarsms.dto.ReceiveConditionDto;

public interface ReceiveConditionRepository extends JpaRepository<ReceiveConditionDto, String> {

    List<ReceiveConditionDto> findByDataTypeOrderBySite(String data_type);

    List<ReceiveConditionDto> findBySiteAndDataType(String site, String data_type);

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
