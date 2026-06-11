package kr.or.kimsn.radar.data.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import kr.or.kimsn.radar.data.dto.pkColumn.CommonPk;
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

    @Column(name = "recv_Condition")
    private String recvCondition;
    @Column(name = "apply_time")
    private String applyTime;
    @Column(name = "last_check_time")
    private String lastCheckTime;
    @Column(name = "sms_send")
    private int smsSend;
    @Column(name = "sms_send_activa")
    private int smsSendActiva;
    private int status;
    @Column(name = "name_kr")
    private String nameKr;
	  private Integer gubun;
    @Column(name = "permitted_watch")
	  private Integer permittedWatch ;
    @Column(name = "agency_cd")
    private String agencyCd;
    @Column(name = "style_attr")
    private String styleAttr;
}
