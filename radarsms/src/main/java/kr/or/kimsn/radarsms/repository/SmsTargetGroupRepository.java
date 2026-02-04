package kr.or.kimsn.radarsms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kimsn.radarsms.dto.SmsTargetGroupDto;

public interface SmsTargetGroupRepository extends JpaRepository<SmsTargetGroupDto, Long> {

    List<SmsTargetGroupDto> findByOrderBySortOrderAsc();

    // List<SmsTargetGroupDto> findByGid(Long gid);
    List<SmsTargetGroupDto> findByIdLessThan(Long gid);

    @Query(
        nativeQuery = true,
        value=
        "update sms_target_group set  \n"+
		"  activation = :activation \n"+
	    "where 1=1 \n"+
		"  and gid = :id \n"
    )
    @Transactional
    @Modifying
    //문자 수신 그룹 관리 설정 일괄 수정
    Integer setSmsTargetGroupModify(
        @Param("id") Long id,
        @Param("activation") String activation
    );
    
}
