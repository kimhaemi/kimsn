package kr.or.kimsn.radarsms.dto;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import kr.or.kimsn.radarsms.dto.pkColumn.SmsTargetGroupMemberPk;
import lombok.Data;

@Data
@Entity
@IdClass(SmsTargetGroupMemberPk.class)
public class SmsTargetGroupMemberDto {

    @Id
    private Long mid;
    @Id
    private Long gid;
    private String name;
    private String organization;

    private String department;
    private String position;
    private String phone_num;

    private Integer noti;
    private Integer warn;
    private Integer tota;
    private Integer retr;
    private Integer sms;

}
