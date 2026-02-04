package kr.or.kimsn.radarsms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.or.kimsn.radarsms.dto.SmsTargetMemberDto;

public interface SmsTargetMemberRepository extends JpaRepository<SmsTargetMemberDto, Long>{

    List<SmsTargetMemberDto> findByOrderByName();

    @Query(
        nativeQuery = true,
        value=
        "select * \n" +
        "from sms_target_member \n" +
        "where 1=1 \n" +
        "  and mid not in (\n" +
        "     select T1.mid \n" +
        "     from sms_target_member T1,\n" +
        "          sms_target_member_link T2\n" +
        "     where 1=1 \n" +
        "       and T1.mid = T2.mid \n" +
        "       and T2.gid = :gid \n" +
        "  )\n" +
        "order by organization, name"
    )
    // 문자 수신 그룹 멤버 ([ *** ] 그룹에 속하지 않은 사용자)
    List<SmsTargetMemberDto> getSmsTargetGroupsMemberIdNot(@Param("gid") Long gid);
}
