package kr.or.kimsn.radar.data.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import kr.or.kimsn.radar.data.dto.pkColumn.SmsTargetMemberLinkPk;
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
