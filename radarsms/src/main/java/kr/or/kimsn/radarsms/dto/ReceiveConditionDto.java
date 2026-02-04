package kr.or.kimsn.radarsms.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "receive_condition")
public class ReceiveConditionDto {
    
    @Id
    private String site;
    private String data_kind;
    @Column(name = "data_type") private String dataType;
    private String recv_condition;
    private String apply_time;
    private String last_check_time;
    private String sms_send;
    private String sms_send_activation;
    private Integer status;
}
