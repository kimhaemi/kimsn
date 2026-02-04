package kr.or.kimsn.radarsms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.or.kimsn.radarsms.dto.SmsTargetGroupMemberDto;
import kr.or.kimsn.radarsms.dto.pkColumn.SmsTargetGroupMemberPk;

public interface SmsTargetGroupMemberRepository extends JpaRepository<SmsTargetGroupMemberDto, SmsTargetGroupMemberPk> {
        @Query(nativeQuery = true, value = "select \n" +
                        "   T1.mid as mid\n" +
                        " , T1.name as name\n" +
                        " , T1.organization as organization\n" +
                        " , T1.department as department\n" +
                        " , T1.position as position\n" +
                        " , T1.phone_num as phone_num\n" +
                        " , T1.activation as activation \n" +
                        " , T2.gid as gid \n" +
                        " , T2.noti as noti \n" +
                        " , T2.warn as warn \n" +
                        " , T2.tota as tota \n" +
                        " , T2.retr as retr \n" +
                        " , T2.sms as sms \n" +
                        "from \n" +
                        "  sms_target_member T1, \n" +
                        "  sms_target_member_link T2 \n" +
                        "where 1=1\n" +
                        "and T1.mid = T2.mid \n" +
                        "and T1.activation = 1 \n" +
                        "and T2.sms = 1 \n" +
                        "and T2.gid > 1 \n" +
                        "order by T2.gid, T1.name \n")
        List<SmsTargetGroupMemberDto> getSmsTargetGroupMemberList();

        @Query(nativeQuery = true, value = "select /*getSmsTargetGroupsMemberId*/\n" +
                        "   T1.mid as mid \n" +
                        " , T1.name as name \n" +
                        " , T1.organization as organization \n" +
                        " , T1.department as department \n" +
                        " , T1.position as position \n" +
                        " , T1.phone_num as phone_num \n" +
                        " , T1.activation as activation \n" +
                        " , T2.gid as gid \n" +
                        " , T2.noti as noti \n" +
                        " , T2.warn as warn \n" +
                        " , T2.tota as tota \n" +
                        " , T2.retr as retr \n" +
                        " , T2.sms as sms \n" +
                        "from\n" +
                        "  sms_target_member T1,\n" +
                        "  sms_target_member_link T2 \n" +
                        "where 1=1 \n" +
                        "  and T1.mid = T2.mid \n" +
                        "  and T2.gid = :gid \n" +
                        "order by T1.organization, T1.name")
        List<SmsTargetGroupMemberDto> getSmsTargetGroupsMemberId(@Param("gid") Long gid);

        @Query(nativeQuery = true, value = "select /*getSmsTargetGroupsMemberId2*/\n" +
                        "   T1.mid as mid \n" +
                        " , T1.name as name \n" +
                        " , T1.organization as organization \n" +
                        " , T1.department as department \n" +
                        " , T1.position as position \n" +
                        " , T1.phone_num as phone_num \n" +
                        " , T1.activation as activation \n" +
                        " , T2.gid as gid \n" +
                        " , T2.noti as noti \n" +
                        " , T2.warn as warn \n" +
                        " , T2.tota as tota \n" +
                        " , T2.retr as retr \n" +
                        " , T2.sms as sms \n" +
                        "from\n" +
                        "  sms_target_member T1,\n" +
                        "  sms_target_member_link T2 \n" +
                        "where 1=1 \n" +
                        "  and T1.mid = T2.mid \n" +
                        "  and T2.gid in (:gid) \n" +
                        "order by T2.gid, T1.mid, T1.organization, T1.name")
        List<SmsTargetGroupMemberDto> getSmsTargetGroupsMemberId2(@Param("gid") List<String> gid);
}
