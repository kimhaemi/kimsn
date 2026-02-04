package kr.or.kimsn.radarsms.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import kr.or.kimsn.radarsms.dto.pkColumn.SmsTargetMemberLinkPk;
import lombok.Data;

@Data
@Entity
@Table(name="sms_target_member_link")
@IdClass(SmsTargetMemberLinkPk.class)
public class SmsTargetMemberLinkDto {
    
    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mid")
    private Long id;

    @Id
    private Long gid;
    
    private Integer noti;
    private Integer warn;
    private Integer tota;
    private Integer retr;
    private Integer sms;
}
