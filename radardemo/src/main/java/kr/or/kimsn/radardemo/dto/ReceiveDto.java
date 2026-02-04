package kr.or.kimsn.radardemo.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import kr.or.kimsn.radardemo.dto.pkColumn.CommonPk;
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
    
}
