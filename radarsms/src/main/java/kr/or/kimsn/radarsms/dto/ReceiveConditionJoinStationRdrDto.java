package kr.or.kimsn.radarsms.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import kr.or.kimsn.radarsms.dto.pkColumn.CommonPk;
import lombok.Data;

@Data
@Entity
@IdClass(CommonPk.class)
public class ReceiveConditionJoinStationRdrDto {

    @Id
    private String site;
    @Id
    @Column(name = "data_kind")
    private String dataKind;
    @Id
    @Column(name = "data_type")
    private String dataType;
    
    private String recv_condition;
    private String apply_time;
    private String last_check_time;
    private String sms_send;
    private String sms_send_activation;
    private Integer status;
    private String name_kr;
	private Integer gubun;
	private Integer permitted_watch ;
    
}
