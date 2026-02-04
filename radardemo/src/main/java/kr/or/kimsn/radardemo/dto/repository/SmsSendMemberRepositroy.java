package kr.or.kimsn.radardemo.dto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.or.kimsn.radardemo.dto.SmsSendMemberDto;

public interface SmsSendMemberRepositroy extends JpaRepository<SmsSendMemberDto, String> {

    @Query(nativeQuery = true, value = "select \n" +
            "  DISTINCT stm.name, \n" +
            "  stm.phone_num,\n" +
            "  stml.warn, -- 경고 -- MISS\n" +
            "  stml.tota, -- 네트워크 오류\n" +
            "  stml.retr, -- 복구 -- 장애 복구, 네트워크 복구\n" +
            "  stml.sms  -- 문자 발송 여부\n" +
            "from watchdog.sms_target_group stg \n" +
            "left outer join watchdog.sms_target_group_link stgl\n" +
            "  on stg.gid = stgl.group_id\n" +
            "left outer join watchdog.sms_target_member_link stml \n" +
            "  on stgl.group_id = stml.gid \n" +
            "left outer join watchdog.sms_target_member stm \n" +
            "on stml.mid = stm.mid \n" +
            "where 1=1\n" +
            "and stg.status = 1\n" +
            "and stg.activation = 1\n" +
            "and stgl.data_type = 'NQC'\n" +
            "and stm.activation = 1\n" +
            "and stgl.data_kind = :data_kind -- param\n" +
            "and (:site is null or stgl.site = :site) -- param\n" +
            "order by stm.name asc\n")
    // site 수신그룹 담당자
    List<SmsSendMemberDto> getSmsSendMemberList(
            @Param("data_kind") String data_kind,
            @Param("site") String site);
}
