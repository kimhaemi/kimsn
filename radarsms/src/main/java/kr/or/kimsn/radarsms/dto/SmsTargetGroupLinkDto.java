package kr.or.kimsn.radarsms.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import kr.or.kimsn.radarsms.dto.pkColumn.SmsTargetGroupLinkPk;
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
}
