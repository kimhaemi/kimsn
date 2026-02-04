package kr.or.kimsn.radarsms.dto;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import kr.or.kimsn.radarsms.dto.pkColumn.CommonPk;
import lombok.Data;

@Data
@Entity
@IdClass(CommonPk.class)
public class SmsSetRcDto {

    @Id
    private String dataKind;
    @Id
    private String site;
    @Id
    private String dataType;
    private String name_kr;
    private String data_name;
    private Integer sms_send_activation;
    private Integer status;
    private Integer gubun;
    private Integer sort_order;

}
