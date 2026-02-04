package kr.or.kimsn.radardemo.dto;

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
    @Column(name = "data_type") private String dataType;
    private String recv_condition;
    private String apply_time;
    private String last_check_time;
    private int sms_send;
    private int sms_send_activation;
    private int status;
    private String codedtl;
}
