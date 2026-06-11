package kr.or.kimsn.radar.data.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "receive_condition", catalog = "watchdog")
public class ReceiveConditionDto {

    @Id
    private String site;
    @Column(name = "data_kind")
    private String dataKind;
    @Column(name = "data_type")
    private String dataType;
    @Column(name = "recv_condition")
    private String recvCondition;
    @Column(name = "apply_time")
    private String applyTime;
    @Column(name = "last_check_time")
    private String lastCheckTime;
    @Column(name = "sms_send")
    private int smsSend;
    @Column(name = "sms_send_activation")
    private int smsSendActivation;
    private int status;
    private String codedtl;

    @Column(name = "agency_cd")
    private String agencyCd;
}
