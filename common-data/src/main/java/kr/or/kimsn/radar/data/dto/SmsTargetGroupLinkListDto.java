package kr.or.kimsn.radar.data.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import lombok.Data;

@Data
@Entity
public class SmsTargetGroupLinkListDto {
    @Id
    private String site;
    private String dataKind;
    private String dataType;
    private String group_id;

    private String name_kr;
    private String data_name;

    @Column(name="agency_cd")
    private String agencyCd;
}
