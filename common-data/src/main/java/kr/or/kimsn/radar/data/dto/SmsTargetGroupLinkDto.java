package kr.or.kimsn.radar.data.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import kr.or.kimsn.radar.data.dto.pkColumn.SmsTargetGroupLinkPk;
import lombok.Data;

@Data
@Entity
@Table(name = "sms_target_group_link")
@IdClass(SmsTargetGroupLinkPk.class)
public class SmsTargetGroupLinkDto {

    @Id
    private String site;
    @Id
    @Column(name = "data_kind")
    private String dataKind;
    @Id
    @Column(name = "data_type")
    private String dataType;
    @Id
    private String group_id;

    @Column(name = "agency_cd")
    private String agencyCd;
}
