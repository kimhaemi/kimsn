package kr.or.kimsn.radar.data.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

import kr.or.kimsn.radar.data.dto.pkColumn.CommonPk;
import lombok.Data;

@Data
@Entity
@IdClass(CommonPk.class)
public class ReceiveDto {

    @Id
    @Column(name = "data_kind")
    private String dataKind;
    @Id
    @Column(name = "data_type")
    private String dataType;
    @Id
    private String site;
    private String name_kr;
    private String data_name;
    private String sms_send_activation;
    private Integer status;

    @Column(name="agency_cd")
    private String agencyCd;
}
